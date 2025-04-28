package com.jeanwest.reader.useCases

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import com.jeanwest.reader.R
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceManager
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState
import com.jeanwest.reader.useCases.pointMobileSDK.OnDeviceEventListener
import com.jeanwest.reader.useCases.pointMobileSDK.RecvPacketParser
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.IUHF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import java.util.UUID

class RFID(var context: Context, var state: SnackbarHostState, var scanTrigger: () -> Unit) :
    OnDeviceEventListener {

    var tids = mutableListOf<String>()
    private lateinit var rf: RFIDWithUHFUART
    private lateinit var deviceManager: DeviceManager
    var scanningPower = 30
    var scanning by mutableStateOf(false)
    var distance by mutableFloatStateOf(1f)
    var epcs = mutableStateListOf<String>()
    private var scanningJob: Job? = null
    private var stopScan = false
    var matchedEpcTable = mutableListOf<String>()
    private var matchedNumber by mutableIntStateOf(0)
    var scannedNumber by mutableIntStateOf(0)
    private var scannerType = "Chainway"
    private var rfIsSupported = false
    private var bufferPM85 = mutableListOf<UHFTAGInfo>()
    private var putDataIntoBufferPM85 = false
    private var readingDataFromBufferPM85 = false
    var writeTestEPC = ""
    private var beepJob: Job? = null
    var inputEPCs = mutableListOf<String>()
    var justFindInputEPCs = false
    var mocEpcsForTestCode = mutableListOf("")
    val tagInfos = mutableListOf<UHFTAGInfo>()

    init {

        when (Build.MODEL) {
            context.getString(R.string.EXARK) -> {
                rf = RFIDWithUHFUART.getInstance()
                rfIsSupported = true
                scannerType = "Chainway"
            }

            context.getString(R.string.chainway) -> {
                rf = RFIDWithUHFUART.getInstance()
                rfIsSupported = true
                scannerType = "Chainway"
            }

            context.getString(R.string.new_chainway) -> {
                rf = RFIDWithUHFUART.getInstance()
                rfIsSupported = true
                scannerType = "Chainway"
            }

            context.getString(R.string.chainwayCapital) -> {
                rf = RFIDWithUHFUART.getInstance()
                rfIsSupported = true
                scannerType = "Chainway"
            }

            context.getString(R.string.point_mobile) -> {
                scannerType = "PM85"
                rfIsSupported = true
                deviceManager =
                    DeviceManager()
                deviceManager.setOnDeviceEventListener(this)
                deviceManager.sendCmdOpenInterface1()
                deviceManager.sendSetDefaultParameter()
                deviceManager.sendSettingTxPower(30)
                deviceManager.sendSettingBuzzerVolume(0, 0)
            }

            context.getString(R.string.point_mobile2) -> {
                scannerType = "PM85"
                rfIsSupported = true
                deviceManager =
                    DeviceManager()
                deviceManager.setOnDeviceEventListener(this)
                deviceManager.sendCmdOpenInterface1()
                deviceManager.sendSetDefaultParameter()
                deviceManager.sendSettingTxPower(30)
                deviceManager.sendSettingBuzzerVolume(0, 0)
            }

            else -> {
                rfIsSupported = false
                scannerType = "test"
            }
        }
    }

    fun enable() {

        val frequency: Int
        val rfLink = 2

        when (Build.MODEL) {
            context.getString(R.string.EXARK) -> {
                frequency = 0x08
                rfIsSupported = true
                scannerType = "Chainway"
            }

            context.getString(R.string.chainway) -> {
                frequency = 0x04
                rfIsSupported = true
                scannerType = "Chainway"
            }

            context.getString(R.string.chainwayCapital) -> {
                frequency = 0x04
                rfIsSupported = true
                scannerType = "Chainway"
            }

            context.getString(R.string.new_chainway) -> {
                frequency = 0x04
                rfIsSupported = true
                scannerType = "Chainway"
            }

            context.getString(R.string.point_mobile) -> {
                frequency = 0x04
                scannerType = "PM85"
                rfIsSupported = true
            }

            context.getString(R.string.point_mobile2) -> {
                frequency = 0x04
                scannerType = "PM85"
                rfIsSupported = true
            }

            else -> {
                rfIsSupported = false
                return
            }
        }
        if (scannerType == "Chainway") {
            chainwayInit(frequency, rfLink)
            setEpcMode()
        } else {
            pm85Init()
        }
    }

    fun setTIDMode() {
        if (scannerType == "Chainway") {
            rf.setEPCAndTIDMode()
        }
    }

    fun setNonTIDMode() {
        if (scannerType == "Chainway") {
            rf.setEPCMode()
        }
    }

    @SuppressLint("MissingPermission")
    private fun pm85Init() {

        val sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var deviceMacAddress = ""
        var deviceName = ""

        if (!bluetoothAdapter.isEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                state.showSnackbar(
                    "لطفا ابتدا بلوتوث دستگاه را روشن کنید سپس برنامه را باز کنید.",
                    null,
                    duration = SnackbarDuration.Long
                )
            }
            rfIsSupported = false
            return
        }

        bluetoothAdapter?.bondedDevices?.forEach {
            if (it.name != null) {
                deviceName = it.name.lowercase(Locale.getDefault())
                if (deviceName.isNotEmpty()) {
                    if (deviceName.contains("rf")) {
                        deviceMacAddress = it.address
                        deviceName = it.name
                    }
                }
            }
        }

        if (deviceMacAddress == "") {
            rfIsSupported = false
            CoroutineScope(Dispatchers.Default).launch {
                state.showSnackbar(
                    "مشکلی در سخت افزار ار اف ای دی پیش آمده است",
                    null,
                    duration = SnackbarDuration.Long
                )
            }
        }

        DeviceState.Device.Bluetooth.setConnectedDevice(deviceMacAddress, deviceName)
        deviceManager.connectToBluetoothDevice(deviceMacAddress, sppUuid)
    }

    private fun chainwayInit(frequency: Int, rfLink: Int) {
        for (i in 0..3) {

            if (rf.init()) {
                break
            } else if (i == 2) {

                CoroutineScope(Dispatchers.Default).launch {
                    state.showSnackbar(
                        "0مشکلی در سخت افزار ار اف ای دی پیش آمده است",
                        null,
                        duration = SnackbarDuration.Long
                    )
                }
                rfIsSupported = false
                return
            } else {
                rf.free()
            }
        }

        for (i in 0..3) {

            if (rf.setFrequencyMode(frequency)) {
                break
            } else if (i == 2) {

                CoroutineScope(Dispatchers.Default).launch {
                    state.showSnackbar(
                        "1مشکلی در سخت افزار ار اف ای دی پیش آمده است",
                        null,
                        duration = SnackbarDuration.Long
                    )
                }
                rfIsSupported = false
                return
            }
        }

        for (i in 0..3) {

            if (rf.setRFLink(rfLink)) {
                break
            } else if (i == 2) {

                CoroutineScope(Dispatchers.Default).launch {
                    state.showSnackbar(
                        "مشکلی در سخت افزار ار اف ای دی پیش آمده است",
                        null,
                        duration = SnackbarDuration.Long
                    )
                }
                rfIsSupported = false
                return
            }
        }
        setEpcMode()
    }

    fun disconnect() {

        if (rfIsSupported) {

            if (scannerType == "Chainway") {
                rf.free()
            } else {
                deviceManager.byeBluetoothDevice()
                SystemClock.sleep(500)
                DeviceState.Device.Bluetooth.disconnect()
                deviceManager.disconnect()
            }
        }
    }

    private fun setEpcMode(): Boolean {
        for (i in 0..3) {
            if (rf.setEPCMode()) {
                return true
            }
        }
        showLog("مشکلی در سخت افزار ار اف ای دی پیش آمده است", state)
        return false
    }

    private fun changePower(power: Int): Boolean {

        if (scannerType == "Chainway") {
            if (rf.power != power) {
                for (i in 0..3) {
                    if (rf.setPower(power)) {
                        return true
                    }
                }
                showLog("مشکلی در سخت افزار ار اف ای دی پیش آمده است", state)
                return false
            } else {
                return true
            }
        } else if (scannerType == "PM85") {
            deviceManager.sendSettingBuzzerVolume(0, 0)
            deviceManager.sendSettingTxPower(power - 30)
            return true
        } else {
            return true
        }
    }

    fun startBulkScan(
        justFindInputEPCs: Boolean = false,
        inputEPCs: List<String> = listOf(),
    ) {

        this.justFindInputEPCs = justFindInputEPCs
        this.inputEPCs.clear()
        this.inputEPCs.addAll(inputEPCs)

        var beep: ToneGenerator? = null
        try {
            beep = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            showLog("مشکلی در پخش صدای دستگاه به وجود آمده است.", state)
        }

        scanningJob = CoroutineScope(Dispatchers.IO).launch {
            scanning = true
            stopScan = false
            if (!changePower(scanningPower)) {
                scanning = false
                return@launch
            }
            var previousScannedEpcsNumber = epcs.size

            startScan()
            while (!stopScan) {

                var uhfTagInfo: UHFTAGInfo?
                while (true) {
                    uhfTagInfo = readBuffer()
                    if (uhfTagInfo != null) {
                        if (uhfTagInfo.epc.startsWith("30") ||
                            uhfTagInfo.epc.startsWith("1") ||
                            uhfTagInfo.epc.startsWith("00") ||
                            uhfTagInfo.epc.startsWith("22")
                        ) {
                            Log.e("rfidInputEpcs", uhfTagInfo.epc)
                            if (!justFindInputEPCs || uhfTagInfo.epc in this@RFID.inputEPCs) {
                                Log.e("rfidInputEpcs", uhfTagInfo.epc)
                                epcs.add(uhfTagInfo.epc)
                            }
                        }
                    } else {
                        break
                    }
                }

                epcs = epcs.distinct().toMutableStateList()

                val speed = epcs.size - previousScannedEpcsNumber
                when {
                    speed > 100 -> {
                        beep?.startTone(ToneGenerator.TONE_CDMA_PIP, 700)
                    }

                    speed > 30 -> {
                        beep?.startTone(ToneGenerator.TONE_CDMA_PIP, 500)
                    }

                    speed > 10 -> {
                        beep?.startTone(ToneGenerator.TONE_CDMA_PIP, 300)
                    }

                    speed > 0 -> {
                        beep?.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                    }
                }
                previousScannedEpcsNumber = epcs.size
                scannedNumber = epcs.size
                delay(1000)
            }
            beep?.release()
            stopScan()
            scanning = false
            stopScan = false
        }
    }

    fun findNearbyTags() {

        //add moc epcs to epc table for write test
        epcs.addAll(mocEpcsForTestCode)
        mocEpcsForTestCode.clear()

        scanningJob = CoroutineScope(Dispatchers.IO).launch {
            scanning = true
            stopScan = false
            if (!changePower(scanningPower)) {
                scanning = false
                return@launch
            }
            startScan()
            while (!stopScan) {

                var uhfTagInfo: UHFTAGInfo?
                while (true) {
                    uhfTagInfo = readBuffer()
                    if (uhfTagInfo != null) {
                        epcs.add(uhfTagInfo.epc)
                    } else {
                        break
                    }
                }
                epcs = epcs.distinct().toMutableStateList()
                delay(100)
            }
            stopScan()
            scanning = false
            stopScan = false
        }
    }

    fun findNearbyTagsWithResult(onNewTagAdded: (tagInfo: UHFTAGInfo) -> Unit) {

        //add moc epcs to epc table for write test
        //epcs.addAll(mocEpcsForTestCode)
        //mocEpcsForTestCode.clear()

        scanningJob = CoroutineScope(Dispatchers.IO).launch {
            scanning = true
            stopScan = false
            if (!changePower(scanningPower)) {
                scanning = false
                return@launch
            }
            startScan()
            while (!stopScan) {

                var uhfTagInfo: UHFTAGInfo?
                while (true) {
                    uhfTagInfo = readBuffer()
                    if (uhfTagInfo != null && uhfTagInfo.tid !in tids) {
                        tagInfos.add(uhfTagInfo)
                        onNewTagAdded(uhfTagInfo)
                        tids.add(uhfTagInfo.tid)
                        epcs.add(uhfTagInfo.epc)
                        epcs = epcs.distinct().toMutableStateList()
                        tids = tids.distinct().toMutableList()
                    } else {
                        break
                    }
                }
                //delay(100)
            }
            stopScan()
            scanning = false
            stopScan = false
        }
    }

    private fun startBulkScanWithoutBeep() {
        scanningJob = CoroutineScope(Dispatchers.IO).launch {
            scanning = true
            stopScan = false
            if (!changePower(scanningPower)) {
                scanning = false
                return@launch
            }
            startScan()
            while (!stopScan) {

                var uhfTagInfo: UHFTAGInfo?
                while (true) {
                    uhfTagInfo = readBuffer()
                    if (uhfTagInfo != null) {
                        epcs.add(uhfTagInfo.epc)
                    } else {
                        break
                    }
                }

                delay(100)
            }
            stopScan()
            scanning = false
            stopScan = false
        }
    }

    fun stopScanning() {

        scanningJob?.let {
            if (it.isActive) {
                stopScan = true // cause scanning routine loop to stop
                runBlocking { it.join() }
            }
        }

        beepJob?.let {
            if (it.isActive) {
                scanning = false // cause beep to stop
                runBlocking { it.join() }
            }
        }
    }

    fun startFinding(product: Product) {

        scanningJob = CoroutineScope(Dispatchers.IO).launch {

            scanning = true
            stopScan = false
            if (!changePower(scanningPower)) {
                scanning = false
                return@launch
            }

            startScan()
            while (!stopScan) {

                var isFound = false
                val epcTable = mutableListOf<String>()
                var uhfTagInfo: UHFTAGInfo?

                while (true) {

                    uhfTagInfo = readBuffer()
                    if (uhfTagInfo != null) {
                        if (uhfTagInfo.epc.startsWith("30")
                            || uhfTagInfo.epc.startsWith("1")
                            || uhfTagInfo.epc.startsWith("00")
                        //|| uhfTagInfo.epc.startsWith("22")
                        ) {
                            epcs.add(uhfTagInfo.epc)
                            epcTable.add(uhfTagInfo.epc)
                        }
                    } else {
                        break
                    }
                }

                epcTable.forEach { epcString ->

                    epcDecoder(epcString)?.let { epcData ->

                        if (epcString.startsWith("30")) {

                            if ((epcData.company == 101 && epcData.item == product.rfidKey) ||
                                (epcData.company == 100 && epcData.item == product.primaryKey)
                            ) {
                                matchedEpcTable.add(epcString)
                                matchedEpcTable = matchedEpcTable.distinct().toMutableList()
                                isFound = true
                            }
                        } else if (epcString.startsWith("00")) {

                            if (epcData.styleCode in product.productCode && epcData.color == product.color && epcData.size == product.size) {
                                matchedEpcTable.add(epcString)
                                matchedEpcTable = matchedEpcTable.distinct().toMutableList()
                                isFound = true
                            }
                        } else if (epcString.startsWith("1")) {
                            if (epcData.searchCode in product.searchCodes) {
                                matchedEpcTable.add(epcString)
                                matchedEpcTable = matchedEpcTable.distinct().toMutableList()
                                isFound = true
                            }
                        }
                    }
                }

                when (scanningPower) {
                    30 -> {
                        if (isFound) {
                            distance = 0.7f
                            changePowerWhileScanning(20)
                        } else {
                            distance = 1f
                            delay(500)
                        }
                    }

                    20 -> {
                        if (isFound) {
                            distance = 0.5f
                            changePowerWhileScanning(10)
                        } else {
                            distance = 0.7f
                            changePowerWhileScanning(30)
                        }
                    }

                    10 -> {
                        if (isFound) {
                            distance = 0.2f
                            changePowerWhileScanning(5)
                        } else {
                            distance = 0.5f
                            changePowerWhileScanning(20)
                        }
                    }

                    5 -> {
                        if (isFound) {
                            distance = 0.05f
                            delay(500)
                        } else {
                            distance = 0.2f
                            changePowerWhileScanning(10)
                        }
                    }
                }

                matchedNumber = matchedEpcTable.size
                delay(1000)
            }
            stopScan()
            scanning = false
            stopScan = false
        }

        beepJob = CoroutineScope(Dispatchers.IO).launch {
            beep()
        }
    }

    private suspend fun beep() {

        val beep = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        while (scanning) {
            when (distance) {

                0.7f -> {
                    beep.startTone(ToneGenerator.TONE_PROP_BEEP)
                    delay(
                        300
                    )
                    beep.stopTone()
                }

                0.5f -> {
                    beep.startTone(ToneGenerator.TONE_PROP_BEEP)
                    delay(150)
                    beep.stopTone()
                }

                0.2f -> {
                    beep.startTone(ToneGenerator.TONE_PROP_BEEP)
                    delay(100)
                    beep.stopTone()
                }

                0.05f -> {
                    beep.startTone(ToneGenerator.TONE_PROP_BEEP)
                    delay(50)
                    beep.stopTone()
                }
            }
        }
        beep.release()
    }

    private suspend fun changePowerWhileScanning(power: Int) {

        if (rfIsSupported) {
            scanningPower = power
            stopScan()
            delay(100)
            if (!changePower(scanningPower)) {
                return
            }
            delay(100)
            Log.e("p85", "power changed to $scanningPower")
            startScan()
            delay(500)
        }
    }

    fun write(productEPC: String, tagFilter: String): Boolean {

        if (!changePower(scanningPower)) {
            scanning = false
            return false
        }

        when (scannerType) {
            "Chainway" -> {
                for (k in 0..15) {
                    if (
                        rf.writeData(
                            "00000000",
                            IUHF.Bank_EPC,
                            32,
                            96,
                            tagFilter,
                            IUHF.Bank_EPC,
                            2,
                            6,
                            productEPC
                        )
                    ) {
                        return true
                    }
                }
                return false
            }

            "PM85" -> {
                val scanningJobTemp = CoroutineScope(Dispatchers.IO).launch {
                    delay(100)
                    deviceManager.sendCmdSelectMask(0, 96, 1, 32, tagFilter, 4, 1)
                    deviceManager.setOpMode(true, true, 0, true)
                    deviceManager.sendWriteTag(6, 1, 2, "00000000", productEPC)
                    deviceManager.setOpMode(false, false, 0, true)
                    delay(900)
                }
                runBlocking { scanningJobTemp.join() }
                return true
            }

            else -> {
                writeTestEPC = productEPC
                return true
            }
        }
    }

    fun writeWithTIDFilter(productEPC: String, tagFilter: String): Boolean {

        if (!changePower(scanningPower)) {
            scanning = false
            return false
        }

        when (scannerType) {
            "Chainway" -> {
                for (k in 0..15) {
                    if (
                        rf.writeData(
                            "00000000",
                            IUHF.Bank_TID,
                            0,
                            96,
                            tagFilter,
                            IUHF.Bank_EPC,
                            2,
                            6,
                            productEPC
                        )
                    ) {
                        return true
                    }
                }
                return false
            }

            else -> {
                writeTestEPC = productEPC
                return true
            }
        }
    }

    fun verify(productEPC: String): Boolean {

        when (scannerType) {
            "Chainway" -> {
                if (!changePower(scanningPower)) {
                    scanning = false
                    return false
                }

                for (k in 0..15) {

                    rf.readData("00000000", IUHF.Bank_EPC, 32, 96, productEPC, IUHF.Bank_EPC, 2, 6)
                        ?.let {
                            if (productEPC == it.lowercase()) {
                                return true
                            }
                        }
                }
                return false
            }

            "PM85" -> {

                epcs.clear()
                val scanningJobTemp = CoroutineScope(Dispatchers.IO).launch {
                    startBulkScanWithoutBeep()
                    delay(1000)
                    stopScanning()
                }
                runBlocking { scanningJobTemp.join() }
                return productEPC in epcs
            }

            else -> {
                return productEPC == writeTestEPC
            }
        }
    }

    private fun startScan() {

        if (rfIsSupported) {

            if (scannerType == "Chainway") {
                rf.startInventoryTag()
            } else {
                var uhfTagInfo: UHFTAGInfo?
                while (true) {
                    uhfTagInfo = readBuffer()
                    if (uhfTagInfo == null) {
                        break
                    }
                }
                deviceManager.sendCmdInventory(0, 0, 0)
            }
        }
    }

    fun stopScan() {
        if (rfIsSupported) {

            if (scannerType == "Chainway") {
                rf.stopInventory()
            } else {
                deviceManager.sendCmdStop()
            }
        }
    }

    fun scanEpcFilteredTag(tid: String): String? {
        return rf.readData("00000000", IUHF.Bank_TID, 0, 96, tid, IUHF.Bank_EPC, 2, 6)
    }

    private fun readBuffer(): UHFTAGInfo? {
        if (rfIsSupported) {

            return if (scannerType == "Chainway") {
                rf.readTagFromBuffer()
            } else {
                if (putDataIntoBufferPM85) {
                    return null
                }
                if (bufferPM85.isNotEmpty()) {
                    readingDataFromBufferPM85 = true
                    val data = bufferPM85[0]
                    bufferPM85.remove(bufferPM85[0])
                    readingDataFromBufferPM85 = false
                    data
                } else {
                    return null
                }
            }
        } else {
            return null
        }
    }

    override fun onNotifyDataReceive() {
        val packetParser: RecvPacketParser = DeviceManager.recvPacketParser
        while (true) {
            val parameter = packetParser.popPacket()
            Log.i("p85", "Recv :: [$parameter]")
            if (parameter != null) {
                Log.i("p85", "Recv :: [$parameter]")
                processPacket(parameter)
            } else {
                break
            }
        }
    }

    private fun processPacket(param: String?) {
        val data = param!!.lowercase()
        val cmd: String
        var value: String

        if (data.contains("=")) {
            cmd = data.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            value = data.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            when (cmd) {
                "\$trigger" -> if (value.contains(",")) {
                    value = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0]
                    when (value) {
                        "0" -> {}
                        "1" -> {
                            scanTrigger()
                        }

                        "2" -> {}
                        "3" -> {}
                    }
                }
            }
        } else if (scanning) {

            if (data.length > 8) {
                if (readingDataFromBufferPM85) {
                    return
                }
                val epc = data.substring(4, data.length - 4)
                putDataIntoBufferPM85 = true
                val uhfTagInfo = UHFTAGInfo()
                uhfTagInfo.epc = epc
                bufferPM85.add(uhfTagInfo)
                Log.i("p85", epc)
                putDataIntoBufferPM85 = false
            }
        }
    }

    override fun onNotifyDataWriteFail() {

    }

    override fun onBtDeviceConnected() {

    }

    override fun onBtDeviceConnectFail() {

    }
}