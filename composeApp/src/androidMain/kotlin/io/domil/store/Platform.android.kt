package io.domil.store

import android.os.Build
import androidx.compose.runtime.Composable
import com.google.mlkit.vision.barcode.common.Barcode

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()