package io.github.leonisandes.krelease.github.exceptions

import java.lang.RuntimeException

class GithubException(statusCode: Int, json: String) : RuntimeException() {

    override val message: String = "Github returned statusCode $statusCode with body $json"
}
