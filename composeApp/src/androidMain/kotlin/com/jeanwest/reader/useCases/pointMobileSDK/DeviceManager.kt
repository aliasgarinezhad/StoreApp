package com.jeanwest.reader.useCases.pointMobileSDK

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Created by NG on 2016-04-06.
 */
/**
 * FOR DeviceManager.java
 * This file is command functions define. if you want add function, have to add function define.
 */
@SuppressLint("MissingPermission")
class DeviceManager {
    protected var mSingleTag: Boolean = true
    protected var mUseMask: Boolean = false
    protected var mTimeout: Int = 0
    protected var mQuerySelected: Boolean = true
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDevice: BluetoothDevice? = null
    private var mBluetoothSocket: BluetoothSocket? = null
    private var mUuid: UUID? = null

    fun connectToBluetoothDevice(address: String?, uuid: UUID?) {
        try {
            val DEVICE = getBluetoothDevice(address)
            if (DEVICE != null) connectToBluetoothDevice(DEVICE, uuid)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun connectToBluetoothDevice(device: BluetoothDevice?, uuid: UUID?) {
        mBluetoothDevice = device
        mUuid = uuid
        disconnect()
        mConnectThread = ConnectThread()
        mConnectThread!!.start()
    }

    fun getBluetoothDevice(address: String?): BluetoothDevice? {
        if (mBluetoothAdapter != null) return mBluetoothAdapter!!.getRemoteDevice(address)
        return null
    }

    fun disconnect() {
        try {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                //connectThread.stop();
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        mConnectThread = null

        try {
            if (mConnectedThread != null) {
                mConnectedThread!!.cancel()
                //connectedThread.stop();
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        mConnectedThread = null
    }

    val isTryingConnect: Boolean
        get() = mConnectThread != null

    fun setOnDeviceEventListener(listener: OnDeviceEventListener?) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mOnDeviceEventListener = listener
    }

    fun byeBluetoothDevice() {
        sendData(DeviceProtocol.BYE)
    }

    /* send Command */
    fun sendCmdOpenInterface1() {
        sendData(DeviceProtocol.OPEN_INTERFACE_1)
    }

    fun sendCmdOpenInterface2() {
        sendData(DeviceProtocol.OPEN_INTERFACE_2)
    }

    @JvmOverloads
    fun sendCmdInventory(
        f_s: Int = if (mSingleTag) 1 else 0,
        f_m: Int = if (mUseMask) (if (mQuerySelected) 3 else 2) else 0,
        to: Int = mTimeout
    ) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_INVENT, intArrayOf(f_s, f_m, to)))
    }

    fun sendCmdStop() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_STOP, null as IntArray?))
    }

    fun sendHeartBeat(value: Int) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_HEART_BEAT, intArrayOf(value)))
    }

    /**
     * Select mask command
     *
     * @param n        : the index of mask table(0~7). (default = 0)
     * @param bits     : number of bits of the select mask pattern. (default = 0)
     * @param mem      : memory bank id of the tag to match for the select mask. (default = 0)
     * 0 - RESERVED
     * 1 - EPC
     * 2 - TID
     * 3 - USER
     * @param b_offset : bit offset of the memory bank of the tag to match for the select mask. (default = 0)
     * @param pattern  : Bit pattern of the memory in the tag to match for the select mask. Must be HEXA_STRING, MSB is starting bit. (default = 0)
     * @param target   : Target flag in the tag will be altered after select command. (default = 4)
     * @param action   : Flag setting option. (default = 4)
     */
    fun sendCmdSelectMask(
        n: Int,
        bits: Int,
        mem: Int,
        b_offset: Int,
        pattern: String?,
        target: Int,
        action: Int
    ) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_SEL_MASK,
                intArrayOf(n, bits, mem, b_offset),
                pattern,
                intArrayOf(target, action)
            )
        )
    }

    /* Access */
    fun sendSetSession(session: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_INVENT_PARAM,
                intArrayOf(session, DeviceProtocol.SKIP_PARAM, DeviceProtocol.SKIP_PARAM)
            )
        )
    }

    fun sendSetQValue(q: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_INVENT_PARAM,
                intArrayOf(DeviceProtocol.SKIP_PARAM, q, DeviceProtocol.SKIP_PARAM)
            )
        )
    }

    fun sendSetInventoryTarget(m_ab: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_INVENT_PARAM,
                intArrayOf(DeviceProtocol.SKIP_PARAM, DeviceProtocol.SKIP_PARAM, m_ab)
            )
        )
    }

    fun sendInventParam(session: Int, q: Int, m_ab: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_INVENT_PARAM,
                intArrayOf(session, q, m_ab)
            )
        )
    }

    fun sendGetInventParam() {
        sendGettingParameter(DeviceProtocol.CMD_INVENT_PARAM, "0")
    }

    fun sendSetSelectAction(bits: Int, mem: Int, b_offset: Int, pattern: String?, action: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_SEL_MASK,
                intArrayOf(0, bits, mem, b_offset),
                pattern,
                intArrayOf(DeviceProtocol.SKIP_PARAM, action)
            )
        )
    }

    fun setOpMode(singleTag: Boolean, useMask: Boolean, timeout: Int, querySelected: Boolean) {
        mSingleTag = singleTag
        mUseMask = useMask
        mTimeout = timeout
        mQuerySelected = querySelected
    }

    fun sendReadTag(w_count: Int, mem: Int, w_offset: Int, ACS_PWD: String?) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_READ_TAG_MEM,
                intArrayOf(w_count, mem, w_offset),
                ACS_PWD,
                intArrayOf(
                    if (mSingleTag) 1 else 0,
                    if (mUseMask) (if (mQuerySelected) 3 else 2) else 0,
                    mTimeout
                )
            )
        )
    }

    fun sendWriteTag(w_count: Int, mem: Int, w_offset: Int, ACS_PWD: String, wordPattern: String) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_WRITE_TAG_MEM,
                intArrayOf(w_count, mem, w_offset),
                arrayOf(wordPattern, ACS_PWD),
                intArrayOf(
                    if (mSingleTag) 1 else 0,
                    if (mUseMask) (if (mQuerySelected) 3 else 2) else 0,
                    mTimeout
                )
            )
        )
    }

    private fun convertLockIndex(enable: Boolean, index: Boolean): Int {
        return if (enable) (if (index) 1 else 0) else -1
    }

    fun sendLockTag(lockPattern: LockPattern, ACS_PWD: String?) {
        val user = convertLockIndex(
            lockPattern.enableUser,
            lockPattern.indexUser
        ) //( ( lockPattern.indexUser == false ) ? ( DeviceProtocol.SKIP_PARAM ) : ( lockPattern.enableUser ? 1 : 0 ) );
        val tid = convertLockIndex(
            lockPattern.enableTid,
            lockPattern.indexTid
        ) //( ( lockPattern.indexTid == false ) ? ( DeviceProtocol.SKIP_PARAM ) : ( lockPattern.enableTid ? 1 : 0 ) );
        val epc = convertLockIndex(
            lockPattern.enableUii,
            lockPattern.indexUii
        ) //( ( lockPattern.indexUii == false ) ? ( DeviceProtocol.SKIP_PARAM ) : ( lockPattern.enableUii ? 1 : 0 ) );
        val acs_pwd = convertLockIndex(
            lockPattern.enableAcsPwd,
            lockPattern.indexAcsPwd
        ) //( ( lockPattern.indexAcsPwd == false ) ? ( DeviceProtocol.SKIP_PARAM ) : ( lockPattern.enableAcsPwd ? 1 : 0 ) );
        val kill_pwd = convertLockIndex(
            lockPattern.enableKillPwd,
            lockPattern.indexKillPwd
        ) //( ( lockPattern.indexKillPwd == false ) ? ( DeviceProtocol.SKIP_PARAM ) : ( lockPattern.enableKillPwd ? 1 : 0 ) );

        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_LOCK_TAG_MEM,
                intArrayOf(user, tid, epc, acs_pwd, kill_pwd),
                ACS_PWD,
                intArrayOf(
                    if (mSingleTag) 1 else 0,
                    if (mUseMask) (if (mQuerySelected) 3 else 2) else 0,
                    mTimeout
                )
            )
        )
    }

    fun sendLockTag(lockMask: Int, lockEnable: Int, ACS_PWD: String?) {
        var mask: Boolean
        var enable: Boolean

        //---
        var bitFlag = 0x200 //0x02;
        mask = (lockMask and bitFlag) == bitFlag
        enable = (lockEnable and bitFlag) == bitFlag
        val user = (if ((!mask)) (DeviceProtocol.SKIP_PARAM) else (if (enable) 1 else 0))

        //---
        bitFlag = 0x80 //0x08;
        mask = (lockMask and bitFlag) == bitFlag
        enable = (lockEnable and bitFlag) == bitFlag
        val tid = (if ((!mask)) (DeviceProtocol.SKIP_PARAM) else (if (enable) 1 else 0))

        //---
        bitFlag = 0x20
        mask = (lockMask and bitFlag) == bitFlag
        enable = (lockEnable and bitFlag) == bitFlag
        val epc = (if ((!mask)) (DeviceProtocol.SKIP_PARAM) else (if (enable) 1 else 0))

        //---
        bitFlag = 0x08 //0x80;
        mask = (lockMask and bitFlag) == bitFlag
        enable = (lockEnable and bitFlag) == bitFlag
        val acs_pwd = (if ((!mask)) (DeviceProtocol.SKIP_PARAM) else (if (enable) 1 else 0))

        //---
        bitFlag = 0x02 //0x200;
        mask = (lockMask and bitFlag) == bitFlag
        enable = (lockEnable and bitFlag) == bitFlag
        val kill_pwd = (if ((!mask)) (DeviceProtocol.SKIP_PARAM) else (if (enable) 1 else 0))

        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_LOCK_TAG_MEM,
                intArrayOf(user, tid, epc, acs_pwd, kill_pwd),
                ACS_PWD,
                intArrayOf(
                    if (mSingleTag) 1 else 0,
                    if (mUseMask) (if (mQuerySelected) 3 else 2) else 0,
                    mTimeout
                )
            )
        )
    }

    fun sendKillTag(killPwd: String?) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_KILL_TAG,
                killPwd,
                intArrayOf(
                    if (mSingleTag) 1 else 0,
                    if (mUseMask) (if (mQuerySelected) 3 else 2) else 0,
                    mTimeout
                )
            )
        )
    }

    /* Other Command */
    fun sendGetVersion() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_VERSION))
    }

    fun sendSetDefaultParameter() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_DEF_PARAM))
    }

    fun sendGettingParameter(cmd: String, p: String) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_PARAM, arrayOf(cmd, p)))
    }

    fun sendSettingTxPower(a: Int) {
        if (a == -1) {
            val cmd = DeviceProtocol.CMD_SET_TX_POWER + ",-1"
            sendData(DeviceProtocol.makeProtocol(cmd))
        } else {
            sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_TX_POWER, intArrayOf(a)))
        }
    }

    fun sendGetMaxPower() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_MAX_POWER))
    }

    fun sendSettingTxCycle(on: Int, off: Int) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_TX_CYCLE, intArrayOf(on, off)))
    }

    fun sendChangeChannelState(n: Int, f_e: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_CHANGE_CH_STATE,
                intArrayOf(n, f_e)
            )
        )
    }

    fun sendSettingCountry(code: Int) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_CHANGE_CH_STATE, intArrayOf(code)))
    }

    fun sendGettingCountry() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_COUNTRY_CAP))
    }

    fun sendSetLockTagMemStatePerm(mem_id: Int, f_l: Int, ACS_PWD: String?) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_SET_LOCK_TAG_MEM,
                intArrayOf(mem_id, f_l),
                ACS_PWD,
                intArrayOf(
                    if (mSingleTag) 1 else 0,
                    if (mUseMask) (if (mQuerySelected) 3 else 2) else 0,
                    mTimeout
                )
            )
        )
    }

    fun sendPauseTx() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_PAUSE_TX))
    }

    fun sendStatusReporting(f_link: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_STATUS_REPORT,
                intArrayOf(f_link)
            )
        )
    }

    fun sendInventoryReportingFormat(f_time: Int, f_rssi: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_INVENT_REPORT_FORMAT,
                intArrayOf(f_time, f_rssi)
            )
        )
    }

    fun sendDislink() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_DISLINK))
    }

    //---- R900 Controls
    fun sendUploadingTagData(index: Int, count: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_UPLOAD_TAG_DATA,
                intArrayOf(index, count)
            )
        )
    }

    fun sendClearingTagData() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_CLEAR_TAG_DATA))
    }

    fun sendAlertReaderStatus(
        f_link: Int,
        f_trigger: Int,
        f_lowbat: Int,
        f_autooff: Int,
        f_pwr: Int
    ) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_ALERT_READER_STATUS,
                intArrayOf(f_link, f_trigger, f_lowbat, f_autooff, f_pwr)
            )
        )
    }

    fun sendGettingStatusWord() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_STATUS_WORD))
    }

    fun sendSettingBuzzerVolume(volume: Int, f_nv: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_SET_BUZZER_VOL,
                intArrayOf(volume, f_nv)
            )
        )
    }

    fun sendSettingVibration(on: Int, f_nv: Int) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_VIBRATION, intArrayOf(on, f_nv)))
    }

    fun sendBeep(f_on: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_BEEP,
                intArrayOf(f_on)
            )
        )
    }

    fun sendSettingAutoPowerOffDelay(delay: Int, f_nv: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_SET_AUTO_POWER_OFF_DELAY,
                intArrayOf(delay, f_nv)
            )
        )
    }

    fun sendGettingBatteryLevel(f_ext: Int) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_BATT_LEVEL, intArrayOf(f_ext)))
    }

    fun sendReportingBatteryState(f_report: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_REPORT_BATT_STATE,
                intArrayOf(f_report)
            )
        )
    }

    fun sendTurningReaderOff() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_TURN_READER_OFF))
    }

    //<-- eric 2012.12.12
    fun sendDeviceProprietary(mode: Int, password: String?, name: String?) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_READER_PROPRIETARY,
                intArrayOf(mode),
                password,
                name
            )
        )
    }

    //<-- eric 2013.10.18
    fun sendGettingBTmacAddress() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_BT_MAC_ADDRESS))
    }

    /* Expanded command */ /* Send Command */
    fun sendTextCommand(sCmd: String?) {
        sendData(DeviceProtocol.makeProtocol(sCmd!!))
    }

    /* COMMON */
    fun sendCmdGetBluetoothDeviceName() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_BT_NAME))
    }

    //--> eric 2012.12.12
    fun sendCmdSetClearReport(enable: Int) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_CLEAR_REPORT, intArrayOf(enable)))
    }

    //--> eric 2013.10.18
    fun sendCmdGetRFIDBoardInfo() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_HW_BOARD_VERSION))
    }

    fun sendCmdOEMInfo() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_OEM_INFO))
    }

    fun sendCmdGetLocalDataCount() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_LOCAL_DATA_COUNT))
    }

    /* UHF RFID */
    fun sendCmdRfidTagFocus(enable: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_SET_TAGFOCUS,
                intArrayOf(enable)
            )
        )
    }

    fun sendCmdRfidFastID(enable: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_SET_FASTID,
                intArrayOf(enable)
            )
        )
    }

    fun sendLinkProfile(a: Int) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_LINK_PROFILE, intArrayOf(a)))
    }

    fun sendSingleTagSearch(length: Int, ACS_EPC: String?, threshold: Int, step: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_TAG_SINGLE_SEARCH,
                intArrayOf(length),
                ACS_EPC,
                intArrayOf(threshold, step)
            )
        )
    }

    fun sendMultiTagSearch() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH))
    }

    fun sendSetMultiTagList(index: Int, length: Int, ACS_EPC: String?) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH_SET_LIST,
                intArrayOf(index, length), ACS_EPC
            )
        )
    }

    fun sendGetMultiTagList() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH_GET_LIST))
    }

    fun sendGetMultiTagList(index: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH_GET_LIST,
                intArrayOf(index)
            )
        )
    }

    fun sendClearMultiTagList(index: Int, length: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH_CLEAR_LIST,
                intArrayOf(index, length)
            )
        )
    }

    fun sendClearAllMultiTagList() {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH_CLEAR_LIST,
                intArrayOf(0, 50)
            )
        )
    }

    fun sendWildcardTagSearch(length: Int, ACS_EPC: String?) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_TAG_WILDCARD_SEARCH,
                intArrayOf(length), ACS_EPC
            )
        )
    }

    fun sendSetRFIDDataFormat(index: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_SET_DATA_FORMAT,
                intArrayOf(index)
            )
        )
    }

    fun sendSetRFIDFixDataFormat(index: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_SET_FIX_DATA_FORMAT,
                intArrayOf(index)
            )
        )
    }

    fun sendSetRFIDPrefix(index: Int) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_SET_PREFIX, intArrayOf(index)))
    }

    fun sendSetRFIDSuffix1(index: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_SET_SUFFIX1,
                intArrayOf(index)
            )
        )
    }

    fun sendSetRFIDSuffix2(index: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_SET_SUFFIX2,
                intArrayOf(index)
            )
        )
    }

    fun sendBlockWrite(
        length: Int,
        membank: Int,
        offset: Int,
        writeData: String,
        password: String,
        readMode: Int,
        select: Int,
        timeout: Int,
        blockMode: Int
    ) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_TAG_BLOCK_WRITE,
                intArrayOf(length, membank, offset),
                arrayOf(writeData, password),
                intArrayOf(readMode, select, timeout, blockMode)
            )
        )
    }

    fun sendBlockErase(
        length: Int,
        membank: Int,
        offset: Int,
        password: String,
        readMode: Int,
        select: Int,
        timeout: Int,
        blockMode: Int
    ) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_RFID_TAG_BLOCK_WRITE,
                intArrayOf(length, membank, offset),
                arrayOf(password),
                intArrayOf(readMode, select, timeout, blockMode)
            )
        )
    }

    /* Scanner */
    fun sendGetScannerType() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_GET_TYPE))
    }

    fun sendGetScannerVersion() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_GET_VERSION))
    }

    fun sendCmdScannerScanStart() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_SCAN_START))
    }

    fun sendCmdScannerScanStop() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_SCAN_STOP))
    }

    fun sendCmdScannerSetDefault() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_DEFAULT))
    }

    fun sendCmdSetScannerRegister(sOpCode: String?, nRegister: Int) {
        sendData(
            DeviceProtocol.makeProtocol(
                DeviceProtocol.CMD_SCANNER_PARAMETER,
                sOpCode,
                intArrayOf(nRegister)
            )
        )
    }

    class LockPattern {
        /*
         * public short lockMask; public short lockEnable;
         */
        var enableUser: Boolean = false
        var enableTid: Boolean = false
        var enableUii: Boolean = false
        var enableAcsPwd: Boolean = false
        var enableKillPwd: Boolean = false

        var indexUser: Boolean = false
        var indexTid: Boolean = false
        var indexUii: Boolean = false
        var indexAcsPwd: Boolean = false
        var indexKillPwd: Boolean = false

        var lockPerma: Boolean = false
    }

    private open inner class ConnectThread : Thread() {
        init {
            if (mBluetoothDevice != null) {
                try {
                    mBluetoothSocket = mBluetoothDevice!!.createRfcommSocketToServiceRecord(mUuid)
                } catch (e: IOException) {
                    mOnDeviceEventListener!!.onBtDeviceConnectFail()
                }
            } else {
                Log.d("ConnectThread", "BluetoothDevice is null")
                mOnDeviceEventListener!!.onBtDeviceConnectFail()
            }
        }

        override fun run() {
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter!!.cancelDiscovery()
            }

            if (mBluetoothSocket != null) {
                try {
                    mBluetoothSocket!!.connect()
                } catch (connectException: IOException) {
                    try {
                        mOnDeviceEventListener!!.onBtDeviceConnectFail()
                        mBluetoothSocket!!.close()
                    } catch (closeException: IOException) {
                    }
                    mConnectThread = null
                    return
                }

                mConnectedThread = ConnectedThread(mBluetoothSocket!!)
                if (mConnectedThread!!.isInitOk) {
                    mConnectedThread!!.start()
                    Log.d("ConnectThread", "Connected")
                    mOnDeviceEventListener!!.onBtDeviceConnected()
                }
            } else {
                Log.d("ConnectThread", "BluetothSocket is null.")
            }
            mConnectThread = null
        }

        open fun cancel() {
        }
    }

    private inner class ConnectedThread(socket: BluetoothSocket) :
        ConnectThread() {

        val bluetoothSocket = socket
        private val inputStream: InputStream?
        private val outputStream: OutputStream?
        var isInitOk: Boolean
            private set

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            recvPacketParser.reset()
            isInitOk = false
            try {
                tmpIn = socket.getInputStream()
                tmpOut = socket.getOutputStream()
                isInitOk = true
            } catch (e: IOException) {
                Log.d("ConnectedThread", "BluetothSocket is null")
                isInitOk = false
            }
            inputStream = tmpIn
            outputStream = tmpOut
        }

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (isInitOk) {
                try {
                    bytes = inputStream!!.read(buffer)
                    recvPacketParser.pushPacket(buffer, bytes)
                    if (mOnDeviceEventListener != null) {
                        mOnDeviceEventListener!!.onNotifyDataReceive()
                    }
                } catch (e: IOException) {
                    Log.d("ConnectedThread", "[Bluetooth Socket] Read Fail")
                    break
                }
            }
        }

        /* Call this from the main Activity to send data to the remote device */
        fun write(bytes: ByteArray?) {
            try {
                outputStream!!.write(bytes)
            } catch (e: IOException) {
                Log.d("ConnectedThread", "[Bluetooth Socket] Write Fail")
            }
        }

        /* Call this from the main Activity to shutdown the connection */
        override fun cancel() {
            try {
                Log.d("ConnectedThread", "[cancel] bluetoothSocket.close")
                isInitOk = false
                bluetoothSocket.close()
            } catch (e: IOException) {
            }
        }
    }

    companion object {
        val recvPacketParser: RecvPacketParser = RecvPacketParser()
        private var mOnDeviceEventListener: OnDeviceEventListener? = null
        private var mConnectThread: ConnectThread? = null
        private var mConnectedThread: ConnectedThread? = null
        private fun sendData(bytes: ByteArray) {
            if (mConnectedThread != null) {
                mConnectedThread!!.write(bytes)
            } else {
                Log.d("DeviceManager", "[sendData] Write Fail")
                mOnDeviceEventListener!!.onNotifyDataWriteFail()
            }
        }
    }
}
