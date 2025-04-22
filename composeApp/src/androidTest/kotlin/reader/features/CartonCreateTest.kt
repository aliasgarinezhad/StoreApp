package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.features.carton.view.CartonCreate
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.testData.primaryLightEPCs
import com.jeanwest.reader.testData.jootiEPCs
import org.junit.Rule
import org.junit.Test

class CartonCreateTest {

    //RUN REFILL TEST BEFOR RUN THIS TEST(to get products to use in this test)
    @get:Rule
    val activity = createAndroidComposeRule<CartonCreate>()

    //Create carton with barcode
    @Test
    fun test() {

        //Clear previous scanned stuffs
        waitForFinishLoading()
        clearUserData()
        restart()



        activity.onAllNodesWithText("هنوز کالایی برای ایجاد کارتن اسکن نکرده اید")[0].assertExists()

        activity.onNodeWithText("RFID").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("بارکد").performClick()
        activity.waitForIdle()

        barcodeScan("123456")
        activity.onNodeWithText("مشخصات بارکد 123456 یافت نشد.").assertExists()
        activity.onNodeWithText("باشه").performClick()

        val products = createTestProducts().subList(0, 10)
        barcodeArrayScan(10, products)

        assert(activity.activity.viewModel.products.filter {
            it.scannedBarcodeNumber > 0
        }.size == 10)

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.viewModel.uiList[i].KBarCode)
                assertTextContains(activity.activity.viewModel.uiList[i].name)
                assertTextContains("سایز: " + activity.activity.viewModel.uiList[i].size)
                assertTextContains("اسکن: " + "2")
            }
        }
        //we can nottest api and create carton by test code because it will print in real warehouses
    }

    //Create carton by RFID
    @Test
    fun test2() {
        waitForFinishLoading()
        clearUserData()
        restart()

        activity.onAllNodesWithText("هنوز کالایی برای ایجاد کارتن اسکن نکرده اید")[0].assertExists()

        val epcs = mutableListOf<String>()
        epcs.addAll(primaryLightEPCs)
        epcs.addAll(jootiEPCs)
        specialEPCSScan(epcs)

        Thread.sleep(5000)

        //assert showing list with real scanned number
        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.viewModel.uiList[i].KBarCode)
                assertTextContains(activity.activity.viewModel.uiList[i].name)
                assertTextContains("سایز: " + activity.activity.viewModel.uiList[i].size)
                assertTextContains("اسکن: " + activity.activity.viewModel.uiList[i].scannedNumber)
            }
        }
        //we can nottest api and create carton by test code because it will print in real warehouses
    }

    //Recreate activity
    private fun restart() {
        activity.activity.runOnUiThread {
            activity.activity.recreate()
        }
        waitForFinishLoading()
    }

    private fun specialEPCSScan(epcs: MutableList<String>) {

        activity.activity.viewModel.rf.epcs.clear()

        epcs.forEach {
            activity.activity.viewModel.rf.epcs.add(it)
        }

        activity.activity.viewModel.scanTrigger()
        activity.waitForIdle()
        Thread.sleep(1000)
        activity.waitForIdle()
        activity.activity.viewModel.scanTrigger()
        activity.waitForIdle()
        waitForFinishLoading()
    }

    private fun epcScan(withDifference: Boolean, products: MutableList<Product>) {
        activity.activity.viewModel.rf.epcs.clear()
        activity.activity.viewModel.rf.epcs.add("30123456789")

        products.forEach {
            if (withDifference) {
                if (it.scannedNumber >= 3) {
                    for (i in 0 until it.scannedNumber - 3) {
                        activity.activity.viewModel.rf.epcs.add(
                            epcGenerator(
                                48,
                                0,
                                0,
                                101,
                                it.rfidKey,
                                i.toLong()
                            )
                        )
                    }
                } else if (it.scannedNumber == 2) {
                    for (i in 0 until it.scannedNumber + 3) {
                        activity.activity.viewModel.rf.epcs.add(
                            epcGenerator(
                                48,
                                0,
                                0,
                                101,
                                it.rfidKey,
                                i.toLong()
                            )
                        )
                    }
                } else {
                    for (i in 0 until it.scannedNumber + 1) {
                        activity.activity.viewModel.rf.epcs.add(
                            epcGenerator(
                                48,
                                0,
                                0,
                                101,
                                it.rfidKey,
                                i.toLong()
                            )
                        )
                    }
                }
            } else {
                for (i in 0 until it.scannedNumber) {
                    activity.activity.viewModel.rf.epcs.add(
                        epcGenerator(
                            48,
                            0,
                            0,
                            101,
                            it.rfidKey,
                            i.toLong()
                        )
                    )
                }
            }
        }

        activity.activity.viewModel.scanTrigger()
        activity.waitForIdle()
        Thread.sleep(1000)
        activity.waitForIdle()
        activity.activity.viewModel.scanTrigger()
        activity.waitForIdle()
        waitForFinishLoading()
    }

    //Create fake epc for create stockDraft by RFID
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

    //Product scanning simulation, get list of barcodes for input
    private fun barcodeArrayScan(number: Int, scannedProducts: MutableList<Product>) {
        for (i in 0 until number) {
            barcodeScan(scannedProducts[i].KBarCode)
            barcodeScan(scannedProducts[i].KBarCode)
        }
    }

    //Product scanning simulation, get single string as barcode for input
    private fun barcodeScan(barcode: String) {
        activity.activity.viewModel.barcode.barcode = barcode
        activity.activity.viewModel.barcode.scannedBarcodes.add(barcode)
        activity.activity.viewModel.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

    //Simulating real user delay ,current delay time is 0.2 second
    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.viewModel.loading || activity.activity.viewModel.rf.scanning) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }

    //Clear user scanned data by clearing source list
    private fun clearUserData() {
        val products = mutableListOf<Product>()
        products.addAll(activity.activity.viewModel.uiList)
        products.forEach {
            activity.activity.viewModel.clear(it)
            activity.waitForIdle()
        }
    }

    //Get the Refill products for use in test
    private fun createTestProducts(): MutableList<Product> {
        val memory = PreferenceManager.getDefaultSharedPreferences(activity.activity)
        val type = object : TypeToken<MutableList<Product>>() {}.type
        val products: MutableList<Product> = Gson().fromJson(
            memory.getString("refillProductsForTest", ""),
            type
        ) ?: mutableListOf()
        return products
    }
}
