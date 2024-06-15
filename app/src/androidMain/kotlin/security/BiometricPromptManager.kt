package security

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricPromptManager(
    private val activity: AppCompatActivity,
) {
    private val resultChannel = Channel<BiometricResult>()
    val promptResult = resultChannel.receiveAsFlow()

    val authenticators =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_STRONG
        }

    private val biometricPromptAuthenticationCallback =
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence,
            ) {
                super.onAuthenticationError(errorCode, errString)
                resultChannel.trySend(BiometricResult.AuthenticationError(errString.toString()))
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                resultChannel.trySend(BiometricResult.AuthenticationFailed)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                resultChannel.trySend(BiometricResult.AuthenticationSuccess)
            }
        }

    fun canUseAuthenticator(): Boolean {
        val canAuthenticate = BiometricManager.from(activity).canAuthenticate(authenticators)
        return when (canAuthenticate) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_SUCCESS,
            -> {
                true
            }
            else -> false
        }
    }

    fun isDeviceEnrolled(): Boolean =
        BiometricManager.from(activity).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS

    fun showBiometricPrompt(
        title: String,
        cancel: String,
    ) {
        val manager = BiometricManager.from(activity)
        val promptInfo =
            PromptInfo
                .Builder()
                .apply {
                    setTitle(title)
                    setAllowedAuthenticators(authenticators)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                        setNegativeButtonText(cancel)
                    }
                }.build()

        when (manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                resultChannel.trySend(BiometricResult.HardwareUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                resultChannel.trySend(BiometricResult.FeatureUnavailable)
                return
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                resultChannel.trySend(BiometricResult.AuthenticationNotSet)
                return
            }
            else -> Unit
        }

        BiometricPrompt(
            activity,
            biometricPromptAuthenticationCallback,
        ).authenticate(promptInfo)
    }

    fun onDestroy() {
        resultChannel.close()
    }

    sealed interface BiometricResult {
        data class AuthenticationError(val error: String) : BiometricResult

        data object HardwareUnavailable : BiometricResult

        data object FeatureUnavailable : BiometricResult

        data object AuthenticationFailed : BiometricResult

        data object AuthenticationSuccess : BiometricResult

        data object AuthenticationNotSet : BiometricResult
    }
}
