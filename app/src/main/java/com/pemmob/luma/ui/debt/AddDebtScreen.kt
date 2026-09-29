package com.pemmob.luma.ui.debt

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.pemmob.luma.ui.theme.SakuInputBackground
import com.pemmob.luma.ui.theme.SakuTextDark
import com.pemmob.luma.ui.theme.SakuTextMuted
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtScreen(
    onNavigateBack: () -> Unit,
    viewModel: DebtViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val addDebtUiState by viewModel.addDebtUiState.collectAsStateWithLifecycle()

    var personName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("DEBT") } // "DEBT" atau "RECEIVABLE"
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("id-ID"))
    val calendar = Calendar.getInstance().apply { timeInMillis = date }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            date = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Handle success state
    LaunchedEffect(addDebtUiState) {
        if (addDebtUiState is AddDebtUiState.Success) {
            viewModel.resetAddDebtState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tambah Utang/Piutang",
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

            // Tipe selector
            Text(
                text = "Tipe",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = SakuTextDark
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DebtTypeButton(
                    label = "Utang",
                    subtitle = "Saya berutang kepada orang lain",
                    selected = selectedType == "DEBT",
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = { selectedType = "DEBT" },
                    modifier = Modifier.weight(1f)
                )
                DebtTypeButton(
                    label = "Piutang",
                    subtitle = "Orang lain berutang kepada saya",
                    selected = selectedType == "RECEIVABLE",
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { selectedType = "RECEIVABLE" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Nama orang
            OutlinedTextField(
                value = personName,
                onValueChange = {
                    personName = it
                    nameError = null
                },
                label = { Text("Nama") },
                placeholder = { Text("Contoh: Fahri", color = SakuTextMuted) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = outlinedFieldColors()
            )

            // Nominal
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { c -> c.isDigit() }
                    amountError = null
                },
                label = { Text("Nominal (Rp)") },
                placeholder = { Text("Contoh: 50000", color = SakuTextMuted) },
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = outlinedFieldColors()
            )

            // Deskripsi
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Catatan (Opsional)") },
                placeholder = { Text("Contoh: Pinjam uang untuk tugas", color = SakuTextMuted) },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = outlinedFieldColors()
            )

            // Tanggal
            OutlinedTextField(
                value = dateFormat.format(Date(date)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal") },
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

            Spacer(modifier = Modifier.height(8.dp))

            // Error message dari state
            if (addDebtUiState is AddDebtUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (addDebtUiState as AddDebtUiState.Error).message,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp
                    )
                }
            }

            // Submit button
            Button(
                onClick = {
                    var hasError = false
                    if (personName.trim().isBlank()) {
                        nameError = "Nama tidak boleh kosong."
                        hasError = true
                    }
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount <= 0L) {
                        amountError = "Nominal harus lebih dari 0."
                        hasError = true
                    }
                    if (!hasError) {
                        viewModel.addDebt(
                            personName = personName.trim(),
                            type = selectedType,
                            amount = amount,
                            description = description.trim(),
                            date = date
                        )
                    }
                },
                enabled = addDebtUiState !is AddDebtUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (addDebtUiState is AddDebtUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        "Simpan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DebtTypeButton(
    label: String,
    subtitle: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) color.copy(alpha = 0.12f) else SakuCardBackground
    val borderColor = if (selected) color else Color(0xFFE2E8F0)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (selected) color else SakuTextDark
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = SakuTextMuted,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SakuInputBackground,
    unfocusedContainerColor = SakuInputBackground,
    disabledContainerColor = SakuInputBackground,
    focusedBorderColor = Color(0xFF4338CA),
    unfocusedBorderColor = Color(0xFFCBD5E1)
)
