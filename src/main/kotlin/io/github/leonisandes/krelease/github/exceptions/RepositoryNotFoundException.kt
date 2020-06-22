package io.github.leonisandes.krelease.github.exceptions

import java.lang.RuntimeException

class RepositoryNotFoundException(owner: String, repository: String) : RuntimeException() {

    override val message: String = "Repository ${owner}/${repository} not found."
}
