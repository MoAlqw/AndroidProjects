package com.example.criminal_intent

import androidx.lifecycle.ViewModel
import com.example.criminal_intent.database.Crime

class CrimeListViewModel: ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    val crimeLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime){
        crimeRepository.addCrime(crime)
    }

}