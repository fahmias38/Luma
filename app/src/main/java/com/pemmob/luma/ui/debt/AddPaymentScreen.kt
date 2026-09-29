package com.pemmob.luma.ui.debt

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pemmob.luma.ui.theme.SakuCanvasBackground
import com.pemmob.luma.ui.theme.SakuCardBackground
import com.pemmob.luma.ui.theme.SakuTextDark
import com.pemmob.luma.ui.theme.SakuTextMuted
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    debtId: String,
    onNavigateBack: () -> Unit,
    viewModel: DebtViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val addPaymentUiState by viewModel.addPaymentUiState.collectAsStateWithLifecycle()
    val detailUiState by viewModel.detailUiState.collectAsStateWithLifecycle()

    // Ambil remaining amount dari detail state
    val remainingAmount = remember(detailUiState) {
        if (detailUiState is DebtDetailUiState.Success) {
            (detailUiState as DebtDetailUiState.Success).remainingAmount
        } else 0L
    }

    val fmt = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var amountError by remember { mutableStateOf<String?>(null) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("id-ID"))
    val calendar = Calendar.getInstance().apply { timeInMillis = paymentDate }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            paymentDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(addPaymentUiState) {
        if (addPaymentUiState is AddPaymentUiState.Success) {
            viewModel.resetPaymentState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Catat Pembayaran",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SakuTextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SakuCanvasBackground
                )
            )
        },
        containerColor = SakuCanvasBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Info sisa kewajiban
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sisa Kewajiban", fontSize = 13.sp, color = SakuTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = fmt.format(remainingAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Input nominal
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { c -> c.isDigit() }
                    amountError = null
                },
                label = { Text("Nominal Pembayaran (Rp)") },
                placeholder = { Text("Maksimal ${fmt.format(remainingAmount)}", color = SakuTextMuted) },
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = outlinedFieldColors()
            )

            // Tanggal pembayaran
            OutlinedTextField(
                value = dateFormat.format(Date(paymentDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal Pembayaran") },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Pilih Tanggal",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = outlinedFieldColors()
            )

            // Catatan
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan (Opsional)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = outlinedFieldColors()
            )

            // Error message dari state
            if (addPaymentUiState is AddPaymentUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (addPaymentUiState as AddPaymentUiState.Error).message,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    when {
                        amount <= 0L -> {
                            amountError = "Nominal harus lebih dari 0."
                        }
                        amount > remainingAmount -> {
                            amountError = "Nominal melebihi sisa kewajiban (${fmt.format(remainingAmount)})."
                        }
                        else -> {
                            viewModel.addPayment(
                                debtId = debtId,
                                amount = amount,
                                paymentDate = paymentDate,
                                note = note.trim()
                            )
                        }
                    }
                },
                enabled = addPaymentUiState !is AddPaymentUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (addPaymentUiState is AddPaymentUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Simpan Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
