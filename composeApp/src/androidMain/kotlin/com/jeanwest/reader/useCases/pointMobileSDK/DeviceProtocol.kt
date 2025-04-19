package com.jeanwest.reader.useCases.pointMobileSDK

/**
 * Created by NG on 2016-04-07.
 */

/**
 * FOR DeviceProtocol.java
 * This file is command define. if you want add command, have to add command define.
 * Please refer to command of the RSP document.
 */
object DeviceProtocol {
    val NULL_1: ByteArray = byteArrayOf(0x0d, 0x0a)
    val NULL_2: ByteArray = byteArrayOf(0x0d)
    val OPEN_INTERFACE_1: ByteArray = byteArrayOf(0x0d, 0x0a, 0x0d, 0x0a, 0x0d, 0x0a, 0x0d, 0x0a)
    val OPEN_INTERFACE_2: ByteArray = byteArrayOf(0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d)

    val BYE: ByteArray =
        byteArrayOf('b'.code.toByte(), 'y'.code.toByte(), 'e'.code.toByte(), 0x0d, 0x0a)
    const val SKIP_PARAM: Int = -0x1

    // ---
    const val CMD_INVENT: String = "I"
    const val CMD_STOP: String = "s"
    const val CMD_GET_VERSION: String = "ver"
    const val CMD_SET_DEF_PARAM: String = "Default"
    const val CMD_GET_PARAM: String = "g"
    const val CMD_SEL_MASK: String = "M"
    const val CMD_INVENT_PARAM: String = "Iparam"
    const val CMD_SET_TX_POWER: String = "Txp"
    const val CMD_GET_MAX_POWER: String = "Maxp"
    const val CMD_SET_TX_CYCLE: String = "Txc"
    const val CMD_CHANGE_CH_STATE: String = "Chs"
    const val CMD_SET_LINK_PROFILE: String = "linkp"
    const val CMD_SET_COUNTRY: String = "Cc"
    const val CMD_GET_COUNTRY_CAP: String = "ccap"
    const val CMD_READ_TAG_MEM: String = "R"
    const val CMD_WRITE_TAG_MEM: String = "W"
    const val CMD_KILL_TAG: String = "Kill"
    const val CMD_LOCK_TAG_MEM: String = "Lock"
    const val CMD_SET_LOCK_TAG_MEM: String = "lockperm"
    const val CMD_PAUSE_TX: String = "Pause"
    const val CMD_HEART_BEAT: String = "Online"
    const val CMD_STATUS_REPORT: String = "alert"
    const val CMD_INVENT_REPORT_FORMAT: String = "Ireport"
    const val CMD_SYSTEM_TIME: String = "Time"
    const val CMD_DISLINK: String = "bye"
    const val CMD_UPLOAD_TAG_DATA: String = "Br.upl"
    const val CMD_CLEAR_TAG_DATA: String = "Br.clrlist"
    const val CMD_ALERT_READER_STATUS: String = "Br.alert"
    const val CMD_GET_STATUS_WORD: String = "Br.sta"
    const val CMD_SET_BUZZER_VOL: String = "Br.vol"
    const val CMD_BEEP: String = "Br.beep"
    const val CMD_SET_AUTO_POWER_OFF_DELAY: String = "Br.autooff"
    const val CMD_GET_BATT_LEVEL: String = "Br.batt"
    const val CMD_REPORT_BATT_STATE: String = "Br.reportbatt"
    const val CMD_TURN_READER_OFF: String = "Br.off"
    const val CMD_READER_PROPRIETARY: String = "Br.bt.config"

    //<--eric 2013.10.18
    const val CMD_GET_BT_MAC_ADDRESS: String = "Br.bt.mac"

    /* expanded command */ /* COMMON */
    const val CMD_GET_BT_NAME: String = "Br.bt.name"

    //--> eric 2013.10.18
    const val CMD_CLEAR_REPORT: String = "clr"
    const val CMD_GET_HW_BOARD_VERSION: String = "Bdif"
    const val CMD_GET_OEM_INFO: String = "Oemif"
    const val CMD_GET_LOCAL_DATA_COUNT: String = "Br.taglist"
    const val CMD_VIBRATION: String = "Br.vib"

    /* UHF RFID */
    const val CMD_RFID_SET_TAGFOCUS: String = "Rf.tagfocus"
    const val CMD_RFID_SET_FASTID: String = "Rf.fastid"
    const val CMD_RFID_TAG_SINGLE_SEARCH: String = "Rf.ss"
    const val CMD_RFID_TAG_MULTI_SEARCH: String = "Rf.ms"
    const val CMD_RFID_TAG_WILDCARD_SEARCH: String = "Rf.ws"
    const val CMD_RFID_TAG_MULTI_SEARCH_GET_LIST: String = "Rf.gsl"
    const val CMD_RFID_TAG_MULTI_SEARCH_SET_LIST: String = "Rf.ssl"
    const val CMD_RFID_TAG_MULTI_SEARCH_CLEAR_LIST: String = "Rf.csl"
    const val CMD_RFID_TAG_BLOCK_WRITE: String = "Rf.bw"
    const val CMD_RFID_TAG_BLOCK_ERASE: String = "Rf.be"
    const val CMD_RFID_SET_DATA_FORMAT: String = "Rf.data"
    const val CMD_RFID_SET_FIX_DATA_FORMAT: String = "Rf.fixdata"
    const val CMD_RFID_SET_PREFIX: String = "Rf.prefix"
    const val CMD_RFID_SET_SUFFIX1: String = "Rf.suffix1"
    const val CMD_RFID_SET_SUFFIX2: String = "Rf.suffix2"

