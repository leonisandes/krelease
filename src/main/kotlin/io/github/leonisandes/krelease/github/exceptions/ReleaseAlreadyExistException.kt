package io.github.leonisandes.krelease.github.exceptions

import java.lang.RuntimeException

class ReleaseAlreadyExistException(val tag: String) : RuntimeException() {

    override val message: String = "Release $tag already exist."
}
