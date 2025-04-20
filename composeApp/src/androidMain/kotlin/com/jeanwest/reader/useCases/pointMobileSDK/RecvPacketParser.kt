package com.jeanwest.reader.useCases.pointMobileSDK

import android.util.Log
import java.util.StringTokenizer
import kotlin.math.min

/**
 * Created by NG on 2016-04-06.
 */
class RecvPacketParser {
    private val mPacket = StringBuilder()
    private var mCharBuff: CharArray? = null
    private var mCharBuffSize = 0

    @Synchronized
    fun reset() {
        mPacket.setLength(0)
    }

    @Synchronized
    fun pushPacket(buffer: ByteArray, len: Int) {
        if (mCharBuffSize < len) {
            mCharBuffSize = (len shl 1)
            mCharBuff = CharArray(mCharBuffSize)
        }

        for (i in 0 until len) mCharBuff!![i] = (buffer[i].toInt() and 0xff).toChar()
        mPacket.append(mCharBuff, 0, len)
    }

    @Synchronized
    fun popPacket(offset: Int, len: Int): String {
        val pop = mPacket.substring(offset, offset + len)
        mPacket.delete(0, offset + len)
        return pop
    }

    @Synchronized
    fun popPacket(): String? {
        val STR_PACKET = mPacket.toString()
        val DELIMETER = DeviceProtocol.delimeter

        val cmdIndex = STR_PACKET.indexOf("$>")
        if (cmdIndex >= 0) {
            if (STR_PACKET.replace("\n", "").indexOf("$>") == 0) return popPacket(cmdIndex, 2)
        }

        val st = StringTokenizer(STR_PACKET, DELIMETER)
        if (st.hasMoreTokens()) {
            val str = st.nextToken()
            if (str != null && str.length > 0) {
                if (str.length + DELIMETER.length > mPacket.length) return null
                if (str.length == 1 || str.length > 40) Log.d(
                    "RecvPacketParser",
                    "Data is too long. String Length : " + str.length
                )
                mPacket.delete(
                    0,
                    min(
                        mPacket.length.toDouble(),
                        (str.length + DELIMETER.length).toDouble()
                    ).toInt()
                )
                return str
            }
        }
        return null
    }
}
