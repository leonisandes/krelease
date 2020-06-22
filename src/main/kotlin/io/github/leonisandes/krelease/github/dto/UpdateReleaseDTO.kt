package io.github.leonisandes.krelease.github.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateReleaseDTO(
    @JsonIgnore
    val owner: String,

    @JsonIgnore
    val repository: String,

    @JsonIgnore
    val id: String,

    @JsonProperty("tag_name")
    val tag: String,

    @JsonProperty("target_commitish")
    val commit: String? = null,

    @JsonProperty("name")
    val name: String? = null,

    @JsonProperty("body")
    val body: String? = null,

    @JsonProperty("draft")
    val draft: Boolean = false,

    @JsonProperty("prerelease")
    val preRelease: Boolean = false
) {
    companion object {
        fun from(
            owner: String,
            repository: String,
            id: String,
            commit: String?,
            title: String?,
            body: String?,
            draft: Boolean,
            prerelease: Boolean,
            tag: String
        ) = UpdateReleaseDTO(
            owner = owner,
            repository = repository,
            id = id,
            commit = commit,
            name = title,
            body = body,
            draft = draft,
            preRelease = prerelease,
            tag = tag
        )
    }
}
