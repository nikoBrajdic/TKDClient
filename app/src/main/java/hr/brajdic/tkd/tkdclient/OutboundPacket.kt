package hr.brajdic.tkd.tkdclient

//import com.beust.klaxon.Json
//import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

//import com.google.gson.GsonBuilder
//import com.google.gson.annotations.Expose
//import com.google.gson.annotations.SerializedName

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class OutboundPacket(
    //@Json(name = "Type")
    //@Expose @SerializedName("Type")
	@JsonProperty("Type")
    val type: String,

    //@Json(name = "Mac")
    //@Expose @SerializedName("Mac")
	@JsonProperty("Mac")
    val mac: String? = null,

    //@Json(name = "Id")
    //@Expose @SerializedName("Id")
	@JsonProperty("Id")
    val id: Int? = null,

    //@Json(name = "Battery")
    //@Expose @SerializedName("Battery")
	@JsonProperty("Battery")
    val battery: Int? = null,

    //@Json(name = "Scores")
    //@Expose @SerializedName("Scores")
	@JsonProperty("Scores")
    val scores: Scores? = null
) {
    companion object {
        @JvmStatic
        fun instructions(type: String, mac: String? = null,
                         id: Int? = null, battery: Int? = null,
                         scores: Scores? = null): String =
//            Klaxon().toJsonString(
//            GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(
            ObjectMapper().registerModule(KotlinModule()).writeValueAsString(
                OutboundPacket(type = type,
                               id = id,
                               mac = mac,
                               battery = battery,
                               scores = scores))
    }
}