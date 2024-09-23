package org.noisevisionproductions.samplelibrary.database

import app.cash.sqldelight.drivers.native.NativeSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import org.noisevisionproductions.samplelibrary.SampleDatabase

actual fun getSqlDriver(context: Any?): SqlDriver {
    // iOS nie potrzebuje kontekstu, więc go ignorujemy
    return NativeSqliteDriver(SampleDatabase.Schema, "sample.db")
}
