package com.example.flexkeypad

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.flexkeypad.data.feedback.HapticFeedbackHelper
import com.example.flexkeypad.data.hid.BluetoothHidController
import com.example.flexkeypad.data.hid.CompositeHidController
import com.example.flexkeypad.data.repository.DataStoreAppSettingsRepositoryImpl
import com.example.flexkeypad.data.repository.JsonProfileRepositoryImpl
import com.example.flexkeypad.data.usb.UsbBridgeController
import com.example.flexkeypad.domain.model.CanvasMode
import com.example.flexkeypad.domain.usecase.DispatchKeyStrokeUseCase
import com.example.flexkeypad.domain.usecase.ManageProfileUseCase
import com.example.flexkeypad.ui.canvas.CanvasScreen
import com.example.flexkeypad.ui.theme.FlexKeypadTheme
import com.example.flexkeypad.ui.viewmodel.KeypadViewModel

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothController: BluetoothHidController
    private lateinit var usbController: UsbBridgeController
    private lateinit var compositeController: CompositeHidController
    private lateinit var hapticHelper: HapticFeedbackHelper
    private lateinit var profileRepository: JsonProfileRepositoryImpl
    private lateinit var appSettingsRepository: DataStoreAppSettingsRepositoryImpl

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            bluetoothController.initializeHidProfile()
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (state == BluetoothAdapter.STATE_ON) {
                    bluetoothController.initializeHidProfile()
                }
            }
        }
    }

    private val viewModel: KeypadViewModel by viewModels {
        KeypadViewModel.Factory(
            manageProfileUseCase = ManageProfileUseCase(profileRepository),
            dispatchKeyStrokeUseCase = DispatchKeyStrokeUseCase(compositeController),
            compositeHidController = compositeController,
            hapticFeedbackHelper = hapticHelper,
            appSettingsRepository = appSettingsRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.ALPHA,
                1f,
                0f
            )
            fadeOut.interpolator = AccelerateDecelerateInterpolator()
            fadeOut.duration = 300L
            fadeOut.doOnEnd { splashScreenViewProvider.remove() }
            fadeOut.start()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // Initialize dependencies
        hapticHelper = HapticFeedbackHelper(this)
        bluetoothController = BluetoothHidController(this)
        usbController = UsbBridgeController.getInstance(port = 8899)
        usbController.startServer()
        compositeController = CompositeHidController(bluetoothController, usbController)
        profileRepository = JsonProfileRepositoryImpl(this)
        appSettingsRepository = DataStoreAppSettingsRepositoryImpl(this)

        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        requestBluetoothPermissionsIfNeeded()

        setContent {
            FlexKeypadTheme {
                val state by viewModel.uiState.collectAsState()

                // PRD 7.3: Screen Always-On during Play Mode
                LaunchedEffect(state.canvasMode) {
                    if (state.canvasMode == CanvasMode.PLAY) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                // Full Screen Mode: Hide / Show System Status & Navigation Bars
                LaunchedEffect(state.isFullScreen) {
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    if (state.isFullScreen) {
                        insetsController.hide(WindowInsetsCompat.Type.systemBars())
                        insetsController.systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        insetsController.show(WindowInsetsCompat.Type.systemBars())
                    }
                }

                CanvasScreen(viewModel = viewModel)
            }
        }
    }

    private fun requestBluetoothPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnect = ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            val hasAdvertise = ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasConnect || !hasAdvertise) {
                bluetoothPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    )
                )
            } else {
                bluetoothController.initializeHidProfile()
            }
        } else {
            bluetoothController.initializeHidProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        bluetoothController.initializeHidProfile()
    }

    override fun onPause() {
        super.onPause()
        // Guarantee no stuck keys if user minimizes or switches apps
        viewModel.releaseAllPressedButtons()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (_: Exception) {}

        if (isFinishing) {
            compositeController.release()
            bluetoothController.release()
            usbController.stopServer()
        }
    }
}