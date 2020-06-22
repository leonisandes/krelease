package io.github.leonisandes.krelease

import io.github.leonisandes.krelease.github.GithubClient
import io.github.leonisandes.krelease.github.dto.CreateReleaseDTO
import io.github.leonisandes.krelease.github.dto.ReleaseDTO
import io.github.leonisandes.krelease.github.dto.UpdateReleaseDTO
import java.io.File

class KRelease(
    private val owner: String,
    private val repository: String,
    private val commit: String,
    private val title: String?,
    private val body: String?,
    private val update: Boolean,
    private val draft: Boolean,
    private val preRelease: Boolean,
    private val tag: String,
    private val path: File,
    private val githubClient: GithubClient
) {

    fun execute() {
        printBanner()

        println("[!] Validating owner and repository...")
        githubClient.validRepositoryOrThrow(owner = owner, repository = repository)

        val releaseDTO = if (update) { updateRelease() } else { createNewRelease() }

        println("[!] Searching files to upload...")
        githubClient.asyncUploadAssetsOnRelease(
            owner = owner,
            repository = repository,
            path = path,
            releaseId = releaseDTO.id
        )

        println("[!] All assets were uploaded!")
    }

    private fun createNewRelease(): ReleaseDTO {
        println("[!] Creating release: $tag")
        val releaseDTO = githubClient.createRelease(createReleaseDTO = createReleaseDTO())
        println("\t[+] Release was created: ${releaseDTO.url}")
        return releaseDTO
    }

    private fun updateRelease(): ReleaseDTO {
        println("[!] Getting release by tag $tag")
        return githubClient.getRelease(owner = owner, repository = repository, tag = tag).let {
            println("[!] Updating release id ${it.id}")
            githubClient.updateRelease(updateReleaseDTO = createUpdateReleaseDTO(it.id))
        }
    }

    private fun createUpdateReleaseDTO(releaseId: String): UpdateReleaseDTO {
        return UpdateReleaseDTO.from(
            owner = owner,
            repository = repository,
            id = releaseId,
            commit = commit,
            title = title,
            body = body,
            draft = draft,
            prerelease = preRelease,
            tag = tag
        )
    }

    private fun printBanner() {
        println(
            """
██╗  ██╗██████╗ ███████╗██╗     ███████╗ █████╗ ███████╗███████╗
██║ ██╔╝██╔══██╗██╔════╝██║     ██╔════╝██╔══██╗██╔════╝██╔════╝
█████╔╝ ██████╔╝█████╗  ██║     █████╗  ███████║███████╗█████╗  
██╔═██╗ ██╔══██╗██╔══╝  ██║     ██╔══╝  ██╔══██║╚════██║██╔══╝  
██║  ██╗██║  ██║███████╗███████╗███████╗██║  ██║███████║███████╗
╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝╚═╝  ╚═╝╚══════╝╚══════╝
                         by Leoni Sandes <leonisandes@gmail.com>
""")
    }

    private fun createReleaseDTO(): CreateReleaseDTO {
        return CreateReleaseDTO.from(
            owner = owner,
            repository = repository,
            commit = commit,
            title = title,
            body = body,
            draft = draft,
            prerelease = preRelease,
            tag = tag
        )
    }
}
