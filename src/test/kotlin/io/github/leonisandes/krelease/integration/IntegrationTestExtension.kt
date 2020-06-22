package io.github.leonisandes.krelease.integration

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource

class IntegrationTestExtension : BeforeAllCallback, CloseableResource {

    override fun beforeAll(context: ExtensionContext?) {
        GithubMockServer.start()
    }

    override fun close() {
        GithubMockServer.stop()
    }
}
