package com.example.flexkeypad.data.hid

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.flexkeypad.domain.model.ConnectionStatus
import com.example.flexkeypad.domain.model.ModifierKey
import com.example.flexkeypad.domain.repository.HidController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

class BluetoothHidController(
    private val context: Context
) : HidController {

    companion object {
        private const val TAG = "BluetoothHidController"
        private const val APP_NAME = "FlexKeypad"
        private const val APP_DESCRIPTION = "Modular Virtual Macro Pad"
        private const val APP_PROVIDER = "Emoulya"
        private const val SUBCLASS_KEYBOARD: Byte = 0x40
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private var isAppRegistered = false

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val executor = Executors.newSingleThreadExecutor()

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "Bluetooth HID Profile Proxy connected")
                hidDevice = proxy as? BluetoothHidDevice
                registerHidApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "Bluetooth HID Profile Proxy disconnected")
                hidDevice = null
                connectedDevice = null
                isAppRegistered = false
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                _connectedDeviceName.value = null
            }
        }
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)
            isAppRegistered = registered
            Log.d(TAG, "HID App registration changed: registered=$registered")
            if (registered && pluggedDevice != null) {
                handleDeviceConnection(pluggedDevice)
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            Log.d(TAG, "Device connection state changed: device=${device?.address}, state=$state")
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    device?.let { handleDeviceConnection(it) }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (connectedDevice?.address == device?.address) {
                        connectedDevice = null
                        _connectedDeviceName.value = null
                        _connectionStatus.value = ConnectionStatus.DISCONNECTED
                    }
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    _connectionStatus.value = ConnectionStatus.CONNECTING
                }
            }
        }
    }

    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun initializeHidProfile() {
        if (!hasBluetoothPermissions()) {
            Log.w(TAG, "Bluetooth permissions not granted yet.")
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth is disabled or not supported.")
            return
        }

        if (hidDevice != null && isAppRegistered) {
            Log.d(TAG, "HID profile is already active and registered.")
            return
        }

        try {
            bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while requesting HID profile proxy", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun registerHidApp() {
        val hid = hidDevice ?: return
        if (isAppRegistered) return
        if (!hasBluetoothPermissions()) return

        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            APP_NAME,
            APP_DESCRIPTION,
            APP_PROVIDER,
            SUBCLASS_KEYBOARD,
            HidReportDescriptor.KEYBOARD_DESCRIPTOR
        )

        try {
            hid.registerApp(sdpSettings, null, null, executor, hidCallback)
            val connected = hid.getConnectedDevices()
            if (connected.isNotEmpty()) {
                handleDeviceConnection(connected.first())
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while registering HID App", e)
        }
    }

    fun getBondedDevices(): List<BluetoothDevice> {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) return emptyList()
        return try {
            bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException reading bonded devices", e)
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice): Boolean {
        val hid = hidDevice
        if (hid == null) {
            Log.w(TAG, "Cannot connect: HID device proxy is null. Attempting re-init...")
            initializeHidProfile()
            return false
        }
        if (!hasBluetoothPermissions()) {
            Log.w(TAG, "Cannot connect: Missing Bluetooth permissions")
            return false
        }
        return try {
            Log.d(TAG, "Initiating HID connection to ${device.name ?: device.address}...")
            _connectionStatus.value = ConnectionStatus.CONNECTING
            val initiated = hid.connect(device)
            Log.d(TAG, "hid.connect result: $initiated")
            if (!initiated) {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
            initiated
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in connect", e)
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            false
        } catch (e: Exception) {
            Log.e(TAG, "Exception in connect", e)
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect(device: BluetoothDevice? = connectedDevice): Boolean {
        val hid = hidDevice ?: return false
        val target = device ?: return false
        if (!hasBluetoothPermissions()) return false
        return try {
            val disconnected = hid.disconnect(target)
            if (disconnected) {
                connectedDevice = null
                _connectedDeviceName.value = null
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
            disconnected
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in disconnect", e)
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleDeviceConnection(device: BluetoothDevice) {
        connectedDevice = device
        _connectedDeviceName.value = try {
            device.name ?: device.address
        } catch (e: SecurityException) {
            device.address
        }
        _connectionStatus.value = ConnectionStatus.CONNECTED_BLUETOOTH
    }

    @SuppressLint("MissingPermission")
    override fun sendKeyDown(keyCode: Int, modifiers: List<ModifierKey>) {
        val hid = hidDevice ?: return
        val device = connectedDevice ?: return
        if (!hasBluetoothPermissions()) return

        val report = HidReportDescriptor.buildKeyboardReport(
            modifiers = modifiers,
            keyCodes = if (keyCode > 0) listOf(keyCode) else emptyList()
        )

        try {
            hid.sendReport(device, HidReportDescriptor.KEYBOARD_REPORT_ID, report)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in sendKeyDown", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending KeyDown report", e)
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendKeyUp(keyCode: Int) {
        val hid = hidDevice ?: return
        val device = connectedDevice ?: return
        if (!hasBluetoothPermissions()) return

        val releaseReport = HidReportDescriptor.buildReleaseReport()

        try {
            hid.sendReport(device, HidReportDescriptor.KEYBOARD_REPORT_ID, releaseReport)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in sendKeyUp", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending KeyUp report", e)
        }
    }

    override fun releaseAllKeys() {
        sendKeyUp(0)
    }

    fun release() {
        releaseAllKeys()
        hidDevice?.let { hid ->
            try {
                if (hasBluetoothPermissions()) {
                    hid.unregisterApp()
                }
                bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hid)
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException in release", e)
            }
        }
        hidDevice = null
        connectedDevice = null
    }
}
