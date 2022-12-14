package com.example.payten_template.ui.core.arhiva

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.payten_template.Data.Rezervacija
import com.example.payten_template.Data.repositories.RezervacijeRepository
import com.example.payten_template.utils.rezervationDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ArhivaViewModel: ViewModel() {

    val rezervacije = mutableStateListOf<Rezervacija>()
    var isLoading by mutableStateOf(false)

    //private var _isLoading = false
    private val rezervacijeRepository = RezervacijeRepository.Instance
    private val networkScope = CoroutineScope(Dispatchers.IO)


    init {
        refresh()
        viewModelScope.launch {
            rezervacijeRepository.rezervacije.collect{
                rezervacije.clear()
                rezervacije.addAll(it.filter { rez ->
                    rez.reservation.rezervationDate()?.let{ date ->
                        return@filter date.toLocalDate().isBefore(LocalDate.now())
                    }
                    false
                }
                .sortedBy { rez ->
                    rez.reservation.rezervationDate()!!
                }
                )
            }
        }
    }

    fun refresh(){
        networkScope.launch {
            withContext(Dispatchers.Main){
                isLoading = true
            }
            try{
                val rezs = rezervacijeRepository.getAllReservations()
            }catch (ex: Exception){ }
            withContext(Dispatchers.Main){
                isLoading = false
            }
        }
    }

}