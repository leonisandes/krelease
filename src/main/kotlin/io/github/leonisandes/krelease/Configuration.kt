package io.github.leonisandes.krelease

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.splitPair
import com.github.ajalt.clikt.parameters.types.file
import com.github.kittinunf.fuel.core.FuelManager
import io.github.leonisandes.krelease.github.GithubClient

fun main(args: Array<String>) {
    runCatching { Configuration().main(args) }
        .onFailure { println("[-] Error message: ${it.message}") }
}

class Configuration : CliktCommand() {
    val accessToken by option(
        help = "Github Access Token",
        names = *arrayOf("-a", "--accessToken"),
        envvar = "GITHUB_ACCESS_TOKEN"
    ).required()

    val repositoryFullName by option(
        help = "GitHub Repository Name: owner/repository",
        names = *arrayOf("-r", "--repository")
    ).splitPair("/").required()

    val commit by option(
        help = "Target commitish, branch or commit SHA. Default: master",
        names = *arrayOf("-c", "--commit")
    ).default("master")

    val title by option(
        help = "Release Title",
        names = *arrayOf("-n", "--title")
    )

    val body by option(
        help = "Text describing the contents of the release",
        names = *arrayOf("-b", "--body")
    )

    val update by option(
        help = "Updated a release",
        names = *arrayOf("--update")
    ).flag(default = false)

    val draft by option(
        help = "It is a draft release?",
        names = *arrayOf("--draft")
    ).flag(default = false)

    val preRelease by option(
        help = "It is a pre release?", names = *arrayOf("--prerelease")
    ).flag(default = false)

    val tag by argument(help = "Release tag")

    val path by argument(
        help = "Path of contents to upload"
    ).file(mustExist = true, canBeFile = false, canBeDir = true, mustBeReadable = true)

    override fun run() {
        println("[!] Connecting to GitHub...")
        val githubClient = GithubClient(fuelManager = FuelManager(), accessToken = accessToken)
        KRelease(
            owner = repositoryFullName.first,
            repository = repositoryFullName.second,
            commit = commit,
            title = title,
            body = body,
            update = update,
            draft = draft,
            preRelease = preRelease,
            tag = tag,
            path = path,
            githubClient = githubClient
        ).execute()
    }

}
