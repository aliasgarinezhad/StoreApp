package reader.features

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.preference.PreferenceManager
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.features.refillStore.view.RefillManual
import com.jeanwest.reader.models.Product
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi
class RefillManualTest {

    @get:Rule
    val activity = createAndroidComposeRule<RefillManual>()

    //RUN REFILL TEST BEFOR RUN THIS TEST(to get products to use in this test)
    @Test
    fun test() {

        //clear scanned product by clear used list
        waitForFinishLoading()
        clearUserData()

        //The number of products to be compared
        /*val numberOfItems = 4

        //Get the Refill products for use in test
        val products = createTestProducts()

        //To make sure that the number of products received is more than test items
        assert(products.size > numberOfItems)

        //To make sure that clearUserData() fun worked correctly
        activity.onAllNodesWithText("هنوز کالایی برای ارسال اسکن نکرده اید")[0].assertExists()

        //Product scanning simulation
        barcodeArrayScan(numberOfItems, products)

         */
        barcodeArrayScan(0, mutableListOf(Product()))

        //Send products to store
        /*activity.onNodeWithText("ارسال به فروشگاه").performClick()
        activity.waitForIdle()

        //To make sure that Product scanning simulation worked correctly
        assert(activity.activity.products.filter {
            it.scannedBarcodeNumber > 0
        }.size == numberOfItems)

        activity.activity.products.filter {
            it.scannedBarcodeNumber > 0
        }.toMutableList().forEach {
            if (it.wareHouseNumber > 1) {
                assert(it.scannedBarcodeNumber == 2)
            } else {
                assert(it.scannedBarcodeNumber == 1)
            }
        }

        //Compare that the displayed information is equal to the original list
        val listSize = if (numberOfItems > 3) 3 else numberOfItems
        for (i in 0 until listSize) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("انبار: " + activity.activity.uiList[i].wareHouseNumber)
                assertTextContains("اسکن: " + if (activity.activity.uiList[i].wareHouseNumber > 1) 2 else 1)
            }
        }
        activity.onNodeWithTag("deleteButton").assertDoesNotExist()

        //Test back button performance
        activity.onNodeWithTag("back", true).performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("deleteButton").assertExists()

        //Test delete all button
        activity.onNodeWithTag("deleteButton").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("confirm").performClick()
        activity.waitForIdle()
        activity.onAllNodesWithText("هنوز کالایی برای ارسال اسکن نکرده اید")[0].assertExists()
        assert(activity.activity.uiList.size == 0)

        //Final Test with call api and without print label
        barcodeArrayScan(numberOfItems, products)
        activity.onNodeWithText("ارسال به فروشگاه").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("ارسال به فروشگاه").performClick()
        activity.waitForIdle()
        waitForFinishLoading()
        activity.onNodeWithTag("notConfirm").performClick()
        activity.waitForIdle()
        activity.onAllNodesWithText("هنوز کالایی برای ارسال اسکن نکرده اید")[0].assertExists()
        assert(activity.activity.uiList.size == 0)*/
    }

    //Product scanning simulation, get list of barcodes for input
    private fun barcodeArrayScan(number: Int, scannedProducts: MutableList<Product>) {

        val barcodes = listOf(
            "J33793510801003001",
            "6903322950548",
            "J33793510801003001",
            "6903322950593",
            "6903322950555",
            "J41773237801002001",
            "J41773237801004001",
            "J41773237801003001",
            "J41773237801003001",
            "J31773232999903040",
            "J31773232999903040",
            "J41773235801002040",
            "J31773232999901040",
            "J31773232999901040",
            "J31773232999901040",
            "J41733206898001001",
            "J41733206842004001",
            "J41733206842003001",
            "J32773235859002040",
            "J32773235859004040",
            "J32773235859002040",
            "J32773235859004040",
            "J32773235859003040",
            "J41773235801004040",
            "J41773235801005040",
            "J41773235801006040",
            "J41773235801001040",
            "J41773235801003040",
            "1228361335268001",
            "1228361335236001",
            "1228361335243001",
            "J42573768201002001",
            "J42573768253002001",
            "J43593593211004001",
            "1119611626171001",
            "1119611626171001",
            "J33771346801005040",
            "J33771346858004040",
            "J33571383201003040",
            "J33571383201003040",
            "J33771903801003040",
            "J33771903801006040",
            "J43571332291004040",
            "J43571332291002040",
            "J43571332217003040",
            "J43571332217003040",
            "J43571332259003040",
            "J43571332259003040",
            "J43593593211004001",
            "J43593525211004040",
            "J34531011217005001",
            "J31573901204002040",
            "J31573901201001040",
            "J31573901201005040",
            "J41573901262002040",
            "J31573902258002040",
            "J41573902282004040",
            "J41573902202004040",
            "J41573902262003040",
            "J41573902257002040",
            "J41573902222003040",
            "J41573902282001040",
            "J41573902250004040",
            "J41573902282002040",
            "J41773902869005040",
            "J41773902869004040",
            "J41773902852002040",
            "J31773902801003040",
            "J41781822803030001",
            "J41781822803028001",
            "J41781822803028001",
            "J41781845801027001",
            "J41781845801029001",
            "J41781845801030001",
            "J41789831851031001",
            "J41789831851031001",
            "J41789831803031001",
            "J42781852851030001",
            "J42781852851027001",
            "J42781852851032001",
            "J43781831852028001",
            "J43781831804027001",
            "J43781831804029001",
            "J43781831804029001",
            "J33581820259034001",
            "J33581820259034001",
            "J33581820259032001",
            "J33581820259036001",
            "J41581802204034002",
            "J41581802204038002",
            "J43591002273003001",
            "J43591002273003001",
            "J43591002273004001",
            "J43591002273004001",
            "J43591001229003001",
            "J43591001229003001",
            "J34591001204004001",
            "J34591001204004001",
            "J34591001204004001",
            "J34591001201004001",
            "J34591001201004001",
            "J14791001841001001",
            "J34791001800002001",
            "J34791001800002001",
            "J34791001801003001",
            "J34791001801002001",
            "J34791001801002001",
            "J24791001819001001",
            "J24791002819001001",
            "J34791002859001001",
            "J34791002804004001",
            "J34791002804004001",
            "J34791002894002001",
            "J34791002894002001",
            "J34791002801001001",
            "J34791002894003001",
            "J34791002894003001",
            "J34791002859004001",
            "J34791002859004001"
        )

        barcodes.forEach {
            barcodeScan(it)
        }
        /*for (i in 0 until number) {
            if (scannedProducts[i].wareHouseNumber > 1) {
                barcodeScan(scannedProducts[i].KBarCode)
                barcodeScan(scannedProducts[i].KBarCode)
            } else {
                barcodeScan(scannedProducts[i].KBarCode)
            }
        }*/
    }

    //Product scanning simulation, get single string as barcode for input
    private fun barcodeScan(barcode: String) {
        activity.activity.barcode.barcode = barcode
        activity.activity.barcode.scannedBarcodes.add(barcode)
        activity.activity.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

    //Simulating real user delay ,current delay time is 0.2 second
    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.loading) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }

    //Clear user scanned data by clearing source list
    private fun clearUserData() {
        val products = mutableListOf<Product>()
        products.addAll(activity.activity.products)
        products.forEach {
            if (it.scannedBarcodeNumber > 0) {
                activity.activity.clear(it)
                activity.waitForIdle()
            }
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