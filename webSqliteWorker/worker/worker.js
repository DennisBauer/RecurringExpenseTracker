/*
 * SQLite worker implementing the androidx.sqlite WebWorkerSQLiteDriver message
 * protocol (open / prepare / step / close) on top of SQLite WASM with OPFS
 * persistence.
 *
 * Storage uses the "opfs-sahpool" VFS. Unlike the plain "opfs" VFS it needs no
 * SharedArrayBuffer and therefore no COOP/COEP headers, so the app runs on any
 * static host (e.g. GitHub Pages). The pool holds exclusive OPFS access handles,
 * so only one tab can open the database at a time.
 *
 * Adapted from the androidx sample worker (Apache License 2.0):
 * https://github.com/danysantiago/room-web-demo
 */
import sqlite3InitModule from '@sqlite.org/sqlite-wasm';

let sqlite3 = null;
let sahPool = null;
// Set when SQLite or the OPFS pool failed to initialize; every request is answered with it.
let initError = null;

// Maps to track active database connections and prepared statements by their unique IDs.
const databases = new Map(); // stores databaseId -> SQLiteDbObject
const statements = new Map(); // stores statementId -> SQLiteStatementObject

// Counters to generate unique IDs for new database connections and statements.
let nextDatabaseId = 0;
let nextStatementId = 0;

function openRequest(id, requestData) {
    try {
        const newDatabase = new sahPool.OpfsSAHPoolDb(requestData.fileName);
        const newDatabaseId = nextDatabaseId++;
        databases.set(newDatabaseId, newDatabase);
        postMessage({'id': id, data: {'databaseId': newDatabaseId}});
    } catch (error) {
        postMessage({'id': id, error: error.message});
    }
}

function prepareRequest(id, requestData) {
    try {
        const database = databases.get(requestData.databaseId);
        if (!database) {
            postMessage({'id': id, error: "Invalid database ID: " + requestData.databaseId});
            return;
        }
        const statement = database.prepare(requestData.sql);
        const newStatementId = nextStatementId++;
        statements.set(newStatementId, statement);
        // Not Stmt.getColumnNames(): it throws for statements without result columns (e.g. DDL).
        const columnNames = [];
        for (let i = 0; i < statement.columnCount; i++) {
            columnNames.push(statement.getColumnName(i));
        }
        postMessage({
            'id': id,
            data: {
                'statementId': newStatementId,
                'parameterCount': statement.parameterCount,
                'columnNames': columnNames,
            },
        });
    } catch (error) {
        postMessage({'id': id, error: error.message});
    }
}

function stepRequest(id, requestData) {
    const statement = statements.get(requestData.statementId);
    if (!statement) {
        postMessage({'id': id, error: "Invalid statement ID: " + requestData.statementId});
        return;
    }
    try {
        const resultData = {
            'rows': [],
            'columnTypes': []
        };
        statement.reset();
        statement.clearBindings();
        for (let i = 0; i < requestData.bindings.length; i++) {
            statement.bind(i + 1, requestData.bindings[i]);
        }
        while (statement.step()) {
            if (!resultData.columnTypes.length) {
                for (let i = 0; i < statement.columnCount; i++) {
                    resultData.columnTypes.push(sqlite3.capi.sqlite3_column_type(statement, i));
                }
            }
            resultData.rows.push(statement.get([]));
        }
        postMessage({'id': id, data: resultData});
    } catch (error) {
        postMessage({'id': id, error: error.message});
    }
}

// "close" is a one-way command: the driver expects no response, so failures are only logged.
// IDs start at 0, hence the explicit null checks.
function closeRequest(requestData) {
    if (requestData.statementId != null) {
        const statement = statements.get(requestData.statementId);
        statements.delete(requestData.statementId);
        if (!statement) {
            console.error("close: invalid statement ID: " + requestData.statementId);
        } else {
            try {
                statement.finalize();
            } catch (error) {
                console.error("close: failed to finalize statement", error);
            }
        }
    }

    if (requestData.databaseId != null) {
        const database = databases.get(requestData.databaseId);
        databases.delete(requestData.databaseId);
        if (!database) {
            console.error("close: invalid database ID: " + requestData.databaseId);
        } else {
            try {
                database.close();
            } catch (error) {
                console.error("close: failed to close database", error);
            }
        }
    }
}

function handleMessage(e) {
    const requestMsg = e.data;
    if (requestMsg == null || requestMsg.data == null) {
        postMessage(
            {'id': requestMsg?.id, 'error': "Invalid request, missing 'data'."}
        );
        return;
    }
    const requestData = requestMsg.data;
    if (requestData.cmd == null) {
        postMessage(
            {'id': requestMsg.id, 'error': "Invalid request, missing 'cmd'."}
        );
        return;
    }
    if (initError) {
        if (requestData.cmd !== 'close') {
            postMessage({'id': requestMsg.id, 'error': initError});
        }
        return;
    }
    switch (requestData.cmd) {
        case 'open':
            openRequest(requestMsg.id, requestData);
            break;
        case 'prepare':
            prepareRequest(requestMsg.id, requestData);
            break;
        case 'step':
            stepRequest(requestMsg.id, requestData);
            break;
        case 'close':
            closeRequest(requestData);
            break;
        default:
            postMessage(
                {'id': requestMsg.id, 'error': "Invalid request, unknown command: '" + requestData.cmd + "'."}
            );
    }
}

// Queue incoming requests until SQLite WASM and the OPFS pool have finished initializing.
const messageQueue = [];
let ready = false;
onmessage = (e) => {
    if (!ready) {
        messageQueue.push(e);
    } else {
        handleMessage(e);
    }
};

function drainQueue() {
    ready = true;
    while (messageQueue.length > 0) {
        handleMessage(messageQueue.shift());
    }
}

sqlite3InitModule()
    .then(async (instance) => {
        sqlite3 = instance;
        sahPool = await sqlite3.installOpfsSAHPoolVfs();
    })
    .catch((error) => {
        initError = "SQLite initialization failed: " + (error?.message ?? error);
        console.error(initError, error);
    })
    .finally(drainQueue);
