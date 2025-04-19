package com.jeanwest.reader.useCases.pointMobileSDK

/**
 * Created by NG on 2016-07-07.
 */
interface OnDeviceEventListener {
    fun onNotifyDataReceive()

    fun onNotifyDataWriteFail()

    fun onBtDeviceConnected()

    fun onBtDeviceConnectFail()
}
