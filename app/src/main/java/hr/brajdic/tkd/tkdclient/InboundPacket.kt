package hr.brajdic.tkd.tkdclient

import com.fasterxml.jackson.annotation.JsonProperty

//import com.beust.klaxon.Json
/*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
*/
data class InboundPacket(
    //@Json(name = "Type")
    //@Expose @SerializedName("Type")
    @JsonProperty("Type")
    val type: String,

    //@Json(name = "Id")
    //@Expose @SerializedName("Id")
	@JsonProperty("Id")
    val id: Int? = null,

    //@Json(name = "Idle")
    //@Expose @SerializedName("Idle")
	@JsonProperty("Idle")
    val idle: Boolean? = false,

    //@Json(name = "Disconnect")
    //@Expose @SerializedName("Disconnect")
	@JsonProperty("Disconnect")
    val dc: Boolean? = false,

    //@Json(name = "Off")
    //@Expose @SerializedName("Off")
	@JsonProperty("Off")
    val off: Boolean? = false,

    //@Json(name = "Battery")
    //@Expose @SerializedName("Battery")
	@JsonProperty("Battery")
    val battery: Boolean? = false,

    //@Json(name = "Scores")
    //@Expose @SerializedName("Scores")
	@JsonProperty("Scores")
    val scores: Boolean? = false,

    //@Json(name = "Vibrate")
    //@Expose @SerializedName("Vibrate")
	@JsonProperty("Vibrate")
    val vibrate: Boolean? = false,

    //@Json(name = "Message")
    //@Expose @SerializedName("Message")
	@JsonProperty("Message")
    val message: String? = "",

    //@Json(name = "SetScores")
    //@Expose @SerializedName("SetScores")
	@JsonProperty("SetScores")
    val setScores: Scores? = null
)