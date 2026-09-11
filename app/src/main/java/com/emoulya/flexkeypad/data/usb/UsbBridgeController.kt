package com.emoulya.flexkeypad.data.usb

import android.util.Log
import com.emoulya.flexkeypad.domain.model.ConnectionStatus
import com.emoulya.flexkeypad.domain.model.ModifierKey
import com.emoulya.flexkeypad.domain.repository.HidController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

@Serializable
data class UsbKeyEvent(
    val action: String, // "DOWN", "UP", "RELEASE_ALL"
    val keyCode: Int = 0,
    val modifiers: List<String> = emptyList()
)

class UsbBridgeController private constructor(
    private val port: Int = 8899
) : HidController {

    companion object {
        private const val TAG = "UsbBridgeController"
        const val DEFAULT_PORT = 8899

        @Volatile
        private var instance: UsbBridgeController? = null

        fun getInstance(port: Int = DEFAULT_PORT): UsbBridgeController {
            return instance ?: synchronized(this) {
                instance ?: UsbBridgeController(port).also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverJob: Job? = null
    private var isServerRunning = false
    private var serverSocket: ServerSocket? = null
    private val connectedClients = CopyOnWriteArrayList<Socket>()
    private val clientWriters = CopyOnWriteArrayList<PrintWriter>()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _connectedClientsCount = MutableStateFlow(0)
    val connectedClientsCount: StateFlow<Int> = _connectedClientsCount.asStateFlow()

    @Synchronized
    fun startServer() {
        if (isServerRunning && serverJob?.isActive == true) {
            Log.d(TAG, "Server already running on port $port")
            return
        }
        isServerRunning = true

        serverJob = scope.launch {
            while (isActive && isServerRunning) {
                var socket: ServerSocket? = null
                try {
                    socket = ServerSocket().apply {
                        reuseAddress = true
                        bind(java.net.InetSocketAddress(port))
                    }
                    serverSocket = socket
                    Log.d(TAG, "USB Bridge Server listening on port $port")

                    while (isActive && isServerRunning) {
                        val client = socket.accept()
                        Log.d(TAG, "New USB companion connected: ${client.remoteSocketAddress}")

                        connectedClients.add(client)
                        val writer = PrintWriter(client.getOutputStream(), true)
                        clientWriters.add(writer)

                        // Immediately acknowledge companion handshake
                        writer.println("{\"action\":\"CONNECTED_ACK\"}")

                        _connectedClientsCount.value = connectedClients.size
                        _connectionStatus.value = ConnectionStatus.CONNECTED_USB

                        // Monitor client disconnect & companion messages
                        launch {
                            try {
                                val reader = client.getInputStream().bufferedReader()
                                while (isActive && isServerRunning) {
                                    val line = reader.readLine() ?: break
                                    if (line.contains("\"PING\"")) {
                                        writer.println("{\"action\":\"PONG\"}")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.d(TAG, "Client read exception: ${e.message}")
                            } finally {
                                Log.d(TAG, "Companion disconnected: ${client.remoteSocketAddress}")
                                clientWriters.remove(writer)
                                connectedClients.remove(client)
                                try { client.close() } catch (_: Exception) {}
                                _connectedClientsCount.value = connectedClients.size
                                if (connectedClients.isEmpty()) {
                                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (!isActive || !isServerRunning) break
                    Log.e(TAG, "Server socket error, retrying in 2 seconds...", e)
                    try { socket?.close() } catch (_: Exception) {}
                    try { serverSocket?.close() } catch (_: Exception) {}
                    serverSocket = null
                    kotlinx.coroutines.delay(2000)
                }
            }
        }
    }

    private fun broadcastEvent(event: UsbKeyEvent) {
        if (clientWriters.isEmpty()) {
            Log.d(TAG, "broadcastEvent skipped: 0 clients connected (event: ${event.action} ${event.keyCode})")
            return
        }
        scope.launch {
            val json = Json.encodeToString(event)
            Log.d(TAG, "Broadcasting USB event: $json to ${clientWriters.size} client(s)")
            for (writer in clientWriters) {
                try {
                    writer.println(json)
                } catch (e: Exception) {
                    Log.e(TAG, "Error writing to USB companion client", e)
                }
            }
        }
    }

    override fun sendKeyDown(keyCode: Int, modifiers: List<ModifierKey>) {
        broadcastEvent(
            UsbKeyEvent(
                action = "DOWN",
                keyCode = keyCode,
                modifiers = modifiers.map { it.name }
            )
        )
    }

    override fun sendKeyUp(keyCode: Int) {
        broadcastEvent(
            UsbKeyEvent(
                action = "UP",
                keyCode = keyCode
            )
        )
    }

    override fun releaseAllKeys() {
        broadcastEvent(
            UsbKeyEvent(action = "RELEASE_ALL")
        )
    }

    @Synchronized
    fun stopServer() {
        isServerRunning = false
        serverJob?.cancel()
        serverJob = null
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server socket", e)
        }
        serverSocket = null
        for (client in connectedClients) {
            try { client.close() } catch (_: Exception) {}
        }
        connectedClients.clear()
        clientWriters.clear()
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _connectedClientsCount.value = 0
    }
}
