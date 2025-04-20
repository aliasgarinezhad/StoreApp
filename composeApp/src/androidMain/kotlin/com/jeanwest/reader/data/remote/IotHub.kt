package com.jeanwest.reader.data.remote

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.preference.PreferenceManager
import com.jeanwest.reader.useCases.commonCatchHandler
import com.microsoft.azure.sdk.iot.device.DeviceClient
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException
import com.microsoft.azure.sdk.iot.device.twin.ReportedPropertiesUpdateResponse
import com.microsoft.azure.sdk.iot.device.twin.Twin
import com.microsoft.azure.sdk.iot.device.twin.TwinCollection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.json.JSONObject


/*
*
* this service handle IOTHub client functions in background.
* each device has its own IOTHub profile that created when user register it in app.
* device serial number, write permission, write serial number range, app version and
* device location saved in device IOTHub profile.
* this service used for tracking devices, write access and automatic update.
*
* */

class IotHub : Service() {

    private var deviceId = ""
    private var iotToken = ""
    private var serial = ""
    private var serialNumber = 0L
    private var serialNumberMax = 0L
    private var serialNumberMin = 0L
    private lateinit var client: DeviceClient

    private var deviceLocationCode = 0
    private var deviceLocation = ""

    private fun getTwinDesiredParameters(twin: Twin) {

        Log.e("iothub", twin.toString())

        if ("epcGenerationProps" in twin.desiredProperties) {
            val epcGenerationProps =
                JSONObject(twin.desiredProperties.getValue("epcGenerationProps").toString())
            Log.e("iothub", "epcGenerationProps changed to $epcGenerationProps")

            val tagSerialNumberRange =
                epcGenerationProps.getJSONObject("tagSerialNumberRange")
            if (tagSerialNumberRange.getLong("min") != serialNumberMin ||
                tagSerialNumberRange.getLong("max") != serialNumberMax
            ) {
                serialNumberMin = tagSerialNumberRange.getLong("min")
                serialNumberMax = tagSerialNumberRange.getLong("max")
                serialNumber = serialNumberMin

                Log.e("iothub", "serialNumberMin changed to $serialNumberMin")
                Log.e("iothub", "serialNumber changed to $serialNumberMin")
                Log.e("iothub", "serialNumberMax changed to $serialNumberMax")
                saveToMemory()
            }
        }
    }

    private fun setTwinReportedParameters() {
        val reportedProperties = TwinCollection()

        val location = JSONObject()
        location.put("deviceLocationCode", deviceLocationCode)
        location.put("deviceLocation", deviceLocation)
        reportedProperties["location"] = location
        reportedProperties["Serial"] = serial
        reportedProperties["nextTagSerialNumber"] = serialNumber
        reportedProperties["username"] = ""
        reportedProperties["installedAppVersion"] =
            packageManager.getPackageInfo(packageName, 0).versionName
        reportedProperties["connectivityType"] = null
        client.updateReportedPropertiesAsync(/* reportedProperties = */ reportedProperties, /* reportedPropertiesCallback = */
            { iotHubStatusCode: IotHubStatusCode, _: ReportedPropertiesUpdateResponse, e: IotHubClientException?, _: Any ->
                if (iotHubStatusCode != IotHubStatusCode.OK) {
                    if (e != null) {
                        commonCatchHandler(e)
                    }
                    Log.e("iothub", "set reported parameters fail with status code $iotHubStatusCode")
                } else {
                    Log.e("iothub", "set reported parameters successfully")
                }
            }, /* callbackContext = */
            this
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null) {
            return START_STICKY
        }

        loadMemory()

        CoroutineScope(IO).launch {
            initClient()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun loadMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        serialNumber = memory.getLong("value", -1L)
        serialNumberMax = memory.getLong("max", -1L)
        serialNumberMin = memory.getLong("min", -1L)
        deviceId = memory.getString("deviceId", "") ?: ""
        iotToken = memory.getString("iotToken", "") ?: ""
        serial = memory.getString("deviceSerialNumber", "") ?: ""
        deviceLocationCode = memory.getInt("deviceLocationCode", 0)
        deviceLocation = memory.getString("deviceLocation", "") ?: ""
    }

    private fun saveToMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val memoryEditor = memory.edit()
        memoryEditor.putLong("value", serialNumber)
        memoryEditor.putLong("max", serialNumberMax)
        memoryEditor.putLong("min", serialNumberMin)
        memoryEditor.putLong("counterModified", 0L)
        memoryEditor.putString("deviceId", deviceId)
        memoryEditor.putString("iotToken", iotToken)
        memoryEditor.putString("deviceSerialNumber", serial)
        memoryEditor.apply()
    }

    private fun initClient() {

        if (deviceId.isEmpty() || iotToken.isEmpty()) {
            return
        }

        val connString = "HostName=rfid-frce.azure-devices.net;DeviceId=" + deviceId +
                ";SharedAccessKey=" + iotToken

        Log.e("iothub", connString)
        client = DeviceClient(connString, IotHubClientProtocol.MQTT)
        Log.e("iothub", "connect to IOTHub started")

        try {
            client.open(true)
            Log.e("iothub", "connect to IOTHub successfully")

            client.subscribeToDesiredPropertiesAsync(
                /* desiredPropertiesCallback = */ { twin, _ ->

                    getTwinDesiredParameters(twin)
                    Log.e("iothub", "successfully updated desired properties")
                },
                /* desiredPropertiesCallbackContext = */ this,
                /* subscriptionAcknowledgedCallback = */ { e, _ ->
                    if (e != null) {
                        commonCatchHandler(e)
                    } else {
                        Log.e("iothub", "successfully connected to desired properties")
                        setTwinReportedParameters()
                    }
                },
                /* desiredPropertiesSubscriptionCallbackContext = */ this
            )

        } catch (e: Exception) {
            commonCatchHandler(e)
            client.close()
            Log.e("iothub", "connect to IOTHub error")
        }
    }
}