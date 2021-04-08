/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.engine

import io.ktor.application.*
import io.ktor.util.*
import java.io.*
import java.security.*

/**
 * Represents a type of a connector, e.g HTTP or HTTPS.
 * @param name name of the connector.
 *
 * Some engines can support other connector types, hence not a enum.
 */
public data class ConnectorType(val name: String) {
    public companion object {
        /**
         * Non-secure HTTP connector.
         * 1
         */
        public val HTTP: ConnectorType = ConnectorType("HTTP")

        /**
         * Secure HTTP connector.
         */
        public val HTTPS: ConnectorType = ConnectorType("HTTPS")
    }
}

/**
 * Represents a connector configuration.
 */
public interface EngineConnectorConfig {
    /**
     * Type of the connector, e.g HTTP or HTTPS.
     */
    public val type: ConnectorType

    /**
     * The network interface this host binds to as an IP address or a hostname.  If null or 0.0.0.0, then bind to all interfaces.
     */
    public val host: String

    /**
     * The port this application should be bound to or 0 to bind on a random free port.
     */
    public val port: Int
}

/**
 * Represents an SSL connector configuration.
 */
public interface EngineSSLConnectorConfig : EngineConnectorConfig {
    /**
     * KeyStore where a certificate is stored
     */
    public val keyStore: KeyStore

    /**
     * File where the keystore is located
     */
    public val keyStorePath: File?

    /**
     * TLS key alias
     */
    public val keyAlias: String

    /**
     * Keystore password provider
     */
    public val keyStorePassword: () -> CharArray

    /**
     * Private key password provider
     */
    public val privateKeyPassword: () -> CharArray
}

/**
 * Adds a non-secure connector to this engine environment
 */
public inline fun ApplicationEngineEnvironmentBuilder.connector(builder: EngineConnectorBuilder.() -> Unit) {
    connectors.add(EngineConnectorBuilder().apply(builder))
}

/**
 * Adds a secure connector to this engine environment
 */
public inline fun ApplicationEngineEnvironmentBuilder.sslConnector(
    keyStore: KeyStore,
    keyAlias: String,
    noinline keyStorePassword: () -> CharArray,
    noinline privateKeyPassword: () -> CharArray,
    builder: EngineSSLConnectorBuilder.() -> Unit
) {
    connectors.add(EngineSSLConnectorBuilder(keyStore, keyAlias, keyStorePassword, privateKeyPassword).apply(builder))
}

/**
 * Mutable implementation of EngineConnectorConfig for building connectors programmatically
 */
public open class EngineConnectorBuilder(
    override val type: ConnectorType = ConnectorType.HTTP
) : EngineConnectorConfig {
    override var host: String = "0.0.0.0"

    /**
     * The port this application should be bound to.
     * Should be positive port number or 0 to bind on a random free port.
     */
    override var port: Int = 80
        set(newPort) {
            require(newPort in 0..65535) { "A port should be in range [0; 65535] but it's $newPort" }
            field = newPort
        }

    override fun toString(): String {
        return "${type.name} $host:$port"
    }
}

/**
 * Configure connector to start on a random free port.
 */
public fun EngineConnectorBuilder.randomPort() {
    port = 0
}

/**
 * Mutable implementation of EngineSSLConnectorConfig for building connectors programmatically
 */
public class EngineSSLConnectorBuilder(
    override var keyStore: KeyStore,
    override var keyAlias: String,
    override var keyStorePassword: () -> CharArray,
    override val privateKeyPassword: () -> CharArray
) : EngineConnectorBuilder(ConnectorType.HTTPS), EngineSSLConnectorConfig {
    override var keyStorePath: File? = null
}

/**
 * Actual running connector listening properties. The [port] is a positive port number.
 * @property host that the connector is listening
 * @property port the connector is bound to
 */
public class EngineConnectorInfo(
    public val type: ConnectorType,
    public val host: String,
    public val port: Int
) {
    init {
        require(port in 1..65535) {
            "Port should be in range [1..65535] but it's $port"
        }
    }
}

/**
 * Event that is fired by engines when a particular connector is bound and ready.
 * Depending on the engine it may be fired exactly after the port is ready or
 * slightly later when the startup sequence is complete.
 */
public val EngineConnectorStarted: EventDefinition<EngineConnectorInfo> = EventDefinition()

@EngineAPI
internal val StartedConnectorsAttributeKey: AttributeKey<List<EngineConnectorInfo>> =
    AttributeKey("StartedConnectors")

/**
 * Provides the list of started connectors or fails if it not supported on the particular engine.
 * This may be the same as [connectorsConfig] in some cases. For example, if the server
 * is not yet started.
 */
public val ApplicationEnvironment.startedConnectors: List<EngineConnectorInfo>
    get() {
        if (this !is ApplicationEngineEnvironment) {
            error("This engine doesn't provide connector's state information")
        }

        val startedConnectorsList = application.attributes.getOrNull(StartedConnectorsAttributeKey)
        if (startedConnectorsList != null) {
            return startedConnectorsList
        }

        // for engines that don't support this attribute
        return connectorsConfig.mapNotNull {
            when (it.port) {
                0 -> null
                else -> EngineConnectorInfo(it.type, it.host, it.port)
            }
        }
    }

/**
 * Engine connectors configuration. Unlike [startedConnectors], it provides connectors
 * as they configured. In particular, the difference is that it could have zero port specified
 * in the config while in the [startedConnectors] it will have the particular actual port.
 */
public val ApplicationEngineEnvironment.connectorsConfig: List<EngineConnectorConfig>
    get() = @Suppress("DEPRECATION") connectors
