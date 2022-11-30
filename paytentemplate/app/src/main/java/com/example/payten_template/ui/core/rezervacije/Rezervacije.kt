package com.example.payten_template

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.ui.tooling.preview.Preview
import com.example.payten_template.Data.EUsluge
import com.example.payten_template.Data.Usluge
import com.example.payten_template.navigation.Screen
import com.example.payten_template.payment.PaymentManager
import com.example.payten_template.ui.core.rezervacije.RezervacijeViewModel
import com.example.payten_template.ui.shared.Rezervacija
import com.example.payten_template.ui.shared.TerminListItem
import com.example.payten_template.utils.rezervationDate
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Rezervacije(
    navController: NavController,
    rezervacijeViewModel: RezervacijeViewModel = viewModel(
        viewModelStoreOwner = ((LocalContext.current as? ViewModelStoreOwner)?: LocalViewModelStoreOwner) as ViewModelStoreOwner
    )
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ScanQRCode()){
        if (it is QRResult.QRSuccess) {
            Toast.makeText(context, it.content.rawValue, Toast.LENGTH_SHORT).show()
        }
        if (it is QRResult.QRError){
            Toast.makeText(context, "Nevalidan QR code", Toast.LENGTH_SHORT).show()
        }
    }
    val paymentManager = PaymentManager(LocalContext.current)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp)
        ) {
            Row(horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(modifier = Modifier
                    .clickable {
                        navController.popBackStack()
                    }
                    .padding(8.dp), imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
                Spacer(modifier = Modifier.size(8.dp))
                Column {
                    Text(text = "Rezervacije",  style = MaterialTheme.typography.h4)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier.clickable {
                        launcher.launch(null)
                    }.size(32.dp)
                    ,
                    painter = painterResource(id = R.drawable.ic_qr_code),
                    contentDescription = "scan qr code"
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Lista trenutnih rezervacija", style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.size(16.dp))
            SwipeRefresh(
                modifier = Modifier.weight(1f),
                state = rememberSwipeRefreshState(rezervacijeViewModel.isLoading),
                onRefresh = {
                rezervacijeViewModel.refresh()
            }) {
                LazyColumn(
                    content = {
                        var date:LocalDate? = null
                        items(rezervacijeViewModel.rezervacije){ rezervacija ->
                            /*val newDate = rezervacija.reservation.rezervationDate()!!.toLocalDate()
                            if(date == null){
                                Text(text = newDate.toString())
                                Spacer(modifier = Modifier.size(8.dp))
                                date = newDate
                            }else{
                                if(newDate.isAfter(date)){
                                    Text(text = newDate.toString())
                                    Spacer(modifier = Modifier.size(8.dp))
                                    date = newDate
                                }
                            }*/
                            Rezervacija(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                rezervacija = rezervacija,
                                onClick = {
                                    if(!rezervacija.placeno) {
                                        val amount = Usluge[rezervacija.services] ?: 600.0
                                        paymentManager.requestPayment(
                                            rezervacija.id ?: "",
                                            amount,
                                            rezervacija.worker
                                        )
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                    })
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            onClick = {
                navController.navigate(Screen.Rezervisi.route)
            }) {
            Icon(Icons.Filled.Add,"add reservation")
        }
    }

}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun RezervacijePreview(){
    Rezervacije(navController = NavController(LocalContext.current))
}