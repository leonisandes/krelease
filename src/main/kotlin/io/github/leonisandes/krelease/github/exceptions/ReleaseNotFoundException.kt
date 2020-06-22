package io.github.leonisandes.krelease.github.exceptions

import java.lang.RuntimeException

class ReleaseNotFoundException(search: String, value: String) : RuntimeException() {

    override val message: String = "It was not possible to find a release by $search $value."
}
