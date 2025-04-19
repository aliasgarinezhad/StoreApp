package com.jeanwest.reader.useCases.pointMobileSDK;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by NG on 2016-04-06.
 */

/**
 * FOR DeviceManager.java
 * This file is command functions define. if you want add function, have to add function define.
 **/
public class DeviceManager {

    private static final RecvPacketParser mRecvPacketParser = new RecvPacketParser();
    private static OnDeviceEventListener mOnDeviceEventListener;
    private static ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;
    protected boolean mSingleTag = true;
    protected boolean mUseMask = false;
    protected int mTimeout = 0;
    protected boolean mQuerySelected = true;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private UUID mUuid;

    public DeviceManager() {
        //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private static void sendData(byte[] bytes) {
        if (mConnectedThread != null) {
            mConnectedThread.write(bytes);
        } else {
            Log.d("DeviceManager", "[sendData] Write Fail");
            mOnDeviceEventListener.onNotifyDataWriteFail();
        }
    }

    public void connectToBluetoothDevice(String address, UUID uuid) {
        try {
            final BluetoothDevice DEVICE = getBluetoothDevice(address);
            if (DEVICE != null)
                connectToBluetoothDevice(DEVICE, uuid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void connectToBluetoothDevice(BluetoothDevice device, UUID uuid) {
        mBluetoothDevice = device;
        mUuid = uuid;
        disconnect();
        mConnectThread = new ConnectThread();
        mConnectThread.start();
    }

    public BluetoothDevice getBluetoothDevice(String address) {
        if (mBluetoothAdapter != null)
            return mBluetoothAdapter.getRemoteDevice(address);
        return null;
    }

    public void disconnect() {
        try {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                //connectThread.stop();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mConnectThread = null;

        try {
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                //connectedThread.stop();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mConnectedThread = null;
    }

    public boolean isTryingConnect() {
        return mConnectThread != null;
    }

    public final RecvPacketParser getRecvPacketParser() {
        return mRecvPacketParser;
    }

    public void setOnDeviceEventListener(OnDeviceEventListener listener) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mOnDeviceEventListener = listener;
    }

    public void byeBluetoothDevice() {
        sendData(DeviceProtocol.BYE);
    }

    /* send Command */
    public void sendCmdOpenInterface1() {
        sendData(DeviceProtocol.OPEN_INTERFACE_1);
    }

    public void sendCmdOpenInterface2() {
        sendData(DeviceProtocol.OPEN_INTERFACE_2);
    }

    public void sendCmdInventory() {
        sendCmdInventory(mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout);
    }

    public void sendCmdInventory(int f_s, int f_m, int to) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_INVENT, new int[]{f_s, f_m, to}));
    }

    public void sendCmdStop() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_STOP, (int[]) null));
    }

    public void sendHeartBeat(int value) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_HEART_BEAT, new int[]{value}));
    }

    /**
     * Select mask command
     *
     * @param n        : the index of mask table(0~7). (default = 0)
     * @param bits     : number of bits of the select mask pattern. (default = 0)
     * @param mem      : memory bank id of the tag to match for the select mask. (default = 0)
     *                 0 - RESERVED
     *                 1 - EPC
     *                 2 - TID
     *                 3 - USER
     * @param b_offset : bit offset of the memory bank of the tag to match for the select mask. (default = 0)
     * @param pattern  : Bit pattern of the memory in the tag to match for the select mask. Must be HEXA_STRING, MSB is starting bit. (default = 0)
     * @param target   : Target flag in the tag will be altered after select command. (default = 4)
     * @param action   : Flag setting option. (default = 4)
     */
    public void sendCmdSelectMask(int n, int bits, int mem, int b_offset, String pattern, int target, int action) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SEL_MASK, new int[]{n, bits, mem, b_offset}, pattern, new int[]{target, action}));
    }

    /* Access */
    public void sendSetSession(int session) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_INVENT_PARAM,
                new int[]{session, DeviceProtocol.SKIP_PARAM, DeviceProtocol.SKIP_PARAM}));
    }

    public void sendSetQValue(int q) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_INVENT_PARAM,
                new int[]{DeviceProtocol.SKIP_PARAM, q, DeviceProtocol.SKIP_PARAM}));
    }

    public void sendSetInventoryTarget(int m_ab) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_INVENT_PARAM,
                new int[]{DeviceProtocol.SKIP_PARAM, DeviceProtocol.SKIP_PARAM, m_ab}));
    }

    public void sendInventParam(int session, int q, int m_ab) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_INVENT_PARAM,
                new int[]{session, q, m_ab}));
    }

    public void sendGetInventParam() {
        sendGettingParameter(DeviceProtocol.CMD_INVENT_PARAM, "0");
    }

    public void sendSetSelectAction(int bits, int mem, int b_offset, String pattern, int action) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SEL_MASK,
                new int[]{0, bits, mem, b_offset}, pattern, new int[]{DeviceProtocol.SKIP_PARAM, action}));
    }

    public void setOpMode(boolean singleTag, boolean useMask, int timeout, boolean querySelected) {
        mSingleTag = singleTag;
        mUseMask = useMask;
        mTimeout = timeout;
        mQuerySelected = querySelected;
    }

    public void sendReadTag(int w_count, int mem, int w_offset, String ACS_PWD) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_READ_TAG_MEM,
                new int[]{w_count, mem, w_offset},
                ACS_PWD,
                new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout}));
    }

    public void sendWriteTag(int w_count, int mem, int w_offset, String ACS_PWD, String wordPattern) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_WRITE_TAG_MEM,
                new int[]{w_count, mem, w_offset},
                new String[]{wordPattern, ACS_PWD},
                new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout}));
    }

    private int convertLockIndex(boolean enable, boolean index) {
        return enable ? (index ? 1 : 0) : -1;
    }

    public void sendLockTag(LockPattern lockPattern, String ACS_PWD) {
        final int user = convertLockIndex(lockPattern.enableUser, lockPattern.indexUser);//( ( lockPattern.indexUser == false ) ? ( DeviceProtocol.SKIP_PARAM ) : ( lockPattern.enableUser ? 1 : 0 ) );
        final int tid = convertLockIndex(lockPattern.enableTid, lockPattern.indexTid);//( ( lockPattern.indexTid == false ) ? ( DeviceProtocol.SKIP_PARAM ) : ( lockPattern.enableTid ? 1 : 0 ) );
        final int epc = convertLockIndex(lockPattern.enableUii, lockPattern.indexUii);//( ( lockPattern.indexUii == false ) ? ( DeviceProtocol.SKIP_PARAM ) : ( lockPattern.enableUii ? 1 : 0 ) );
        final int acs_pwd = convertLockIndex(lockPattern.enableAcsPwd, lockPattern.indexAcsPwd);//( ( lockPattern.indexAcsPwd == false ) ? ( DeviceProtocol.SKIP_PARAM ) : ( lockPattern.enableAcsPwd ? 1 : 0 ) );
        final int kill_pwd = convertLockIndex(lockPattern.enableKillPwd, lockPattern.indexKillPwd);//( ( lockPattern.indexKillPwd == false ) ? ( DeviceProtocol.SKIP_PARAM ) : ( lockPattern.enableKillPwd ? 1 : 0 ) );

        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_LOCK_TAG_MEM,
                new int[]{user, tid, epc, acs_pwd, kill_pwd},
                ACS_PWD,
                new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout}));
    }

    public void sendLockTag(int lockMask, int lockEnable, String ACS_PWD) {
        int bitFlag;
        boolean mask;
        boolean enable;

        //---
        bitFlag = 0x200;//0x02;
        mask = (lockMask & bitFlag) == bitFlag;
        enable = (lockEnable & bitFlag) == bitFlag;
        final int user = ((!mask) ? (DeviceProtocol.SKIP_PARAM) : (enable ? 1 : 0));

        //---
        bitFlag = 0x80;//0x08;
        mask = (lockMask & bitFlag) == bitFlag;
        enable = (lockEnable & bitFlag) == bitFlag;
        final int tid = ((!mask) ? (DeviceProtocol.SKIP_PARAM) : (enable ? 1 : 0));

        //---
        bitFlag = 0x20;
        mask = (lockMask & bitFlag) == bitFlag;
        enable = (lockEnable & bitFlag) == bitFlag;
        final int epc = ((!mask) ? (DeviceProtocol.SKIP_PARAM) : (enable ? 1 : 0));

        //---
        bitFlag = 0x08;//0x80;
        mask = (lockMask & bitFlag) == bitFlag;
        enable = (lockEnable & bitFlag) == bitFlag;
        final int acs_pwd = ((!mask) ? (DeviceProtocol.SKIP_PARAM) : (enable ? 1 : 0));

        //---
        bitFlag = 0x02;//0x200;
        mask = (lockMask & bitFlag) == bitFlag;
        enable = (lockEnable & bitFlag) == bitFlag;
        final int kill_pwd = ((!mask) ? (DeviceProtocol.SKIP_PARAM) : (enable ? 1 : 0));

        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_LOCK_TAG_MEM,
                new int[]{user, tid, epc, acs_pwd, kill_pwd},
                ACS_PWD,
                new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout}));
    }

    public void sendKillTag(String killPwd) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_KILL_TAG,
                killPwd,
                new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout}));
    }

    /* Other Command */
    public void sendGetVersion() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_VERSION));
    }

    public void sendSetDefaultParameter() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_DEF_PARAM));
    }

    public void sendGettingParameter(String cmd, String p) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_PARAM, new String[]{cmd, p}));
    }

    public void sendSettingTxPower(int a) {
        if (a == -1) {
            String cmd;
            cmd = DeviceProtocol.CMD_SET_TX_POWER + ",-1";
            sendData(DeviceProtocol.makeProtocol(cmd));
        } else {
            sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_TX_POWER, new int[]{a}));
        }
    }

    public void sendGetMaxPower() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_MAX_POWER));
    }

    public void sendSettingTxCycle(int on, int off) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_TX_CYCLE, new int[]{on, off}));
    }

    public void sendChangeChannelState(int n, int f_e) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_CHANGE_CH_STATE, new int[]{n, f_e}));
    }

    public void sendSettingCountry(int code) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_CHANGE_CH_STATE, new int[]{code}));
    }

    public void sendGettingCountry() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_COUNTRY_CAP));
    }

    public void sendSetLockTagMemStatePerm(int mem_id, int f_l, String ACS_PWD) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_LOCK_TAG_MEM,
                new int[]{mem_id, f_l},
                ACS_PWD,
                new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout}));
    }

    public void sendPauseTx() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_PAUSE_TX));
    }

    public void sendStatusReporting(int f_link) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_STATUS_REPORT,
                new int[]{f_link}));
    }

    public void sendInventoryReportingFormat(int f_time, int f_rssi) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_INVENT_REPORT_FORMAT,
                new int[]{f_time, f_rssi}));
    }

    public void sendDislink() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_DISLINK));
    }

    //---- R900 Controls
    public void sendUploadingTagData(int index, int count) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_UPLOAD_TAG_DATA,
                new int[]{index, count}));

    }

    public void sendClearingTagData() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_CLEAR_TAG_DATA));
    }

    public void sendAlertReaderStatus(int f_link, int f_trigger, int f_lowbat, int f_autooff, int f_pwr) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_ALERT_READER_STATUS,
                new int[]{f_link, f_trigger, f_lowbat, f_autooff, f_pwr}));
    }

    public void sendGettingStatusWord() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_STATUS_WORD));
    }

    public void sendSettingBuzzerVolume(int volume, int f_nv) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_BUZZER_VOL, new int[]{volume, f_nv}));
    }

    public void sendSettingVibration(int on, int f_nv) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_VIBRATION, new int[]{on, f_nv}));
    }

    public void sendBeep(int f_on) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_BEEP,
                new int[]{f_on}));
    }

    public void sendSettingAutoPowerOffDelay(int delay, int f_nv) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_AUTO_POWER_OFF_DELAY, new int[]{delay, f_nv}));
    }

    public void sendGettingBatteryLevel(int f_ext) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_BATT_LEVEL, new int[]{f_ext}));
    }

    public void sendReportingBatteryState(int f_report) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_REPORT_BATT_STATE,
                new int[]{f_report}));
    }

    public void sendTurningReaderOff() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_TURN_READER_OFF));
    }

    //<-- eric 2012.12.12
    public void sendDeviceProprietary(int mode, String password, String name) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_READER_PROPRIETARY,
                new int[]{mode},
                password,
                name));
    }

    //<-- eric 2013.10.18
    public void sendGettingBTmacAddress() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_BT_MAC_ADDRESS));
    }

    /* Expanded command */
    /* Send Command */
    public void sendTextCommand(String sCmd) {
        sendData(DeviceProtocol.makeProtocol(sCmd));
    }

    /* COMMON */
    public void sendCmdGetBluetoothDeviceName() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_BT_NAME));
    }
    //--> eric 2012.12.12

    public void sendCmdSetClearReport(int enable) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_CLEAR_REPORT, new int[]{enable}));
    }
    //--> eric 2013.10.18

    public void sendCmdGetRFIDBoardInfo() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_HW_BOARD_VERSION));
    }

    public void sendCmdOEMInfo() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_OEM_INFO));
    }

    public void sendCmdGetLocalDataCount() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_GET_LOCAL_DATA_COUNT));
    }

    /* UHF RFID */
    public void sendCmdRfidTagFocus(int enable) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_SET_TAGFOCUS, new int[]{enable}));
    }

    public void sendCmdRfidFastID(int enable) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_SET_FASTID, new int[]{enable}));
    }

    public void sendLinkProfile(int a) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SET_LINK_PROFILE, new int[]{a}));
    }

    public void sendSingleTagSearch(int length, String ACS_EPC, int threshold, int step) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_SINGLE_SEARCH,
                new int[]{length},
                ACS_EPC,
                new int[]{threshold, step}));
    }

    public void sendMultiTagSearch() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH));
    }

    public void sendSetMultiTagList(int index, int length, String ACS_EPC) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH_SET_LIST,
                new int[]{index, length}, ACS_EPC));
    }

    public void sendGetMultiTagList() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH_GET_LIST));
    }

    public void sendGetMultiTagList(int index) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH_GET_LIST, new int[]{index}));
    }

    public void sendClearMultiTagList(int index, int length) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH_CLEAR_LIST, new int[]{index, length}));
    }

    public void sendClearAllMultiTagList() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_MULTI_SEARCH_CLEAR_LIST, new int[]{0, 50}));
    }

    public void sendWildcardTagSearch(int length, String ACS_EPC) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_WILDCARD_SEARCH,
                new int[]{length}, ACS_EPC));
    }

    public void sendSetRFIDDataFormat(int index) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_SET_DATA_FORMAT, new int[]{index}));
    }

    public void sendSetRFIDFixDataFormat(int index) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_SET_FIX_DATA_FORMAT, new int[]{index}));
    }

    public void sendSetRFIDPrefix(int index) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_SET_PREFIX, new int[]{index}));
    }

    public void sendSetRFIDSuffix1(int index) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_SET_SUFFIX1, new int[]{index}));
    }

    public void sendSetRFIDSuffix2(int index) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_SET_SUFFIX2, new int[]{index}));
    }

    public void sendBlockWrite(int length, int membank, int offset, String writeData, String password, int readMode, int select, int timeout, int blockMode) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_BLOCK_WRITE,
                new int[]{length, membank, offset},
                new String[]{writeData, password},
                new int[]{readMode, select, timeout, blockMode}));
    }

    public void sendBlockErase(int length, int membank, int offset, String password, int readMode, int select, int timeout, int blockMode) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_RFID_TAG_BLOCK_WRITE,
                new int[]{length, membank, offset},
                new String[]{password},
                new int[]{readMode, select, timeout, blockMode}));
    }

    /* Scanner */
    public void sendGetScannerType() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_GET_TYPE));
    }

    public void sendGetScannerVersion() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_GET_VERSION));
    }

    public void sendCmdScannerScanStart() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_SCAN_START));
    }

    public void sendCmdScannerScanStop() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_SCAN_STOP));
    }

    public void sendCmdScannerSetDefault() {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_DEFAULT));
    }

    public void sendCmdSetScannerRegister(String sOpCode, int nRegister) {
        sendData(DeviceProtocol.makeProtocol(DeviceProtocol.CMD_SCANNER_PARAMETER, sOpCode, new int[]{nRegister}));
    }

    public static class LockPattern {
        /*
         * public short lockMask; public short lockEnable;
         */
        public boolean enableUser;
        public boolean enableTid;
        public boolean enableUii;
        public boolean enableAcsPwd;
        public boolean enableKillPwd;

        public boolean indexUser;
        public boolean indexTid;
        public boolean indexUii;
        public boolean indexAcsPwd;
        public boolean indexKillPwd;

        public boolean lockPerma;
    }

    private class ConnectThread extends Thread {
        public ConnectThread() {
            if (mBluetoothDevice != null) {
                try {
                    mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(mUuid);
                } catch (IOException e) {
                    mOnDeviceEventListener.onBtDeviceConnectFail();
                }
            } else {
                Log.d("ConnectThread", "BluetoothDevice is null");
                mOnDeviceEventListener.onBtDeviceConnectFail();
            }
        }

        public void run() {
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.cancelDiscovery();
            }

            if (mBluetoothSocket != null) {
                try {
                    mBluetoothSocket.connect();
                } catch (IOException connectException) {
                    try {
                        mOnDeviceEventListener.onBtDeviceConnectFail();
                        mBluetoothSocket.close();
                    } catch (IOException closeException) {
                    }
                    mConnectThread = null;
                    return;
                }

                mConnectedThread = new ConnectedThread(mBluetoothSocket);
                if (mConnectedThread.isInitOk()) {
                    mConnectedThread.start();
                    Log.d("ConnectThread", "Connected");
                    mOnDeviceEventListener.onBtDeviceConnected();
                }
            } else {
                Log.d("ConnectThread", "BluetothSocket is null.");
            }
            mConnectThread = null;
        }

        public void cancel() {
        }
    }

    private class ConnectedThread extends ConnectThread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private boolean InitOk;

        public ConnectedThread(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            mRecvPacketParser.reset();
            InitOk = false;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                InitOk = true;
            } catch (IOException e) {
                Log.d("ConnectedThread", "BluetothSocket is null");
                InitOk = false;
            }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public boolean isInitOk() {
            return InitOk;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (InitOk) {
                try {
                    bytes = inputStream.read(buffer);
                    mRecvPacketParser.pushPacket(buffer, bytes);
                    if (mOnDeviceEventListener != null) {
                        mOnDeviceEventListener.onNotifyDataReceive();
                    }
                } catch (IOException e) {
                    Log.d("ConnectedThread", "[Bluetooth Socket] Read Fail");
                    break;
                }
            }
        }

        /* Call this from the main Activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.d("ConnectedThread", "[Bluetooth Socket] Write Fail");
            }
        }

        /* Call this from the main Activity to shutdown the connection */
        public void cancel() {
            try {
                Log.d("ConnectedThread", "[cancel] bluetoothSocket.close");
                InitOk = false;
                bluetoothSocket.close();
            } catch (IOException e) {

            }
        }
    }
}
