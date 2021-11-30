package hr.brajdic.tkd.tkdclient

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class OutboundPacket(
	@JsonProperty("Type")
    val type: String,

	@JsonProperty("Mac")
    val mac: String? = null,

	@JsonProperty("Id")
    val id: Int? = null,

	@JsonProperty("Battery")
    val battery: Int? = null,

	@JsonProperty("Scores")
    val scores: Scores? = null
) {
    companion object {
        @JvmStatic
        fun instructions(type: String, mac: String? = null,
                         id: Int? = null, battery: Int? = null,
                         scores: Scores? = null): String =
            ObjectMapper().registerModule(KotlinModule()).writeValueAsString(
                OutboundPacket(type = type,
                               id = id,
                               mac = mac,
                               battery = battery,
                               scores = scores))
    }
}