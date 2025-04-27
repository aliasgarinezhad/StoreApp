package reader.features

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.jeanwest.reader.useCases.epcDecoder
import com.jeanwest.reader.view.doneColor
import com.jeanwest.reader.view.errorLight
import com.jeanwest.reader.view.warningColor
import com.jeanwest.reader.features.write.view.WriteTag
import org.junit.Rule
import org.junit.Test

/*
* this class test several write-tag scenarios occurred
* when user wants to write a tag with given product barcode.
* during test, RFID and barcode classes return
* moc parameters to the test class, and
* ensure that the activity encounter real situations.
* */

class WriteTest {

    @get:Rule
    var activity = createAndroidComposeRule<WriteTag>()

    @Test
    fun test1() {

        waitForFinishLoading()

        val serialNumberRange =
            activity.activity.viewModel.counterValue..activity.activity.viewModel.counterValue + 100

        for (i in serialNumberRange) {

            activity.activity.viewModel.rf.mocEpcsForTestCode.clear()

            activity.waitForIdle()

            barcodeScan("J24591001253003001")
            activity.waitForIdle()

            repeat(3) {
                activity.activity.viewModel.rf.mocEpcsForTestCode.add("E28001940010ab6c000000016")
            }
            activity.activity.viewModel.scanTrigger()

            waitForFinishLoading()
            activity.waitForIdle()

            Log.e("writeTest", activity.activity.viewModel.result)
            assert(epcDecoder(activity.activity.viewModel.rf.writeTestEPC)?.item == 273115L)
            assert(epcDecoder(activity.activity.viewModel.rf.writeTestEPC)?.serial == i)
            assert(activity.activity.viewModel.resultColor == doneColor)

        }
    }

    @Test
    fun test2() {

        waitForFinishLoading()

        activity.activity.viewModel.rf.mocEpcsForTestCode.clear()

        barcodeScan("J24591001253003001")
        activity.waitForIdle()
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("30001940010ab7c000000016")
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("30001940010ab7c000000017")
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("30001940010ab7c000000018")
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("E00052833627970110000000")
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("30001940010ab7c000000019")
        activity.activity.viewModel.scanTrigger()

        waitForFinishLoading()
        assert(epcDecoder(activity.activity.viewModel.rf.writeTestEPC)?.item == 273115L)
        assert(activity.activity.viewModel.resultColor == doneColor)
    }

    @Test
    fun test3() {

        waitForFinishLoading()

        activity.activity.viewModel.rf.mocEpcsForTestCode.clear()

        barcodeScan("64822109J-8010-F")

        activity.waitForIdle()
        activity.activity.viewModel.scanTrigger()

        waitForFinishLoading()

        assert(activity.activity.viewModel.result.contains("هیج تگی پیدا نشد. لطفا دستگاه را نزدیک تگ قرار دهید و دوباره تلاش کنید."))
        assert(activity.activity.viewModel.rf.writeTestEPC == "")
        assert(activity.activity.viewModel.resultColor == errorLight)

    }

    @Test
    fun test4() {

        waitForFinishLoading()

        activity.activity.viewModel.rf.mocEpcsForTestCode.clear()

        barcodeScan("J24591001253003001")
        activity.waitForIdle()
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("30001940010ab7c000000016")
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("00001940010ab7c000000017")
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("10001940010ab7c000000018")
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("30001940010ab7c000000019")
        activity.activity.viewModel.scanTrigger()

        waitForFinishLoading()

        Thread.sleep(10000)
        assert(activity.activity.viewModel.result.contains("تعداد تگ های پیدا شده بیشتر از یک است. لطفا تگ مورد نظرتان را جدا از بقیه قرار دهید و دوباره تلاش کنید."))

        assert(activity.activity.viewModel.rf.writeTestEPC == "")
        assert(activity.activity.viewModel.resultColor == errorLight)
    }

