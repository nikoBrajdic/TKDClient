package hr.brajdic.tkd.tkdclient

import com.fasterxml.jackson.annotation.JsonProperty

data class Settings(
    @JsonProperty("ip")
    val ip: String
)