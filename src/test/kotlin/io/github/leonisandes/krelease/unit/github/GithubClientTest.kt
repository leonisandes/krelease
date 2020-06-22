package io.github.leonisandes.krelease.unit.github

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import io.github.leonisandes.krelease.Constants.HTTP_UNPROCESSABLE_ENTITY
import io.github.leonisandes.krelease.github.GithubClient
import io.github.leonisandes.krelease.github.dto.CreateReleaseDTO
import io.github.leonisandes.krelease.github.dto.UpdateReleaseDTO
import io.github.leonisandes.krelease.github.exceptions.GithubException
import io.github.leonisandes.krelease.github.exceptions.ReleaseAlreadyExistException
import io.github.leonisandes.krelease.github.exceptions.ReleaseNotFoundException
import io.github.leonisandes.krelease.github.exceptions.RepositoryNotFoundException
import io.github.leonisandes.krelease.objectmother.ReleaseDTOObjectMother
import io.github.leonisandes.krelease.providers.ObjectMapperProvider
import io.github.leonisandes.krelease.utils.request
import io.github.leonisandes.krelease.utils.response
import io.mockk.every
import io.mockk.spyk
import java.io.File
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_OK
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GithubClientTest {

    private val fuelManager = spyk<FuelManager>()
    private val baseUrl = "http://localhost:9090"
    private val accessToken = "accessToken"
    private val githubClient = GithubClient(baseUrl = baseUrl, accessToken = accessToken, fuelManager = fuelManager)
    private val mapper = ObjectMapperProvider.provide()

    private val owner = "owner"
    private val repository = "repository"
    private val tag = "v1.0.0"

    private val invalidOwner = "invalidOwner"
    private val invalidRepository = "invalidRepository"

    @Nested
    @DisplayName("When validating a repository")
    inner class ValidateRepository {
        private val url = "$baseUrl/repos/${owner}/${repository}"
        private val invalidUrl = "$baseUrl/repos/${invalidOwner}/${invalidRepository}"

        @Test
        @DisplayName("given a valid owner and repository should validate")
        fun `given a valid owner and repository should validate`() {
            val request = spyk(request(Method.GET, url))
            val response = response(url, HTTP_OK)

            every { fuelManager.get(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatCode { githubClient.validRepositoryOrThrow(owner = owner, repository = repository) }
                .doesNotThrowAnyException()
        }

        @Test
        @DisplayName("should throw GithubException when a not expected status code was returned")
        fun `should throw GithubException when a not expected status code was returned`() {
            val request = spyk(request(Method.GET, url))
            val response = response(url, HTTP_INTERNAL_ERROR, "ERROR")

            every { fuelManager.get(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatExceptionOfType(GithubException::class.java)
                .isThrownBy { githubClient.validRepositoryOrThrow(owner = owner, repository = repository) }
                .matches { it.message == "Github returned statusCode 500 with body ERROR" }
        }

        @Test
        @DisplayName("given an invalid owner and repository should throw RepositoryNotFoundException")
        fun `given an invalid owner and repository should throw RepositoryNotFoundException`() {
            val request = spyk(request(Method.GET, invalidUrl))
            val response = response(url, HTTP_NOT_FOUND)

            every { fuelManager.get(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatExceptionOfType(RepositoryNotFoundException::class.java)
                .isThrownBy {
                    githubClient.validRepositoryOrThrow(owner = invalidOwner, repository = invalidRepository)
                }.matches { it.message == "Repository invalidOwner/invalidRepository not found." }
        }
    }


    @Nested
    @DisplayName("When getting a release")
    inner class GetRelease {
        private val url = "$baseUrl/repos/${owner}/${repository}/releases/tags/$tag"
        private val invalidUrl = "$baseUrl/repos/${invalidOwner}/${invalidRepository}/releases/tags/$tag"

        @Test
        @DisplayName("given a valid owner, repository and tag should return a release")
        fun `given a valid owner, repository and tag should return a release`() {
            val request = spyk(request(Method.GET, url))
            val releaseDTOResponse = mapper.writeValueAsString(ReleaseDTOObjectMother.new())
            val response = response(url, HTTP_OK, releaseDTOResponse)

            every { fuelManager.get(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatCode { githubClient.getRelease(owner = owner, repository = repository, tag = tag) }
                .doesNotThrowAnyException()
        }

        @Test
        @DisplayName("should throw GithubException when a not expected status code was returned")
        fun `should throw GithubException when a not expected status code was returned`() {
            val request = spyk(request(Method.GET, url))
            val response = response(url, HTTP_INTERNAL_ERROR, "ERROR")

            every { fuelManager.get(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatExceptionOfType(GithubException::class.java)
                .isThrownBy { githubClient.getRelease(owner = owner, repository = repository, tag = tag) }
                .matches { it.message == "Github returned statusCode 500 with body ERROR" }
        }

        @Test
        @DisplayName("given an invalid owner, repository and tag should throw RepositoryNotFoundException")
        fun `given an invalid owner, repository and tag should throw RepositoryNotFoundException`() {
            val request = spyk(request(Method.GET, invalidUrl))
            val response = response(url, HTTP_NOT_FOUND)

            every { fuelManager.get(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatExceptionOfType(ReleaseNotFoundException::class.java)
                .isThrownBy { githubClient.getRelease(owner = owner, repository = repository, tag = tag) }
                .matches { it.message == "It was not possible to find a release by tag ${tag}." }
        }
    }

    @Nested
    @DisplayName("When creating a release")
    inner class CreateRelease {
        private val createReleaseDTO = CreateReleaseDTO(
            owner = "owner",
            repository = "repository",
            tag = "v0.0.1"
        )
        private val url = "$baseUrl/repos/${createReleaseDTO.owner}/${createReleaseDTO.repository}/releases"

        @Test
        @DisplayName("given a valid createReleaseDTO should create a release")
        fun `given a valid createReleaseDTO should create a release`() {
            val request = spyk(request(Method.POST, url))
            val releaseDTOResponse = mapper.writeValueAsString(ReleaseDTOObjectMother.new())
            val response = response(url, HTTP_CREATED, releaseDTOResponse)

            every { fuelManager.post(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatCode { githubClient.createRelease(createReleaseDTO = createReleaseDTO) }
                .doesNotThrowAnyException()
        }

        @Test
        @DisplayName("should throw GithubException when a not expected status code was returned")
        fun `should throw GithubException when a not expected status code was returned`() {
            val request = spyk(request(Method.POST, url))
            val response = response(url, HTTP_INTERNAL_ERROR, "ERROR")

            every { fuelManager.post(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatExceptionOfType(GithubException::class.java)
                .isThrownBy { githubClient.createRelease(createReleaseDTO = createReleaseDTO) }
                .matches { it.message == "Github returned statusCode 500 with body ERROR" }
        }

        @Test
        @DisplayName("given a tag already released in createReleaseDTO should throw ReleaseAlreadyExistException")
        fun `given a tag already released in createReleaseDTO should throw ReleaseAlreadyExistException`() {
            val request = spyk(request(Method.POST, url))
            val response = response(url, HTTP_UNPROCESSABLE_ENTITY)

            every { fuelManager.post(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatExceptionOfType(ReleaseAlreadyExistException::class.java)
                .isThrownBy { githubClient.createRelease(createReleaseDTO = createReleaseDTO) }
                .matches { it.message == "Release v0.0.1 already exist." }
        }
    }

    @Nested
    @DisplayName("When updating a release")
    inner class UpdateRelease {
        private val updateReleaseDTO = UpdateReleaseDTO(
            owner = "owner",
            repository = "repository",
            tag = "v0.0.1",
            id = "1"
        )
        private val url = "$baseUrl/repos/${updateReleaseDTO.owner}/${updateReleaseDTO.repository}/releases/" +
            updateReleaseDTO.id

        @Test
        @DisplayName("given a valid x should update it")
        fun `given a valid updateReleaseDTO should update it`() {
            val request = spyk(request(Method.PATCH, url))
            val releaseDTOResponse = mapper.writeValueAsString(ReleaseDTOObjectMother.new())
            val response = response(url, HTTP_OK, releaseDTOResponse)

            every { fuelManager.patch(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatCode { githubClient.updateRelease(updateReleaseDTO = updateReleaseDTO) }
                .doesNotThrowAnyException()
        }

        @Test
        @DisplayName("should throw GithubException when a not expected status code was returned")
        fun `should throw GithubException when a not expected status code was returned`() {
            val request = spyk(request(Method.PATCH, url))
            val response = response(url, HTTP_INTERNAL_ERROR, "ERROR")

            every { fuelManager.patch(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatExceptionOfType(GithubException::class.java)
                .isThrownBy { githubClient.updateRelease(updateReleaseDTO = updateReleaseDTO) }
                .matches { it.message == "Github returned statusCode 500 with body ERROR" }
        }

        @Test
        @DisplayName("given a invalid release in updateRelease should throw ReleaseNotFoundException")
        fun `given a invalid release in updateRelease should throw ReleaseNotFoundException`() {
            val request = spyk(request(Method.PATCH, url))
            val response = response(url, HTTP_NOT_FOUND)

            every { fuelManager.patch(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatExceptionOfType(ReleaseNotFoundException::class.java)
                .isThrownBy { githubClient.updateRelease(updateReleaseDTO = updateReleaseDTO) }
                .matches { it.message == "It was not possible to find a release by id ${updateReleaseDTO.id}." }
        }
    }

    @Nested
    @DisplayName("When uploading an asset")
    inner class UploadAsset {
        private val path = File.createTempFile("integration", "test")
        private val releaseId = "1"
        private val owner = "owner"
        private val repository = "repository"
        private val url = "$baseUrl/repos/$owner/$repository/releases/$releaseId/" +
            "assets?name=${path.name}&label=${path.nameWithoutExtension}"

        @Test
        @DisplayName("given a valid asset should upload it on release")
        fun `given a valid asset should upload it`() {
            val request = spyk(request(Method.POST, url))
            val response = response(url, HTTP_CREATED)

            every { fuelManager.post(path = any(), parameters = any()) } returns request
            every { request.responseString() } returns response

            assertThatCode {
                githubClient.asyncUploadAssetsOnRelease(
                    path = path,
                    releaseId = releaseId,
                    owner = owner,
                    repository = repository
                )
            }.doesNotThrowAnyException()
        }

    }
}
