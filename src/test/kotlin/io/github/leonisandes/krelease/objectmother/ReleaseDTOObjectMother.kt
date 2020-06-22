package io.github.leonisandes.krelease.objectmother

import io.github.leonisandes.krelease.github.dto.ReleaseDTO

object ReleaseDTOObjectMother {

    fun new(
        id: String = "1",
        url: String = "https://api.github.com/repos/octocat/Hello-World/releases/1"
    ) = ReleaseDTO(id = id, url = url)
}
