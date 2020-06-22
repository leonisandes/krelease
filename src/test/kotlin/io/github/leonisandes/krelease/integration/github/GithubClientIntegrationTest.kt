package io.github.leonisandes.krelease.integration.github

import com.github.kittinunf.fuel.core.FuelManager
import io.github.leonisandes.krelease.github.GithubClient
import io.github.leonisandes.krelease.github.dto.CreateReleaseDTO
import io.github.leonisandes.krelease.integration.IntegrationTestExtension
import java.io.File
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(IntegrationTestExtension::class)
class GithubClientIntegrationTest {

    private val fuelManager = FuelManager()
    private val accessToken = "token123"
    private val githubClient = GithubClient(
        baseUrl = "http://localhost:8090",
        fuelManager = fuelManager,
        accessToken = accessToken
    )
    private val owner = "owner"
    private val repository = "repository"
    private val tag = "v0.0.1"

    @Nested
    @DisplayName("When validating a repository")
    inner class ValidateRepository {

        @Test
        @DisplayName("given a valid SearchRepositoryDTO should validate it")
        fun `given a valid SearchRepositoryDTO should validate it`() {

            assertThatCode { githubClient.validRepositoryOrThrow(owner = owner, repository = repository) }
                .doesNotThrowAnyException()
        }
    }

    @Nested
    @DisplayName("When creating a release")
    inner class CreateRelease {

        @Test
        @DisplayName("given a valid createReleaseDTO should create a release")
        fun `given a valid createReleaseDTO should create a release`() {

            val createReleaseDTO = CreateReleaseDTO(
                owner = owner,
                repository = repository,
                tag = tag
            )
            assertThatCode { githubClient.createRelease(createReleaseDTO = createReleaseDTO) }
                .doesNotThrowAnyException()
        }
    }

    @Nested
    @DisplayName("When getting a release")
    inner class GetRelease {

        @Test
        @DisplayName("given a valid owner, repository and tag should get a release")
        fun `given a valid owner, repository and tag should get a release`() {
            assertThatCode { githubClient.getRelease(owner = owner, repository = repository, tag = tag) }
                .doesNotThrowAnyException()
        }
    }

    @Nested
    @DisplayName("When uploading an asset")
    inner class UploadAsset {

        @Test
        @DisplayName("given a valid asset should upload it on release")
        fun `given a valid asset should upload it`() {
            val path = File.createTempFile("integration", "test", File("/tmp"))
            val releaseId = "1"

            assertThatCode {
                githubClient.asyncUploadAssetsOnRelease(
                    path = path,
                    releaseId = releaseId,
                    owner = owner,
                    repository = repository
                )
            }.doesNotThrowAnyException()

            path.deleteOnExit()
        }
    }
}
