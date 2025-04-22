@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.kiosk.view

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.data.RepositoryImpl
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.AppBarWithDeleteButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.SnackBarActions
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StoreShelf
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class SearchProduct : ComponentActivity() {

    private lateinit var product: Product
    lateinit var barcode: Barcode
    lateinit var rf: RFID

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var api: API

    @Inject
    lateinit var repository: RepositoryImpl

    @Inject
    lateinit var memory: SharedPreference
    var loading by mutableStateOf(false)
    private var isFullScreenImage by mutableStateOf(false)
    private var imgUrls = mutableListOf<String>()
    private val uiList = mutableListOf<StoreShelf>()
    private var mainImgUrl by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        init()
        setContent {
            Page()
        }
        intent.getStringExtra("product").let {
            if (it.isNullOrEmpty()) {
                showLog("کالایی جهت بررسی وجود ندارد", state)

                product = Product(
                    name = "ساپورت",
                    KBarCode = "64822109J-8010-F",
                    imageUrl = "https://www.banimode.com/primaryLight/image.php?token=tmv43w4as&code=64822109J-8010-F",
                    storeNumber = 1,
                    wareHouseNumber = 0,
                    productCode = "64822109",
                    size = "F",
                    color = "8010",
                    originalPrice = "1490000",
                    salePrice = "1490000",
                    primaryKey = 9514289L,
                    rfidKey = 130290L,
                )

            } else {
                val type = object : TypeToken<Product>() {}.type
                product = Gson().fromJson(
                    it,
                    type
                )
                getProductDetails(product.KBarCode)
            }
        }
        mainImgUrl = product.imageUrl

        getImgsUrls()
    }

    private fun init() {
        rf = RFID(this, state) {
            scanTrigger()
        }
        barcode = Barcode(this)
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    override fun onPause() {
        super.onPause()
        state.currentSnackbarData?.dismiss()
        if (rf.scanning) {
            rf.stopScanning()
        }
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

    private fun getImgsUrls() {
        api.getProductImagesList(product.KBarCode, {
            imgUrls.addAll(it)
        }, {
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (event.repeatCount == 0) {
            if (keyCode == 280 || keyCode == 293 || keyCode == 139) {
                scanTrigger()
            } else if (keyCode == 4) {
                back()
            }
        }
        return true
    }

    private fun getProductShelfs() {
        loading = true
        val skuCode = product.productCode + "-" + product.color
        api.getProductShelfAddressStore(skuCode, {
            uiList.clear()
            uiList.addAll(it)
            loading = false
        }, {
            loading = false
        })
    }

    private fun getProductDetails(barcode: String) {

        loading = true
        repository.getBarcodeDetails(
            barcode,
            { productDetails ->

                product.rfidKey = productDetails.rfidKey
                api.getProductsSearchCode(product.primaryKey.toString(), {
                    product.searchCodes.addAll(it)
                    Log.e("search", product.toString())
                    getProductShelfs()
                }, {
                    showLog("اطلاعات سرچ کد یافت نشد", state)
                    loading = false
                })

                loading = false
            },
            {
                loading = false
            }
        )
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString("searchProductEPCTable", JSONArray(rf.epcs).toString())
        edit.apply()
    }

    fun scanTrigger() {

        if (!rf.scanning) {
            rf.startFinding(product)
        } else {
            rf.stopScanning()
            saveToMemory()
        }
    }

    private fun back() {
        if (isFullScreenImage) {
            isFullScreenImage = !isFullScreenImage
        } else {
            rf.stopScanning()
            saveToMemory()
            finish()
        }
    }

    @Composable
    fun Page() {
        MyApplicationTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = { AppBar() },
                    content = {
                        Box(modifier = Modifier.padding(it)) {
                            Content()
                        }
                    },
                    snackbarHost = { ErrorSnackBar(state) },
                )
            }
        }
    }

    @Composable
    fun AppBar() {
        AppBarWithDeleteButton(
            onBackPressed = {
                back()
            },
            title = "جست و جو",
            onDeletePressed = {
                rf.matchedEpcTable.clear()
            }
        )
    }

    @Composable
    fun Content() {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (loading) {
                LoadingCircularProgressIndicator(rf.scanning, loading)
            } else {
                if (isFullScreenImage) {
                    FullScreenImage(mainImgUrl)
                } else {
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .border(
                                BorderStroke(1.dp, primaryLight),
                                shape = MaterialTheme.shapes.small
                            )
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = MaterialTheme.shapes.small
                            ),
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "فاصله",
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .align(Alignment.CenterVertically),
                                textAlign = TextAlign.Center
                            )
                            LinearProgressIndicator(
                                progress = { rf.distance },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterVertically)
                                    .padding(horizontal = 8.dp),
                                trackColor = MaterialTheme.colorScheme.background
                            )
                        }
                        LoadingCircularProgressIndicator(rf.scanning)
                    }

                    Column(
                        Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .fillMaxWidth()
                            .border(
                                BorderStroke(1.dp, primaryLight),
                                shape = MaterialTheme.shapes.small
                            )
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = MaterialTheme.shapes.small
                            ),
                    ) {
                        LazyColumnItem()
                    }
                    Text(
                        text = "آدرس در قفسه: ",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Right,
                        color = Color.Black,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(16.dp)
                    )
                    Column(
                        Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .border(
                                BorderStroke(1.dp, primaryLight),
                                shape = MaterialTheme.shapes.small
                            )
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = MaterialTheme.shapes.small
                            ),
                    ) {

                        LazyColumn {
                            items(uiList.size) { i ->
                                SearchProductsItems(
                                    i,
                                    uiList
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LazyColumnItem() {

        val modifier = Modifier
            .padding(top = 2.dp, bottom = 2.dp)
            .wrapContentWidth()

        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.small
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp)
                .clickable {
                    val clipboard: ClipboardManager =
                        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("stockDraftId", product.KBarCode)
                    clipboard.setPrimaryClip(clip)
                    showLog("بارکد کالا کپی شد.", state, SnackBarActions.SUCCESS)
                }
        ) {

            Box(modifier = Modifier.size(width = 180.dp, height = 200.dp)) {
                AsyncImage(
                    model = product.imageUrl, contentDescription = null, modifier = Modifier
                        .height(200.dp)
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .clickable {
                            isFullScreenImage = !isFullScreenImage
                        }
                )
            }

            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = product.KBarCode,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = "قیمت: " + product.originalPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = "فروش: " + product.salePrice,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = "موجودی فروشگاه: " + product.storeNumber.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = "موجودی انبار: " + product.wareHouseNumber.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = "پیدا شده: ${rf.matchedEpcTable.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
            }
        }
    }

    @Composable
    fun FullScreenImage(url: String) {

        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        var rotationState by remember { mutableFloatStateOf(0f) }
        var imageSize by remember { mutableStateOf(IntSize.Zero) }
        var containerSize by remember { mutableStateOf(IntSize.Zero) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { layoutCoordinates ->
                    containerSize = layoutCoordinates.size // Get the size of the container
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        val newScale = scale * zoom
                        scale = max(1f, min(newScale, 4f))
                        rotationState += rotation

                        // Calculate the maximum offset for each direction
                        val maxX = (imageSize.width * scale - containerSize.width) / 2f
                        val maxY = (imageSize.height * scale - containerSize.height) / 2f

                        // Apply panning with constraints
                        offsetX = max(-maxX, min(maxX, offsetX + pan.x))
                        offsetY = max(-maxY, min(maxY, offsetY + pan.y))
                    }
                }
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = url),
                contentDescription = "",
                modifier = Modifier
                    .onGloballyPositioned { layoutCoordinates ->
                        imageSize = layoutCoordinates.size // Get the size of the image
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY,
                        rotationZ = rotationState
                    )
                    .fillMaxSize()
                    .clickable {
                        isFullScreenImage = false
                    },  // Initial size of the image
                contentScale = ContentScale.Crop
            )
            ZoomableImageGrid(images = imgUrls)
        }
    }

    @Composable
    fun ZoomableImageGrid(images: List<String>) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier
                .fillMaxHeight()
                .width(65.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(images.size) { index ->
                Image(
                    painter = rememberAsyncImagePainter(images[index]),
                    contentDescription = images[index],
                    modifier = Modifier
                        .size(60.dp)
                        .clickable {
                            mainImgUrl = images[index]
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    @Composable
    fun SearchProductsItems(i: Int, uiList: MutableList<StoreShelf>) {

        val topPadding = if (i == 0) 16.dp else 12.dp
        val bottomPadding = if (i == uiList.size - 1) 128.dp else 0.dp

        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = bottomPadding,
                    top = topPadding
                )
                .shadow(elevation = 5.dp, shape = MaterialTheme.shapes.small)
                .background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.small
                )
                .fillMaxWidth()
                .height(90.dp)
                .testTag("items")

        ) {
            Row(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .fillMaxWidth()
                    .weight(1f), Arrangement.SpaceBetween
            ) {
                Text(
                    text = "شماره قفسه: ${uiList[i].shelfTitle}",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Right,
                    color = Color.Black,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    text = "نوع قفسه: ${uiList[i].shelfTitleType}",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Row(
                modifier = Modifier
                    .padding(start = 8.dp, end = 10.dp, bottom = 10.dp)
                    .fillMaxWidth()
                    .weight(1f), Arrangement.SpaceBetween
            ) {
                Text(
                    text = "توضیحات: ${uiList[i].shelfDes}",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.align(Alignment.Top)
                )
            }
        }
    }
}

