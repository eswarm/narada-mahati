package `in`.eswarm.narada.mqtt

import io.moquette.broker.config.IConfig
import io.moquette.broker.security.IAuthenticator

class BasicAuthenticator(config: IConfig) : IAuthenticator {

    private val storedUserName: String = config.getProperty(USERNAME, "")
    private val storedPassword: String = config.getProperty(PASSWORD, "")

    override fun checkValid(clientId: String?, username: String?, password: ByteArray?): Boolean {
        if (username == null || password == null) {
            return false
        }

        if (username == storedUserName && password.contentEquals(storedPassword.toByteArray())) {
            return true
        }

        return false
    }

    companion object {
        const val USERNAME = "narada_username"
        const val PASSWORD = "narada_password"
    }
}