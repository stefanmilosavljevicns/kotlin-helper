package com.example.payten_template

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.payten_template.Data.Rezervacija
import com.example.payten_template.Data.Usluge
import com.example.payten_template.Data.repositories.RezervacijeRepository
import com.example.payten_template.navigation.SetupNavGraph
import com.example.payten_template.payment.PaymentManager
import com.example.payten_template.payment.PaynetBroadcastReceiver
import com.example.payten_template.payment.domain.request.PaynetMessage
import com.example.payten_template.ui.core.rezervacije.RezervacijeViewModel
import com.example.payten_template.ui.theme.PaytentemplateTheme
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private val rezervacijeViewModel: RezervacijeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rezervacijeViewModel.refresh()
        setContent {
            PaytentemplateTheme{
                val navController = rememberNavController()
                SetupNavGraph(navController = navController)
            }
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getStringExtra("ResponseResult")?.let {
            try{
                val response: PaynetMessage = json.decodeFromString(it)
                if(response.response!!.financial.result.code.equals("approved", ignoreCase = true)){
                    Toast.makeText(this, response.response!!.financial.result.message, Toast.LENGTH_SHORT).show()
                    // update reservation
                    RezervacijeRepository.Instance.let { repo ->
                        repo.rezervacije.value.find { it.id.equals(PaynetBroadcastReceiver.reservationId) }?.let { rez ->
                            val amount = Usluge[rez.services] ?: 600.0
                            CoroutineScope(Dispatchers.IO).launch {
                                rez.placeno = true
                                repo.updateReservation(rez)
                                rezervacijeViewModel.refresh()
                                PaymentManager(this@MainActivity).requestPrintBill(
                                    rez,
                                    amount,
                                )
                            }

                        }
                    }
                }else{}
            }catch (ex: Exception){
                Log.e("PaynetResponse", ex.message?:"")
            }
        }
    }

}



