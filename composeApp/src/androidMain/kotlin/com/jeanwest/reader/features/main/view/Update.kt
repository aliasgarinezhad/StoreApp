package com.jeanwest.reader.features.main.view

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.features.shared.AlertDialogWith2Button
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.User
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject


/**
 * The `Update` activity handles application updates. It allows users to download and install new versions of the application,
 * displaying information about the new version and managing the download process.
 *
 * Key features */
@AndroidEntryPoint
class Update : ComponentActivity() {

    private lateinit var rf: RFID
    private var isDownloading = mutableStateOf(false)
    private var openDialog = mutableStateOf(false)
    private var downLoadApkId = 0L
    private var downLoadNoteId = 0L
    private lateinit var barcode: Barcode
    private var appVersion = ""
    private var fileContent = mutableStateOf("")

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var memory: SharedPreference

    @SuppressLint("SetTextI18n", "UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        barcode = Barcode(this) {}
        rf = RFID(this, state) {}
        rf.disconnect()
        appVersion = intent.getStringExtra("appVersion") ?: ""
        showLog("نسخه جدید (${appVersion}) موجود است", state)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse(String.format("package:%s", packageName))), 2
                )
            }
        }

        registerReceiver(
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
        downloadNoteFile()
        setContent {
            AboutUsUI()
        }
    }

    override fun onPause() {
        super.onPause()
        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
    }

    override fun onResume() {
        super.onResume()
        state.currentSnackbarData?.dismiss()
        if (barcode.isEnabled) {
            barcode.disable()
        }
    }

    private fun downloadApkFile() {
        val path = getExternalFilesDir(null)?.path + "/download/" + "app.apk"
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }

        val serverAddress = "https://rfid-api.avakatan.ir/apk/app-debug-$appVersion.apk"
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadManagerRequest = DownloadManager.Request(Uri.parse(serverAddress))
        downloadManagerRequest.setTitle("بروزرسانی RFID")
            .setDescription("در حال دانلود ...")
            .setDestinationInExternalFilesDir(
                this,
                "download",
                "app.apk"
            )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downLoadApkId = downloadManager.enqueue(downloadManagerRequest)
    }

    private fun downloadNoteFile() {
        isDownloading.value = true
        val path = getExternalFilesDir(null)?.path + "/download/" + "release-$appVersion.txt"
        val file = File(path)

        if (file.exists()) {
            file.delete()
        }

        val serverAddress = "https://rfid-api.avakatan.ir/apk/release-$appVersion.txt"
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadManagerRequest = DownloadManager.Request(Uri.parse(serverAddress))
        downloadManagerRequest.setTitle("بروزرسانی RFID")
            .setDescription("در حال دانلود ...")
            .setDestinationInExternalFilesDir(
                this,
                "download",
                "release-$appVersion.txt"
            )
        downLoadNoteId = downloadManager.enqueue(downloadManagerRequest)
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (downLoadApkId == id) {
                isDownloading.value = false

                val path = getExternalFilesDir(null)?.path + "/download/" + "app.apk"
                memory.user = User()
                memory.setAppDataImmediately()
                val file = File(path)
                if (file.exists()) {
                    val installIntent = Intent(Intent.ACTION_VIEW)
                    installIntent.setDataAndType(
                        uriFromFile(applicationContext, File(path)),
                        "application/vnd.android.package-archive"
                    )
                    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    applicationContext.startActivity(installIntent)

                } else {
                    showLog("خطا در به روز رسانی", state)
                }
            } else if (downLoadNoteId == id) {
                isDownloading.value = false
                val path =
                    getExternalFilesDir(null)?.path + "/download/" + "release-$appVersion.txt"
                val file = File(path)

                if (file.exists()) {
                    fileContent.value = file.readText(Charsets.UTF_8)
                } else {
                    showLog("خطا در دریافت فایل", state)
                }
            }
        }
    }

    fun uriFromFile(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context, this.packageName + ".provider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
    }

    private fun back() {
        unregisterReceiver(onDownloadComplete)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 4) {
            back()
        }
        return true
    }

    @Composable
    fun AboutUsUI() {
        MyApplicationTheme {

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = {
                        AppBarWithBack({ back() }, "بروزرسانی")
                    },
                    content = {
                        Box(Modifier.padding(it)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (isDownloading.value) {
                                    CircularProgressIndicator(modifier = Modifier.padding(top = 50.dp))
                                    Text(
                                        text = "در حال دانلود",
                                        modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                                    )
                                }
                                Row {
                                    Row {
                                        Text(
                                            text = appVersion,
                                            modifier = Modifier.padding(
                                                bottom = 20.dp,
                                                top = 20.dp
                                            ),
                                            fontSize = 20.sp,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp)) // Add some space before the icon

                                        Icon(
                                            imageVector = Icons.Default.ArrowForward, // Use a default icon or custom one
                                            contentDescription = "Version Info Icon",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.CenterVertically) // Size of the icon
                                        )

                                        Spacer(modifier = Modifier.width(8.dp)) // Add some space after the icon

                                        Text(
                                            text = packageManager.getPackageInfo(
                                                packageName,
                                                0
                                            ).versionName,
                                            modifier = Modifier.padding(
                                                bottom = 20.dp,
                                                top = 20.dp
                                            ),
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                                Text(
                                    text = "تغییرات نسخه اخیر: \n${fileContent.value}",
                                    modifier = Modifier
                                        .padding(bottom = 20.dp, top = 20.dp)
                                        .weight(3f),
                                    fontSize = 20.sp,
                                )
                                BottomBarButton("بروزرسانی") {
                                    openDialog.value = true
                                }
                                if (openDialog.value) {
                                    AlertDialogWith2Button(
                                        title = "نرم افزار به روز رسانی شود؟",
                                        btnConfirm = "بله",
                                        btnNotConfirm = "خیر",
                                        btnConfirmOnClick = {
                                            downloadApkFile()
                                            openDialog.value = false
                                            isDownloading.value = true
                                        },
                                        btnNotConfirmOnClick = {
                                            openDialog.value = false
                                        },
                                        onDismiss = { openDialog.value = false }
                                    )
                                }
                            }
                        }
                    },
                )
            }
        }
    }

    @Preview
    @Composable
    fun Preview() {
        AboutUsUI()
    }
}