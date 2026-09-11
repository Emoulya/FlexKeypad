package com.emoulya.flexkeypad.data.hid

import com.emoulya.flexkeypad.data.usb.UsbBridgeController
import com.emoulya.flexkeypad.domain.model.ConnectionStatus
import com.emoulya.flexkeypad.domain.model.ModifierKey
import com.emoulya.flexkeypad.domain.repository.HidController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CompositeHidController(
    val bluetoothController: BluetoothHidController,
    val usbController: UsbBridgeController
) : HidController {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    init {
        scope.launch {
            combine(
                bluetoothController.connectionStatus,
                usbController.connectionStatus
            ) { btStatus, usbStatus ->
                when {
                    btStatus == ConnectionStatus.CONNECTED_BLUETOOTH -> ConnectionStatus.CONNECTED_BLUETOOTH
                    usbStatus == ConnectionStatus.CONNECTED_USB -> ConnectionStatus.CONNECTED_USB
                    btStatus == ConnectionStatus.CONNECTING -> ConnectionStatus.CONNECTING
                    else -> ConnectionStatus.DISCONNECTED
                }
            }.collect { mergedStatus ->
                _connectionStatus.value = mergedStatus
            }
        }
    }

    override fun sendKeyDown(keyCode: Int, modifiers: List<ModifierKey>) {
        bluetoothController.sendKeyDown(keyCode, modifiers)
        usbController.sendKeyDown(keyCode, modifiers)
    }

    override fun sendKeyUp(keyCode: Int) {
        bluetoothController.sendKeyUp(keyCode)
        usbController.sendKeyUp(keyCode)
    }

    override fun releaseAllKeys() {
        bluetoothController.releaseAllKeys()
        usbController.releaseAllKeys()
    }

    fun release() {
        job.cancel()
    }
}
