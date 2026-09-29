package com.pemmob.luma.ui.splitbill

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pemmob.luma.ui.debt.outlinedFieldColors
import com.pemmob.luma.ui.theme.SakuCanvasBackground
import com.pemmob.luma.ui.theme.SakuCardBackground
import com.pemmob.luma.ui.theme.SakuInputBackground
import com.pemmob.luma.ui.theme.SakuTextDark
import com.pemmob.luma.ui.theme.SakuTextMuted
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillScreen(
    onNavigateBack: () -> Unit,
    onSplitBillCreated: (String) -> Unit,
    viewModel: SplitBillViewModel = hiltViewModel()
) {
    val createUiState by viewModel.createUiState.collectAsStateWithLifecycle()
    val validationError by viewModel.validationError.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    val participants by viewModel.participants.collectAsStateWithLifecycle()
    val payerName by viewModel.payerName.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    // Step state (1 = setup, 2 = payer, 3 = preview, 4 = result)
    var currentStep by remember { mutableIntStateOf(1) }

    // Observasi navigasi sukses
    LaunchedEffect(createUiState) {
        if (createUiState is SplitBillCreateUiState.Success) {
            onSplitBillCreated((createUiState as SplitBillCreateUiState.Success).splitBillId)
            viewModel.resetCreateState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Buat Split Bill",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SakuTextDark
                        )
                        Text(
                            "Langkah $currentStep dari 4",
                            fontSize = 12.sp,
                            color = SakuTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onNavigateBack()
                    }) {
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
        ) {
            // Step indicator
            StepIndicator(currentStep = currentStep, totalSteps = 4)

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "stepContent"
            ) { step ->
                when (step) {
                    1 -> StepOneSetup(
                        title = title,
                        totalAmount = totalAmount,
                        participants = participants,
                        selectedDate = selectedDate,
                        validationError = validationError,
                        onTitleChange = { viewModel.setTitle(it) },
                        onAmountChange = { viewModel.setTotalAmount(it) },
                        onAddParticipant = { viewModel.addParticipant(it) },
                        onRemoveParticipant = { viewModel.removeParticipant(it) },
                        onDateChange = { viewModel.setDate(it) },
                        onClearError = { viewModel.clearValidationError() },
                        onNext = {
                            // Validasi dasar sebelum step 2
                            if (title.isBlank()) {
                                viewModel.setTitle(title) // trigger recompose dengan error via calculate
                            }
                            if (title.isNotBlank() && totalAmount > 0 && participants.size >= 2) {
                                currentStep = 2
                                viewModel.clearValidationError()
                            } else {
                                viewModel.calculate() // akan trigger validation error
                            }
                        }
                    )
                    2 -> StepTwoPayer(
                        participants = participants,
                        payerName = payerName,
                        onSelectPayer = { viewModel.setPayerName(it) },
                        onNext = {
                            if (payerName.isNotBlank()) {
                                viewModel.calculate()
                                currentStep = 3
                            }
                        }
                    )
                    3 -> StepThreePreview(
                        createUiState = createUiState,
                        validationError = validationError,
                        onNext = { currentStep = 4 },
                        onBack = { currentStep = 2 }
                    )
                    4 -> StepFourResult(
                        createUiState = createUiState,
                        onSave = { viewModel.saveSplitBill() },
                        onBack = { currentStep = 3 }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val stepNum = index + 1
            val isCompleted = stepNum < currentStep
            val isCurrent = stepNum == currentStep
            val color = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                isCurrent -> MaterialTheme.colorScheme.primary
                else -> Color(0xFFE2E8F0)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

// ===== STEP 1: SETUP =====

@Composable
private fun StepOneSetup(
    title: String,
    totalAmount: Long,
    participants: List<String>,
    selectedDate: Long,
    validationError: String?,
    onTitleChange: (String) -> Unit,
    onAmountChange: (Long) -> Unit,
    onAddParticipant: (String) -> Unit,
    onRemoveParticipant: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onClearError: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val fmt = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("id-ID"))
    var amountText by remember { mutableStateOf(if (totalAmount > 0) totalAmount.toString() else "") }
    var newParticipant by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            onDateChange(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Detail Tagihan",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = SakuTextDark
        )

        // Judul
        OutlinedTextField(
            value = title,
            onValueChange = { onTitleChange(it); onClearError() },
            label = { Text("Nama / Judul") },
            placeholder = { Text("Contoh: Makan Bareng", color = SakuTextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = outlinedFieldColors()
        )

        // Total
        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it.filter { c -> c.isDigit() }
                onAmountChange(amountText.toLongOrNull() ?: 0L)
                onClearError()
            },
            label = { Text("Total Tagihan (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = outlinedFieldColors()
        )

        // Tanggal
        OutlinedTextField(
            value = dateFormat.format(Date(selectedDate)),
            onValueChange = {},
            readOnly = true,
            label = { Text("Tanggal") },
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = outlinedFieldColors()
        )

        HorizontalDivider(color = Color(0xFFF1F5F9))

        Text(
            "Peserta (${participants.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = SakuTextDark
        )

        // Add participant
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newParticipant,
                onValueChange = { newParticipant = it; onClearError() },
                placeholder = { Text("Nama peserta", color = SakuTextMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = outlinedFieldColors()
            )
            IconButton(
                onClick = {
                    if (newParticipant.isNotBlank()) {
                        onAddParticipant(newParticipant.trim())
                        newParticipant = ""
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
            }
        }

        // Validation error
        if (validationError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = validationError,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Participant chips
        if (participants.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SakuCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    participants.forEach { name ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = SakuTextDark
                                )
                            }
                            IconButton(onClick = { onRemoveParticipant(name) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Hapus",
                                    tint = SakuTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Lanjut: Pilih Payer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ===== STEP 2: PILIH PAYER =====

@Composable
private fun StepTwoPayer(
    participants: List<String>,
    payerName: String,
    onSelectPayer: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Siapa yang membayar tagihan terlebih dahulu?",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = SakuTextDark,
            lineHeight = 22.sp
        )
        Text(
            "Payer adalah orang yang membayar total tagihan di awal.",
            fontSize = 13.sp,
            color = SakuTextMuted,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        participants.forEach { name ->
            val isSelected = name == payerName
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else SakuCardBackground
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    2.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPayer(name) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else SakuInputBackground
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isSelected) Color.White else SakuTextMuted
                            )
                        }
                        Text(
                            text = name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else SakuTextDark
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = payerName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Lanjut: Lihat Perhitungan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ===== STEP 3: PREVIEW KALKULASI =====

@Composable
private fun StepThreePreview(
    createUiState: SplitBillCreateUiState,
    validationError: String?,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val fmt = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    if (createUiState !is SplitBillCreateUiState.Calculated) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val calc = createUiState.calculation

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Pembagian Biaya", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SakuTextDark)
            Spacer(modifier = Modifier.height(4.dp))

            // Summary card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(calc.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SakuTextDark)
                    Text(
                        "Total: ${fmt.format(calc.totalAmount)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Dibayar oleh: ${calc.payerName}",
                        fontSize = 13.sp,
                        color = SakuTextMuted
                    )
                }
            }
        }

        items(calc.participants) { participant ->
            val isPayer = participant.isPayer
            Card(
                colors = CardDefaults.cardColors(containerColor = SakuCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPayer) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = participant.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isPayer) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                text = participant.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = SakuTextDark
                            )
                            Text(
                                text = if (isPayer) "Membayar di awal" else "Belum membayar",
                                fontSize = 12.sp,
                                color = SakuTextMuted
                            )
                        }
                    }
                    Text(
                        text = fmt.format(participant.shareAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isPayer) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Lanjut: Lihat Hasil", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ===== STEP 4: RESULT =====

@Composable
private fun StepFourResult(
    createUiState: SplitBillCreateUiState,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val fmt = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    if (createUiState !is SplitBillCreateUiState.Calculated) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val calc = createUiState.calculation
    val totalReceivable = calc.settlementItems.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Siapa Bayar Siapa?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SakuTextDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Berikut adalah kewajiban masing-masing peserta.",
                fontSize = 13.sp,
                color = SakuTextMuted
            )
        }

        // Total receivable untuk payer
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Piutang ${calc.payerName}", fontSize = 13.sp, color = SakuTextMuted)
                        Text(
                            text = fmt.format(totalReceivable),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(
                        text = "+${calc.settlementItems.size} tagihan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        items(calc.settlementItems) { settlement ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SakuCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // From avatar
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = settlement.from.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = settlement.from,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = SakuTextDark
                                )
                                Text(text = "bayar ke", fontSize = 12.sp, color = SakuTextMuted)
                                Text(
                                    text = settlement.to,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    "Belum Lunas",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = fmt.format(settlement.amount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // Error state
        if (createUiState.error != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = createUiState.error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = onSave,
                enabled = !createUiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (createUiState.isSaving) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Simpan & Buat Tagihan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
