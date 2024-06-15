package model

import android.content.Context
import android.net.Uri
import forEachEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class DatabaseBackupRestore {
    suspend fun exportDatabaseFile(
        databasePath: String,
        targetUri: Uri,
        applicationContext: Context,
    ): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                writeFilesToZip(
                    listOf(databasePath, "$databasePath-shm", "$databasePath-wal"),
                    targetUri,
                    applicationContext,
                )
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    suspend fun importDatabaseFile(
        srcZipUri: Uri,
        targetPath: String,
        applicationContext: Context,
    ): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                extractZipToDirectory(
                    srcZipUri,
                    targetPath,
                    applicationContext,
                )
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    private fun writeFilesToZip(
        files: List<String>,
        targetUri: Uri,
        applicationContext: Context,
    ) {
        val outputStream = applicationContext.contentResolver.openOutputStream(targetUri) ?: return
        ZipOutputStream(outputStream).use { zipOutputStream ->
            files.forEach { file ->
                val inputFile = File(file)
                val fileName = inputFile.name

                zipOutputStream.putNextEntry(ZipEntry(fileName))

                inputFile.inputStream().use { input ->
                    input.copyTo(zipOutputStream)
                }
            }
        }
    }

    private fun extractZipToDirectory(
        srcZipUri: Uri,
        targetPath: String,
        applicationContext: Context,
    ) {
        val inputStream = applicationContext.contentResolver.openInputStream(srcZipUri) ?: return
        ZipInputStream(inputStream).use { zipInputStream ->
            zipInputStream.forEachEntry { entry ->
                val outputFile = File(targetPath, entry.name)
                // To prevent CWE-22.
                // See Zip Slip Vulnerability: https://security.snyk.io/research/zip-slip-vulnerability
                if (!outputFile.canonicalFile.normalize().startsWith(File(targetPath).canonicalFile)) {
                    throw Exception("Bad zip entry")
                }
                outputFile.outputStream().use { output ->
                    zipInputStream.copyTo(output)
                }
            }
        }
    }
}
