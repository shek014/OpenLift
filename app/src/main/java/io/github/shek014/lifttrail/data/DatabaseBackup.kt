package io.github.shek014.lifttrail.data

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.net.Uri
import io.github.shek014.lifttrail.data.local.LiftTrailDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.LocalDate

/** The file chosen can't be restored. Nothing on the phone has changed. */
class BackupRejectedException : Exception()

/** The name suggested when saving a backup, such as "LiftTrail-backup-2026-11-06.db". */
fun backupFileName(date: LocalDate): String = "LiftTrail-backup-$date.db"

/** Whether these are the first bytes of a database file. */
fun hasDatabaseHeader(firstBytes: ByteArray): Boolean {
    val header = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
    return firstBytes.size >= header.size && header.indices.all { firstBytes[it] == header[it] }
}

/** Copies the whole database to a file someone chooses, and back again. Backups made as OpenLift restore too. */
class DatabaseBackup(private val context: Context, private val database: LiftTrailDatabase) {
    suspend fun save(destination: Uri) = withContext(Dispatchers.IO) {
        // Moves recent changes out of the database's side file, into the main file being copied.
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
        val output = context.contentResolver.openOutputStream(destination, "wt") ?: throw IOException()
        output.use { out ->
            context.getDatabasePath(LiftTrailDatabase.FileName).inputStream().use { it.copyTo(out) }
        }
    }

    /**
     * Replaces the database with a backup, which the app must then restart to use. The backup is
     * checked and brought up to date on a separate copy first, so that a bad file can't do any harm.
     *
     * @throws BackupRejectedException if the file isn't a backup this version of the app can read.
     */
    suspend fun restore(source: Uri) = withContext(Dispatchers.IO) {
        val candidate = context.getDatabasePath(CandidateFileName)
        candidate.parentFile?.mkdirs()
        deleteWithSideFiles(candidate)
        try {
            val input = context.contentResolver.openInputStream(source) ?: throw IOException()
            input.use { from -> candidate.outputStream().use { from.copyTo(it) } }
            if (!isLiftTrailDatabase(candidate)) throw BackupRejectedException()
            upgrade(CandidateFileName)

            database.close()
            val current = context.getDatabasePath(LiftTrailDatabase.FileName)
            deleteWithSideFiles(current)
            if (!candidate.renameTo(current)) throw IOException("Couldn't move the backup into place")
        } finally {
            deleteWithSideFiles(candidate)
        }
    }

    /** Opening it runs any migrations, and fails if it's from a newer version of the app. */
    private fun upgrade(fileName: String) {
        val opened = LiftTrailDatabase.create(context, fileName)
        try {
            opened.openHelper.writableDatabase
        } catch (_: RuntimeException) {
            throw BackupRejectedException()
        } finally {
            opened.close()
        }
    }

    // Android's default response to a damaged file is to delete it and start again with an empty
    // database, which would then pass as a valid backup. So this has to rule that out first.
    private fun isLiftTrailDatabase(file: File): Boolean {
        val firstBytes = ByteArray(HeaderSize)
        val count = file.inputStream().use { it.read(firstBytes) }
        if (!hasDatabaseHeader(firstBytes.copyOf(count.coerceAtLeast(0)))) return false
        return try {
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE, DatabaseErrorHandler {}).use { db ->
                val intact = db.rawQuery("PRAGMA quick_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }
                val tables = db.rawQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name IN " +
                        "('room_master_table', 'exercises', 'workouts', 'workout_exercises', 'workout_sets')",
                    null,
                ).use { it.moveToFirst(); it.getInt(0) }
                intact && tables == RequiredTableCount
            }
        } catch (_: SQLiteException) {
            false
        }
    }

    private fun deleteWithSideFiles(file: File) {
        listOf("", "-wal", "-shm", "-journal").forEach { File(file.path + it).delete() }
    }

    private companion object {
        const val CandidateFileName = "restore-candidate.db"
        const val HeaderSize = 16
        const val RequiredTableCount = 5
    }
}
