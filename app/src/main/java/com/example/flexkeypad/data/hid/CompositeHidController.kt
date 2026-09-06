package com.example.flexkeypad.data.hid

import com.example.flexkeypad.data.usb.UsbBridgeController
import com.example.flexkeypad.domain.model.ConnectionStatus
import com.example.flexkeypad.domain.model.ModifierKey
import com.example.flexkeypad.domain.repository.HidController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CompositeHidController(
    val bluetoothController: BluetoothHidController,
    val usbController: UsbBridgeController
) : HidController {

    private val scope = CoroutineScope(Dispatchers.Default)
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
}
