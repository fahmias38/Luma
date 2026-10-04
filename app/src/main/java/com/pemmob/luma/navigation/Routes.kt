package com.pemmob.luma.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object AuthGraph

@Serializable
object LoginRoute

@Serializable
object RegisterRoute

@Serializable
object MainGraph

@Serializable
object DashboardPlaceholderRoute

@Serializable
object TransactionRoute

// ===== DEBT & RECEIVABLE =====

@Serializable
object DebtRoute

@Serializable
data class DebtDetailRoute(val debtId: String)

@Serializable
object AddDebtRoute

@Serializable
data class AddPaymentRoute(val debtId: String)

// ===== SPLIT BILL =====

@Serializable
object SplitBillRoute

@Serializable
data class SplitBillSuccessRoute(val splitBillId: String)

// ===== PROFILE =====

@Serializable
object ProfileRoute

@Serializable
object StatisticsRoute

@Serializable
object NotificationRoute