    /* Scanner */
    const val CMD_SCANNER_GET_TYPE: String = "sc.type"
    const val CMD_SCANNER_GET_VERSION: String = "sc.ver"
    const val CMD_SCANNER_SCAN_START: String = "sc.start"
    const val CMD_SCANNER_SCAN_STOP: String = "sc.stop"
    const val CMD_SCANNER_PARAMETER: String = "sc.param"
    const val CMD_SCANNER_DEFAULT: String = "sc.default"
    const val CMD_SCANNER_OPCODE_1DSYMBOLOGIES: String = "0"
    const val CMD_SCANNER_OPCODE_2DSYMBOLOGIES: String = "1"
    const val CMD_SCANNER_OPCODE_UPCEAN: String = "2"
    const val CMD_SCANNER_OPCODE_ISBN: String = "3"
    const val CMD_SCANNER_OPCODE_CODE128: String = "4"
    const val CMD_SCANNER_OPCODE_ISBT: String = "5"
    const val CMD_SCANNER_OPCODE_CODE39: String = "6"
    const val CMD_SCANNER_OPCODE_CODE93: String = "7"
    const val CMD_SCANNER_OPCODE_CODE11: String = "8"
    const val CMD_SCANNER_OPCODE_INTERLEAVED2OF5: String = "9"
    const val CMD_SCANNER_OPCODE_DISCRETE2OF5: String = "10"
    const val CMD_SCANNER_OPCODE_CODABAR: String = "11"
    const val CMD_SCANNER_OPCODE_MSI: String = "12"
    const val CMD_SCANNER_OPCODE_MATRIX2OF5: String = "13"
    const val CMD_SCANNER_OPCODE_GS1DATABAR: String = "14"
    const val CMD_SCANNER_OPCODE_POSTALCODE: String = "15"
    const val CMD_SCANNER_OPCODE_COMPOSITE: String = "16"
    const val CMD_SCANNER_OPCODE_MICROPDF417: String = "17"
    const val CMD_SCANNER_OPCODE_MACROPDF: String = "18"
    const val CMD_SCANNER_OPCODE_DATAMATRIX: String = "19"
    const val CMD_SCANNER_OPCODE_SCANNING_PREFERENCES: String = "20"
    const val CMD_SCANNER_OPCODE_DATA_FORMAT: String = "21"
    const val CMD_SCANNER_OPCODE_REDUNDANCYNSECURITY: String = "22"
    const val CMD_SCANNER_OPCODE_DELIMITER: String = "23"
    var N_TYPE: Int = 1

    fun tagErrorCodeToString(code: Int): String {
        when (code) {
            0x00 -> return "general error"
            0x03 -> return "specified memory location does not exist or the PC value is not supported by the tag"
            0x04 -> return "specified memory location is locked and/or permalocked and is not writeable"
            0x0B -> return "tag has insufficient power to perform the memory write"
            0x0F -> return "tag does not support error-specific codes"
        }
        return "Unknown error"
    }

    fun moduleErrorCodeToString(code: Int): String {
        when (code) {
            0x01 -> return "Read after write verify failed."
            0x02 -> return "Problem transmitting tag command."
            0x03 -> return "CRC error on tag response to a write."
            0x04 -> return "CRC error on the read packet when verifying the write."
            0x05 -> return "Maximum retry's on the write exceeded."
            0x06 -> return "Failed waiting for read data from tag, possible timeout."
            0x07 -> return "Failure requesting a new tag handle."
            0x0A -> return "Error waiting for tag response, possible timeout."
            0x0B -> return "CRC error on tag response to a kill."
            0x0C -> return "Problem transmitting 2nd half of tag kill."
            0x0D -> return "Tag responded with an invalid handle on first kill command."
            0x0F -> return "Bad Access Password."
        }
        return "Internal Use"
    }

    val typeSize: Int
        get() {
            if (N_TYPE == 1) return 2
            return 1
        }

    val delimeter: String
        get() {
            if (N_TYPE == 1) return "\r\n"
            return "\r"
        }

    val type: ByteArray
        get() {
            if (N_TYPE == 1) return NULL_1
            return NULL_2
        }

    fun setType(type: Int) {
        if (type == 1) N_TYPE = 1
        else N_TYPE = 2
    }

