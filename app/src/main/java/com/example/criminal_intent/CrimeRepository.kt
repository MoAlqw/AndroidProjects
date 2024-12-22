package com.example.criminal_intent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.criminal_intent.database.Crime
import com.example.criminal_intent.database.CrimeDatabase
import com.example.criminal_intent.database.migration_1_2
import java.util.UUID
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context){

    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2)
        .build()

    private val dao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getCrimes(): LiveData<List<Crime>> = dao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = dao.getCrime(id)

    fun updateCrime(crime: Crime) {
        executor.execute {
            dao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            dao.addCrime(crime)
        }
    }

    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null)
                INSTANCE = CrimeRepository(context)
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }

    }

}