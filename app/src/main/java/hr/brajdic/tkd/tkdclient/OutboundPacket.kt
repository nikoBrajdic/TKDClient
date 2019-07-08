package hr.brajdic.tkd.tkdclient

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon

data class OutboundPacket(
    @Json(name = "Type")
    val type: String,
    @Json(name = "Mac")
    val mac: String? = null,
    @Json(name = "Id")
    val id: Int? = null,
    @Json(name = "Battery")
    val battery: Int? = null,
    @Json(name = "Scores")
    val scores: Scores? = null
) {
    companion object {
        @JvmStatic
        fun instructions(type: String, mac: String? = null,
                         id: Int? = null, battery: Int? = null,
                         scores: Scores? = null): String = Klaxon().toJsonString(
            OutboundPacket(type = type,
                           id = id,
                           mac = mac,
                           battery = battery,
                           scores = scores))
    }
}