    fun makeProtocol(cmd: String?, param: IntArray?): ByteArray {
        val protocol = StringBuilder()
        protocol.append(cmd)

        if (param != null && param.size > 0) {
            for (i in param.indices) {
                protocol.append(',')
                if (param[i] != SKIP_PARAM) {
                    protocol.append(param[i])
                }
            }
        }
        //Log.d("MakeProtocol 1 : ","["+protocol+"]");
        return string2bytes(protocol.toString())
    }

    fun makeProtocol(cmd: String): ByteArray {
        //Log.d("MakeProtocol 2 : ","["+protocol+"]");
        return string2bytes(
            cmd //Log.d("MakeProtocol 2 : ","["+protocol+"]");
        )
    }

    //<-- eric 2012.12.12
    fun makeProtocol(cmd: String?, param: IntArray?, param1: String?, param2: String?): ByteArray {
        val protocol = StringBuilder()
        protocol.append(cmd)

        // param
        if (param != null && param.size > 0) {
            for (i in param.indices) {
                protocol.append(',')
                if (param[i] != SKIP_PARAM) protocol.append(param[i])
            }
        }

        // param1
        protocol.append(",")

        if (param1 != null) protocol.append(param1)

        // param2
        protocol.append(",")

        if (param1 != null) protocol.append(param2)

        //Log.d("MakeProtocol 3 : ","["+protocol+"]");
        return string2bytes(protocol.toString())
    }

    //--> eric 2012.12.12
    fun makeProtocol(cmd: String?, options: Array<String?>?): ByteArray {
        val protocol = StringBuilder()
        protocol.append(cmd)

        if (options != null && options.size > 0) {
            for (i in options.indices) {
                protocol.append(",")
                if (options[i] != null) protocol.append(options[i])
            }
        }

        //Log.d("MakeProtocol 4 : ","["+protocol+"]");
        return string2bytes(protocol.toString())
    }

    fun makeProtocol(
        cmd: String?,
        param: IntArray?,
        options: Array<String?>?,
        param2: IntArray?
    ): ByteArray {
        val protocol = StringBuilder()
        protocol.append(cmd)

        if (param != null && param.size > 0) {
            for (i in param.indices) {
                protocol.append(',')
                if (param[i] != SKIP_PARAM) protocol.append(param[i])
            }
        }


        if (options != null) {
            for (i in options.indices) {
                protocol.append(",")
                if (options[i] != null) protocol.append(options[i])
            }
        }

        if (param2 != null && param2.size > 0) {
            for (i in param2.indices) {
                protocol.append(',')
                if (param2[i] != SKIP_PARAM) protocol.append(param2[i])
            }
        }

        //Log.d("MakeProtocol 5 : ","["+protocol+"]");
        return string2bytes(protocol.toString())
    }

    fun makeProtocol(
        cmd: String?,
        param: IntArray?,
        option: String?,
        param2: IntArray?
    ): ByteArray {
        val protocol = StringBuilder()
        protocol.append(cmd)

        //Log.d("MakeProtocol 6 : ","[]");
        if (param != null && param.size > 0) {
            for (i in param.indices) {
                protocol.append(',')
                if (param[i] != SKIP_PARAM) protocol.append(param[i])
            }
        }

        protocol.append(",")
        if (option != null) protocol.append(option)

        if (param2 != null && param2.size > 0) {
            for (i in param2.indices) {
                protocol.append(',')
                if (param2[i] != SKIP_PARAM) protocol.append(param2[i])
            }
        }

        //Log.d("MakeProtocol 6 : ","["+protocol+"]");
        return string2bytes(protocol.toString())
    }

    fun makeProtocol(cmd: String?, option: String?, param2: IntArray?): ByteArray {
        val protocol = StringBuilder()
        protocol.append(cmd)

        protocol.append(",")
        if (option != null) protocol.append(option)

        if (param2 != null && param2.size > 0) {
            for (i in param2.indices) {
                protocol.append(',')
                if (param2[i] != SKIP_PARAM) protocol.append(param2[i])
            }
        }

        //Log.d("MakeProtocol 7 : ","["+protocol+"]");
        return string2bytes(protocol.toString())
    }

    fun makeProtocol(cmd: String?, param: IntArray?, option: String?): ByteArray {
        val protocol = StringBuilder()
        protocol.append(cmd)

        if (param != null && param.size > 0) {
            for (i in param.indices) {
                protocol.append(',')
                if (param[i] != SKIP_PARAM) protocol.append(param[i])
            }
        }

        protocol.append(",")
        if (option != null) protocol.append(option)

        //Log.d("MakeProtocol 8 : ","["+protocol+"]");
        return string2bytes(protocol.toString())
    }

    fun string2bytes(str: String): ByteArray {
        val charProtocol = str.toCharArray()
        val byteProtocol = ByteArray(charProtocol.size + typeSize)
        var index = 0
        run {
            var i = 0
            while (i < charProtocol.size) {
                byteProtocol[index] = (charProtocol[i].code and 0xff).toByte()
                ++i
                ++index
            }
        }

        // ---
        var i = 0
        while (i < typeSize) {
            byteProtocol[index] = type[i]
            ++i
            ++index
        }

        return byteProtocol
    }
}