package io.github.leonisandes.krelease.unit

import io.github.leonisandes.krelease.KRelease
import io.github.leonisandes.krelease.github.GithubClient
import io.github.leonisandes.krelease.objectmother.ReleaseDTOObjectMother
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

class KReleaseTest {

    private val githubClient = mockk<GithubClient>()
    private val path = File.createTempFile("integration", "test", File("/tmp"))

    @Nested
    @DisplayName("When creating a release")
    inner class CreateRelease {

        @Test
        @DisplayName("given valid options should create a release")
        fun `given valid options should create a release`() {
            val krelease = KRelease(
                owner = "owner",
                repository = "repository",
                commit = "master",
                title = "Title",
                body = "Body",
                update = false,
                draft = false,
                preRelease = false,
                tag = "v1.0.0",
                path = path,
                githubClient = githubClient
            )

            every { githubClient.validRepositoryOrThrow(any(), any()) } just Runs
            every { githubClient.createRelease(any()) } returns ReleaseDTOObjectMother.new()
            every { githubClient.asyncUploadAssetsOnRelease(any(), any(), any(), any()) } just Runs

            krelease.execute()

            verify(exactly = 1) { githubClient.validRepositoryOrThrow(any(), any()) }
            verify(exactly = 1) { githubClient.createRelease(any()) }
            verify(exactly = 0) { githubClient.updateRelease(any()) }
            verify(exactly = 1) { githubClient.asyncUploadAssetsOnRelease(any(), any(), any(), any()) }
        }

    }
    @Nested
    @DisplayName("When updateing a release")
    inner class UpdateRelease {

        @Test
        @DisplayName("given valid options should update a release")
        fun `given valid options should update a release`() {
            val krelease = KRelease(
                owner = "owner",
                repository = "repository",
                commit = "master",
                title = "Title",
                body = "Body",
                update = true,
                draft = false,
                preRelease = false,
                tag = "v1.0.0",
                path = path,
                githubClient = githubClient
            )

            every { githubClient.validRepositoryOrThrow(any(), any()) } just Runs
            every { githubClient.getRelease(any(),any(),any()) } returns ReleaseDTOObjectMother.new()
            every { githubClient.updateRelease(any()) } returns ReleaseDTOObjectMother.new()
            every { githubClient.asyncUploadAssetsOnRelease(any(), any(), any(), any()) } just Runs

            krelease.execute()

            verify(exactly = 1) { githubClient.validRepositoryOrThrow(any(), any()) }
            verify(exactly = 0) { githubClient.createRelease(any()) }
            verify(exactly = 1) { githubClient.updateRelease(any()) }
            verify(exactly = 1) { githubClient.asyncUploadAssetsOnRelease(any(), any(), any(), any()) }
        }

    }

}
