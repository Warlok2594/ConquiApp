package com.conqui.app.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Profile(
    @SerialName("user_id") val userId: String = "",
    val email: String? = null,
    val username: String? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class House(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class HouseMember(
    val id: String = "",
    @SerialName("house_id") val houseId: String = "",
    @SerialName("user_id") val userId: String = "",
    val role: String = "member", // "admin" o "member"
    @SerialName("joined_at") val joinedAt: Long = System.currentTimeMillis()
)

@Serializable
data class Turno(
    val id: String = "",
    @SerialName("house_id") val houseId: String = "",
    val nome: String = "",
    val descrizione: String? = null,
    val frequenza: String = "settimanale", // "giornaliero", "settimanale", "mensile"
    @SerialName("giorni_settimana") val giorniSettimana: List<Int>? = null,
    @SerialName("giorno_mese") val giornoMese: Int? = null,
    val attivo: Boolean = true,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class TurnoRotazione(
    val id: String = "",
    @SerialName("turno_id") val turnoId: String = "",
    @SerialName("user_id") val userId: String = "",
    val ordine: Int = 0,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class TurnoAssegnazione(
    val id: String = "",
    @SerialName("turno_id") val turnoId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("data_assegnazione") val dataAssegnazione: Long = System.currentTimeMillis(),
    val completato: Boolean = false,
    @SerialName("data_completamento") val dataCompletamento: Long? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class TurnoStorico(
    val id: String = "",
    @SerialName("turno_id") val turnoId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("data_turno") val dataTurno: Long = System.currentTimeMillis(),
    val completato: Boolean = false,
    val note: String? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Spesa(
    val id: String = "",
    @SerialName("house_id") val houseId: String = "",
    val descrizione: String = "",
    val importo: Double = 0.0,
    @SerialName("pagata_da") val pagataDa: String = "",
    @SerialName("divisa_tra") val divisaTra: List<String> = emptyList(),
    val categoria: String? = null,
    @SerialName("data_spesa") val dataSpesa: Long = System.currentTimeMillis(),
    val ricorrente: Boolean = false,
    @SerialName("foto_scontrino") val fotoScontrino: String? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Pagamento(
    val id: String = "",
    @SerialName("spesa_id") val spesaId: String = "",
    @SerialName("da_utente") val daUtente: String = "",
    @SerialName("a_utente") val aUtente: String = "",
    val importo: Double = 0.0,
    val pagato: Boolean = false,
    @SerialName("data_pagamento") val dataPagamento: Long? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)