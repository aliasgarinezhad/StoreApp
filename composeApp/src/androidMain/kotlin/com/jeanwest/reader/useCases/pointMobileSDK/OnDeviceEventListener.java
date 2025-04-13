package com.jeanwest.reader.useCases.pointMobileSDK;

/**
 * Created by NG on 2016-07-07.
 */
public interface OnDeviceEventListener {
    void onNotifyDataReceive();

    void onNotifyDataWriteFail();

    void onBtDeviceConnected();

    void onBtDeviceConnectFail();
}
