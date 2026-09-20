package com.pemmob.luma.domain.model

data class UserDomainModel(
    val id: String,
    val email: String,
    val fullName: String? = null
)
