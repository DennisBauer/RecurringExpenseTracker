// OPFS-backed SQLite (sqlite-wasm) requires cross-origin isolation.
;(function(config) {
    config.devServer = config.devServer || {};
    config.devServer.headers = [
        { key: 'Cross-Origin-Opener-Policy', value: 'same-origin' },
        { key: 'Cross-Origin-Embedder-Policy', value: 'require-corp' }
    ];
})(config);
