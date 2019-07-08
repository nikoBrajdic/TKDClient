package hr.brajdic.tkd.tkdclient

import com.beust.klaxon.Json

data class InboundPacket(
    @Json(name = "Type")
    val type: String,
    @Json(name = "Id")
    val id: Int? = null,
    @Json(name = "Idle")
    val idle: Boolean? = false,
    @Json(name = "Disconnect")
    val dc: Boolean? = false,
    @Json(name = "Off")
    val off: Boolean? = false,
    @Json(name = "Battery")
    val battery: Boolean? = false,
    @Json(name = "Scores")
    val scores: Boolean? = false,
    @Json(name = "Vibrate")
    val vibrate: Boolean? = false,
    @Json(name = "Message")
    val message: String? = "",
    @Json(name = "SetScores")
    val setScores: Scores? = null
)