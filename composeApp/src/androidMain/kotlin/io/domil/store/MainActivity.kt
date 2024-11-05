package io.domil.store

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*if (token == "") {
            val intent = Intent(this, UserLoginActivity::class.java)
            intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }*/

        val barcodeScannerComposable: @Composable (
            enable: Boolean, onScanSuccess: (barcodes: String) -> Unit
        ) -> Unit = { enable, onScanSuccess ->
            BarcodeScannerWithCamera(
                enable = enable,
                context = this,
                onScanSuccess = onScanSuccess
            )
        }
        checkPermission()
        clearCash()

        setContent {
            App(barcodeScannerComposable) { _, _ ->
            }
        }
    }

    private fun clearCash() {
        deleteRecursive(cacheDir)
        deleteRecursive(codeCacheDir)
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.let {
                for (child in it) {
                    deleteRecursive(child)
                }
            }
        }
        fileOrDirectory.delete()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                0
            )
        }
    }
}