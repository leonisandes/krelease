package io.github.leonisandes.krelease.github.dto

import com.fasterxml.jackson.annotation.JsonProperty

class ReleaseDTO(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("url")
    val url: String
)
