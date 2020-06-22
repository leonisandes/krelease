package io.github.leonisandes.krelease.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import io.github.leonisandes.krelease.Constants.AUTHORIZATION_HEADER
import io.github.leonisandes.krelease.utils.readJsonResource
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_OK

object GithubMockServer {

    private val server = WireMockServer(8090)
    private val authorization = "Basic dG9rZW4xMjM="

    fun start() {
        stubGetRepository()
        stubUpdateRelease()
        stubCreateRelease()
        stubUploadAssetOnRelease()
        stubGetRelease()

        server.start()
    }

    private fun stubGetRepository() {
        val response = readJsonResource("github/get_repository_response")

        server.stubFor(
            get(urlMatching("/repos/owner/repository"))
                .withHeader(AUTHORIZATION_HEADER, equalTo(authorization))
                .willReturn(
                    aResponse()
                        .withBody(response)
                        .withStatus(HTTP_OK)
                )
        )
    }

    private fun stubCreateRelease() {
        val request = readJsonResource("github/create_release_request")
        val response = readJsonResource("github/create_release_response")

        server.stubFor(
            post(urlMatching("/repos/owner/repository/releases"))
                .withHeader(AUTHORIZATION_HEADER, equalTo(authorization))
                .withRequestBody(equalToJson(request))
                .willReturn(
                    aResponse()
                        .withBody(response)
                        .withStatus(HTTP_CREATED)
                )
        )
    }

    private fun stubUploadAssetOnRelease() {
        val response = readJsonResource("github/upload_asset_on_release_response")

        server.stubFor(
            post(urlMatching("/repos/(.*)/(.*)/releases/(.*)/assets\\?name=(.*)&label=(.*)"))
                .withHeader(AUTHORIZATION_HEADER, equalTo(authorization))
                .willReturn(
                    aResponse()
                        .withBody(response)
                        .withStatus(HTTP_CREATED)
                )
        )
    }

    private fun stubGetRelease() {
        val response = readJsonResource("github/get_release_response")

        server.stubFor(
            get(urlMatching("/repos/owner/repository/releases/tags/(.*)"))
                .withHeader(AUTHORIZATION_HEADER, equalTo(authorization))
                .willReturn(
                    aResponse()
                        .withBody(response)
                        .withStatus(HTTP_OK)
                )
        )
    }

    private fun stubUpdateRelease() {
        val request = readJsonResource("github/update_release_request")
        val response = readJsonResource("github/update_release_response")

        server.stubFor(
            patch(urlMatching("/repos/owner/repository/releases/1"))
                .withHeader(AUTHORIZATION_HEADER, equalTo(authorization))
                .withRequestBody(equalToJson(request))
                .willReturn(
                    aResponse()
                        .withBody(response)
                        .withStatus(HTTP_OK)
                )
        )
    }

    fun stop() = server.stop()
}
