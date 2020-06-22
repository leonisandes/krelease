package io.github.leonisandes.krelease.github

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import io.github.leonisandes.krelease.Constants
import io.github.leonisandes.krelease.Constants.HTTP_UNPROCESSABLE_ENTITY
import io.github.leonisandes.krelease.extensions.responseJson
import io.github.leonisandes.krelease.github.dto.CreateReleaseDTO
import io.github.leonisandes.krelease.github.dto.ReleaseDTO
import io.github.leonisandes.krelease.github.dto.UpdateReleaseDTO
import io.github.leonisandes.krelease.github.exceptions.GithubException
import io.github.leonisandes.krelease.github.exceptions.ReleaseAlreadyExistException
import io.github.leonisandes.krelease.github.exceptions.ReleaseNotFoundException
import io.github.leonisandes.krelease.github.exceptions.RepositoryNotFoundException
import io.github.leonisandes.krelease.providers.ObjectMapperProvider
import java.io.File
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_OK
import java.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class GithubClient(
    private val baseUrl: String = "https://api.github.com",
    private val accessToken: String,
    private val fuelManager: FuelManager
) {
    private val scope by lazy { CoroutineScope(Dispatchers.IO) }
    private val mapper = ObjectMapperProvider.provide()

    init {
        fuelManagerConfiguration()
    }

    private fun fuelManagerConfiguration() {
        val tokenEncoded = Base64.getEncoder().encodeToString(accessToken.toByteArray())

        fuelManager.addRequestInterceptor { next: (Request) -> Request ->
            { request: Request ->
                request.header(Constants.AUTHORIZATION_HEADER, "Basic $tokenEncoded")
                next(request)
            }
        }
    }

    fun validRepositoryOrThrow(
        owner: String,
        repository: String
    ) {
        val url = "$baseUrl/repos/$owner/$repository"
        val (statusCode, json) = fuelManager
            .get(url)
            .responseJson()

        when (statusCode) {
            HTTP_OK -> return
            HTTP_NOT_FOUND -> throw RepositoryNotFoundException(owner = owner, repository = repository)
            else -> throw GithubException(statusCode, json)
        }
    }

    fun getRelease(
        owner: String,
        repository: String,
        tag: String
    ): ReleaseDTO {
        val url = "$baseUrl/repos/$owner/$repository/releases/tags/$tag"
        val (statusCode, json) = fuelManager
            .get(url)
            .responseJson()

        when (statusCode) {
            HTTP_OK -> return mapper.readValue(json)
            HTTP_NOT_FOUND -> throw ReleaseNotFoundException(search = "tag", value = tag)
            else -> throw GithubException(statusCode, json)
        }
    }

    fun createRelease(
        createReleaseDTO: CreateReleaseDTO
    ): ReleaseDTO {
        val url = "$baseUrl/repos/${createReleaseDTO.owner}/${createReleaseDTO.repository}/releases"
        val body = mapper.writeValueAsString(createReleaseDTO)
        val (statusCode, json) = fuelManager
            .post(url)
            .body(body = body)
            .responseJson()

        when (statusCode) {
            HTTP_CREATED -> return mapper.readValue(json)
            HTTP_UNPROCESSABLE_ENTITY -> throw ReleaseAlreadyExistException(createReleaseDTO.tag)
            else -> throw GithubException(statusCode, json)
        }
    }

    fun updateRelease(
        updateReleaseDTO: UpdateReleaseDTO
    ): ReleaseDTO {
        val url = "$baseUrl/repos/${updateReleaseDTO.owner}/${updateReleaseDTO.repository}/releases/" +
            updateReleaseDTO.id
        val body = mapper.writeValueAsString(updateReleaseDTO)
        val (statusCode, json) = fuelManager
            .patch(url)
            .body(body = body)
            .responseJson()

        when (statusCode) {
            HTTP_OK -> return mapper.readValue(json)
            HTTP_NOT_FOUND -> throw ReleaseNotFoundException(search = "id", value = updateReleaseDTO.id)
            else -> throw GithubException(statusCode, json)
        }
    }

    private fun uploadAssetOnRelease(
        file: File,
        releaseId: String,
        owner: String,
        repository: String
    ) {
        val url = "$baseUrl/repos/$owner/$repository/releases/$releaseId/" +
                "assets?name=${file.name}&label=${file.nameWithoutExtension}"

        val (statusCode, json) = fuelManager
            .post(url)
            .responseJson()

        when (statusCode) {
            HTTP_CREATED -> return
            else -> throw GithubException(statusCode, json)
        }
    }

    fun asyncUploadAssetsOnRelease(
        owner: String,
        repository: String,
        path: File,
        releaseId: String
    ) {
        path.listFiles()?.forEach {
            scope.async {
                println("\t[+] Uploading ${it.name}")
                uploadAssetOnRelease(
                    file = it,
                    releaseId = releaseId,
                    owner = owner,
                    repository = repository
                )
            }
        }
    }
}