    @Test
    fun test5() {

        waitForFinishLoading()

        activity.activity.viewModel.rf.mocEpcsForTestCode.clear()

        barcodeScan("J24591001253003001")
        activity.waitForIdle()
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("E28001940010ab6c000000016")
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("E28001940010ab6c000000017")
        activity.activity.viewModel.scanTrigger()

        waitForFinishLoading()
        assert(activity.activity.viewModel.result.contains("تعداد تگ های خام پیدا شده بیشتر از یک است. لطفا تگ مورد نظرتان را جدا از بقیه قرار دهید و دوباره تلاش کنید."))

        assert(activity.activity.viewModel.rf.writeTestEPC == "")
        assert(activity.activity.viewModel.resultColor == errorLight)
    }

    @Test
    fun test6() {

        waitForFinishLoading()

        activity.activity.viewModel.rf.mocEpcsForTestCode.clear()

        barcodeScan("6482109J-8010-F")

        activity.waitForIdle()
        activity.activity.viewModel.rf.mocEpcsForTestCode.add("E28001940010ab6c000000016")
        activity.activity.viewModel.scanTrigger()

        waitForFinishLoading()

        assert(activity.activity.viewModel.result.contains("بارکد مورد نظر در سیستم تعریف نشده است. لطفا با پشتیبانی تماس بگیرید."))
        assert(activity.activity.viewModel.rf.writeTestEPC == "")
        assert(activity.activity.viewModel.resultColor == errorLight)
    }

    @Test
    fun test7() {
        showAlreadyWrittenWithSameEPCWarningTest("J24591001253003001", "30001940010ab6c000000000") // avakatan Type tag
        showAlreadyWrittenWithSameEPCWarningTest("J41531052265101001", "000025C5B39E0E8DA7A9B6C0") // JOOTIJEANS Type tag
        showAlreadyWrittenWithSameEPCWarningTest("1114528336279001", "111452833627900110000000") // JOOTIJEANS Type tag
    }

    @Test
    fun test8() {

        replaceTagEPCTest("J24591001253003001", "3000194000f0afc00003cd86", 273115L) // avakatan Type tag
        replaceTagEPCTest("J41531052265101001", "000025CFADAC53D41388F8F6", 316953L) // JOOTIJEANS Type tag
        replaceTagEPCTest("1114528336279001", "111452833632300110000000", 351L) // JOOTIJEANS Type tag
    }

    private fun showAlreadyWrittenWithSameEPCWarningTest(barcode: String, tag: String) {
        waitForFinishLoading()

        activity.activity.viewModel.rf.mocEpcsForTestCode.clear()

        barcodeScan(barcode)

        activity.waitForIdle()
        activity.activity.viewModel.rf.mocEpcsForTestCode.add(tag)
        activity.activity.viewModel.scanTrigger()

        waitForFinishLoading()

        assert(activity.activity.viewModel.result.contains("این تگ قبلا با همین بارکد رایت شده است"))
        assert(activity.activity.viewModel.rf.writeTestEPC == "")
        assert(activity.activity.viewModel.resultColor == warningColor)
    }

    private fun replaceTagEPCTest(barcode: String, tag: String, rfidKey: Long) {
        waitForFinishLoading()

        activity.activity.viewModel.rf.mocEpcsForTestCode.clear()

        barcodeScan(barcode)
        activity.waitForIdle()

        repeat(5) {
            activity.activity.viewModel.rf.mocEpcsForTestCode.add(tag)
        }
        activity.activity.viewModel.scanTrigger()

        waitForFinishLoading()

        activity.onNodeWithTag("confirm").performClick()

        waitForFinishLoading()

        assert(epcDecoder(activity.activity.viewModel.rf.writeTestEPC)?.item == rfidKey)
        assert(activity.activity.viewModel.resultColor == doneColor)
    }

    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.viewModel.loading) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }

    private fun barcodeScan(barcode: String) {
        activity.activity.viewModel.barcode.barcode = barcode
        activity.activity.viewModel.barcode.scannedBarcodes.add(barcode)
        activity.activity.viewModel.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }
}