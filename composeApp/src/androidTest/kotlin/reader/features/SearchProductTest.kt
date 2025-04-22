package reader.features

import android.view.KeyEvent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.models.Product
import org.junit.Rule
import org.junit.Test

class SearchProductTest {

    @get:Rule
    var activity = createAndroidComposeRule<SearchProduct>()

    //Find special product and see results (found EPC, product specification and image matching and beep)
    @Test
    fun searchSubActivityTest1() {

        start()

        activity.onNodeWithText(product.name).assertExists()
        activity.onNodeWithText(product.KBarCode).assertExists()
        activity.onNodeWithText("قیمت: " + product.originalPrice).assertExists()
        activity.onNodeWithText("فروش: " + product.salePrice).assertExists()
        activity.onNodeWithText("موجودی فروشگاه: " + product.storeNumber.toString()).assertExists()
        activity.onNodeWithText("موجودی انبار: " + product.wareHouseNumber.toString())
            .assertExists()
        activity.onNodeWithText("پیدا شده: 0").assertExists()

        activity.activity.rf.epcs.clear()
        activity.activity.rf.matchedEpcTable.add(epcGenerator(48, 0, 0, 101, product.rfidKey, 50L))

        activity.activity.scanTrigger()
        activity.waitForIdle()
        Thread.sleep(1000)
        activity.waitForIdle()
        activity.activity.scanTrigger()
        activity.waitForIdle()

        var time = System.currentTimeMillis()
        var currentDistance = 1F

        activity.activity.rf.matchedEpcTable

        while (System.currentTimeMillis() - time < 10000) {

            activity.waitForIdle()

            if (currentDistance == 1F && activity.activity.rf.distance == 0.7F) {
                time = System.currentTimeMillis()
                currentDistance = 0.7F
                activity.waitForIdle()
            } else if (currentDistance == 0.7F && activity.activity.rf.distance == 0.5F) {
                time = System.currentTimeMillis()
                currentDistance = 0.5F
            } else if (currentDistance == 0.5F && activity.activity.rf.distance == 0.2F) {
                time = System.currentTimeMillis()
                currentDistance = 0.2F
            } else if (currentDistance == 0.7F && activity.activity.rf.distance == 0.5F) {
                time = System.currentTimeMillis()
                currentDistance = 0.5F
            }
        }
        activity.waitForIdle()
        activity.onNodeWithText("پیدا شده: 1").assertExists()

        activity.waitForIdle()

        activity.activity.rf.epcs.clear()

        currentDistance = 0.05F
        time = System.currentTimeMillis()

        while (System.currentTimeMillis() - time < 2000) {

            activity.waitForIdle()

            if (currentDistance == 0.05F && activity.activity.rf.distance == 0.2F) {
                time = System.currentTimeMillis()
                currentDistance = 0.2F
            } else if (currentDistance == 0.2F && activity.activity.rf.distance == 0.5F) {
                time = System.currentTimeMillis()
                currentDistance = 0.5F
            } else if (currentDistance == 0.5F && activity.activity.rf.distance == 0.7F) {
                time = System.currentTimeMillis()
                currentDistance = 0.7F
            } else if (currentDistance == 0.7F && activity.activity.rf.distance == 1F) {
                time = System.currentTimeMillis()
                currentDistance = 1F
            }
        }

        activity.waitForIdle()
        activity.onNodeWithText("پیدا شده: 1").assertExists()

        activity.activity.onKeyDown(280, KeyEvent(KeyEvent.ACTION_DOWN, 280))
        activity.waitForIdle()

    }

    private val product = Product(
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
        rfidKey = 130290L
    )

    private fun epcGenerator(
        header: Int,
        filter: Int,
        partition: Int,
        company: Int,
        item: Long,
        serial: Long
    ): String {

        var tempStr = java.lang.Long.toBinaryString(header.toLong())
        val headerStr = String.format("%8s", tempStr).replace(" ".toRegex(), "0")
        tempStr = java.lang.Long.toBinaryString(filter.toLong())
        val filterStr = String.format("%3s", tempStr).replace(" ".toRegex(), "0")
        tempStr = java.lang.Long.toBinaryString(partition.toLong())
        val positionStr = String.format("%3s", tempStr).replace(" ".toRegex(), "0")
        tempStr = java.lang.Long.toBinaryString(company.toLong())
        val companynumberStr = String.format("%12s", tempStr).replace(" ".toRegex(), "0")
        tempStr = java.lang.Long.toBinaryString(item)
        val itemNumberStr = String.format("%32s", tempStr).replace(" ".toRegex(), "0")
        tempStr = java.lang.Long.toBinaryString(serial)
        val serialNumberStr = String.format("%38s", tempStr).replace(" ".toRegex(), "0")
        val epcStr =
            headerStr + positionStr + filterStr + companynumberStr + itemNumberStr + serialNumberStr // binary string of EPC (96 bit)

        tempStr = epcStr.substring(0, 64).toULong(2).toString(16)
        val epc0To64 = String.format("%16s", tempStr).replace(" ".toRegex(), "0")
        tempStr = epcStr.substring(64, 96).toULong(2).toString(16)
        val epc64To96 = String.format("%8s", tempStr).replace(" ".toRegex(), "0")

        return epc0To64 + epc64To96

    }

    private fun start() {
        activity.waitForIdle()
        Thread.sleep(1000)
        activity.waitForIdle()
    }
}