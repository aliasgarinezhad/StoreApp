package com.jeanwest.reader.useCases.pointMobileSDK

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.Code11
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.Code128
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.Code39
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.Code93
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.Discrete2of5
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.ISBT
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.Interleaved2of5
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.Matrix2of5
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.UPCEAN
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.UPCEAN.Convert.UPC_E0ToA
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.OneD.UPCEAN.Convert.UPC_E1ToA
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.TwoD.MacroPDF
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.TwoD.MicroPDF417
import com.jeanwest.reader.useCases.pointMobileSDK.DeviceState.Scanner.Symbologies.AdvancedConfig.TwoD.PostalCodes

/**
 * Created by NG on 2016-04-28.
 */
@SuppressLint("MissingPermission")
object DeviceState {
    private const val ENABLE: Int = 1
    private const val DISABLE: Int = 0

    object Scanner {
        fun setDefault() {
            ScanningPreferences.setDefault()
            Symbologies.setDefault()
            DataFormat.setDefault()
            RedundancyAndSecurityLevel.setDefault()
            Delimiter.setDefault()
        }

        private fun enable(bitNum: Int, registerValue: Int): Int {
            return (registerValue or (0x1 shl bitNum))
        }

        private fun disable(bitNum: Int, registerValue: Int): Int {
            return (registerValue and ((0x1 shl bitNum).inv()))
        }

        private fun isEnable(bitNum: Int, registerValue: Int): Boolean {
            return (registerValue and (0x1 shl bitNum)) > 0
        }

        private fun setValue(bitNum: Int, nData: Int, registerValue: Int, nMaxData: Int): Int {
            var registerValue: Int = registerValue
            registerValue = registerValue and ((nMaxData shl bitNum).inv())
            registerValue = registerValue or (nData shl bitNum)
            return registerValue
        }

        private fun getValue(bitNum: Int, nRegister: Int, nMaxData: Int): Int {
            return (nRegister and (nMaxData shl bitNum)) shr bitNum
        }

        object Firmware {
            var firmwareVersion: String? = null
        }

        object Type {
            const val NO_SCANNER: Int = 0
            const val SCANNER_1D_SE655: Int = 1
            const val SCANNER_2D_SE4750: Int = 2
            const val SCANNER_2D_EM3396: Int = 3
            var type: Int = NO_SCANNER
        }

        object ScanningPreferences {
            //private static int registerValue = 0x13900063;
            var register: Int = setDefault()

            fun setDefault(): Int {
                return (DecodeSessionTimeout.defaultValue
                        or Fnc1.defaultValue
                        or Inverse.defaultValue
                        or PickList.defaultValue
                        or MirroredImage.defaultValue
                        or MobileDisplayMode.defaultValue
                        or DecodingIllumination.defaultValue
                        or DecodingAimingPattern.defaultValue
                        or OneDQuietZoneLevel.defaultValue
                        or IntercharacterGapSize.defaultValue
                        or Fuzzy1DProcessing.defaultValue)
            }

            object DecodeSessionTimeout {
                const val MAX_VALUE: Int = 0xFF
                private const val BIT_DECODESESSION: Int = 0
                val defaultValue: Int = 99 shl BIT_DECODESESSION

                var timeout: Int
                    get() = getValue(
                        BIT_DECODESESSION,
                        register,
                        MAX_VALUE
                    )
                    set(nTimeout) {
                        var nTimeout: Int = nTimeout
                        if (nTimeout > 99) nTimeout = 99
                        else if (nTimeout < 5) nTimeout = 5
                        register =
                            setValue(
                                BIT_DECODESESSION,
                                nTimeout,
                                register,
                                MAX_VALUE
                            )
                    }
            }

            object Fnc1 {
                const val S_KEY_ENABLE: String = "Enable"
                const val S_KEY_ASCII: String = "Ascii"
                private const val BIT_FNC1_ENABLE: Int = 8
                private const val BIT_FNC1_VALUE: Int = 9
                val defaultValue: Int = (DISABLE shl BIT_FNC1_ENABLE) or (0 shl BIT_FNC1_VALUE)
                private const val FNC1_MAX_VALUE: Int = 0x7F

                fun enable() {
                    register = enable(BIT_FNC1_ENABLE, register)
                }

                fun disable() {
                    register = disable(BIT_FNC1_ENABLE, register)
                }

                val isEnable: Boolean
                    get() {
                        return isEnable(
                            BIT_FNC1_ENABLE,
                            register
                        )
                    }

                var asciiCode: Int
                    get() {
                        return getValue(
                            BIT_FNC1_VALUE,
                            register,
                            FNC1_MAX_VALUE
                        )
                    }
                    set(nCode) {
                        register =
                            setValue(
                                BIT_FNC1_VALUE,
                                nCode,
                                register,
                                FNC1_MAX_VALUE
                            )
                    }
            }

            object Inverse {
                const val MODE_REGULAR: Int = 0
                const val MODE_INVERSE_ONLY: Int = 1
                const val MODE_INVERSE_AUTO: Int = 2
                const val MAX_VALUE: Int = 3
                private const val BIT_INVERSE_1D: Int = 16
                private const val BIT_INVERSE_2D: Int = 18
                val defaultValue: Int =
                    (MODE_REGULAR shl BIT_INVERSE_1D) or (MODE_REGULAR shl BIT_INVERSE_2D)

                object OneD {
                    var mode: Int
                        get() {
                            return getValue(
                                BIT_INVERSE_1D,
                                register,
                                MAX_VALUE
                            )
                        }
                        set(nMode) {
                            register =
                                setValue(
                                    BIT_INVERSE_1D,
                                    nMode,
                                    register,
                                    MAX_VALUE
                                )
                        }
                }

                object TwoD {
                    var mode: Int
                        get() {
                            return getValue(
                                BIT_INVERSE_2D,
                                register,
                                MAX_VALUE
                            )
                        }
                        set(nMode) {
                            register =
                                setValue(
                                    BIT_INVERSE_2D,
                                    nMode,
                                    register,
                                    MAX_VALUE
                                )
                        }
                }
            }

            object PickList {
                private const val BIT_PICKLIST: Int = 20
                val defaultValue: Int = ENABLE shl BIT_PICKLIST

                fun enable() {
                    register = enable(BIT_PICKLIST, register)
                }

                fun disable() {
                    register = disable(BIT_PICKLIST, register)
                }

                val isEnable: Boolean
                    get() {
                        return isEnable(
                            BIT_PICKLIST,
                            register
                        )
                    }
            }

            object MirroredImage {
                private const val BIT_MIRRORED_IMAGE: Int = 21
                val defaultValue: Int = DISABLE shl BIT_MIRRORED_IMAGE

                fun enable() {
                    register = enable(BIT_MIRRORED_IMAGE, register)
                }

                fun disable() {
                    register = disable(BIT_MIRRORED_IMAGE, register)
                }

                val isEnable: Boolean
                    get() {
                        return isEnable(
                            BIT_MIRRORED_IMAGE,
                            register
                        )
                    }
            }

            object MobileDisplayMode {
                private const val BIT_MOBILE_DISPLAY_MODE: Int = 22
                val defaultValue: Int = DISABLE shl BIT_MOBILE_DISPLAY_MODE

                fun enable() {
                    register = enable(BIT_MOBILE_DISPLAY_MODE, register)
                }

                fun disable() {
                    register = disable(BIT_MOBILE_DISPLAY_MODE, register)
                }

                val isEnable: Boolean
                    get() {
                        return isEnable(
                            BIT_MOBILE_DISPLAY_MODE,
                            register
                        )
                    }
            }

            object DecodingIllumination {
                private const val BIT_DECODING_ILLUMINATION: Int = 23
                val defaultValue: Int = ENABLE shl BIT_DECODING_ILLUMINATION

                fun enable() {
                    register = enable(BIT_DECODING_ILLUMINATION, register)
                }

                fun disable() {
                    register = disable(BIT_DECODING_ILLUMINATION, register)
                }

                val isEnable: Boolean
                    get() {
                        return isEnable(
                            BIT_DECODING_ILLUMINATION,
                            register
                        )
                    }
            }

            object DecodingAimingPattern {
                private const val BIT_DECODING_AIMING_PATTERN: Int = 24
                val defaultValue: Int = ENABLE shl BIT_DECODING_AIMING_PATTERN

                fun enable() {
                    register = enable(BIT_DECODING_AIMING_PATTERN, register)
                }

                fun disable() {
                    register = disable(BIT_DECODING_AIMING_PATTERN, register)
                }

                val isEnable: Boolean
                    get() {
                        return isEnable(
                            BIT_DECODING_AIMING_PATTERN,
                            register
                        )
                    }
            }

            object OneDQuietZoneLevel {
                const val MODE_NORMALLY: Int = 0
                const val MODE_MORE_AGGRESSIVELY: Int = 1
                const val MODE_ONE_SIDE_EB: Int = 2
                const val MODE_ANYTHING: Int = 3

                private const val BIT_QUIETZONE_LEVEL: Int = 25
                val defaultValue: Int = 1 shl BIT_QUIETZONE_LEVEL
                private const val MAX_VALUE: Int = 3

                var mode: Int
                    get() {
                        return getValue(
                            BIT_QUIETZONE_LEVEL,
                            register,
                            MAX_VALUE
                        )
                    }
                    set(nMode) {
                        register =
                            setValue(
                                BIT_QUIETZONE_LEVEL,
                                nMode,
                                register,
                                MAX_VALUE
                            )
                    }
            }

            object IntercharacterGapSize {
                const val LARGE_SIZE: Int = 1
                const val NORMAL_SIZE: Int = 0
                private const val BIT_INTERCHARACTER_GAP_SIZE: Int = 27
                val defaultValue: Int = NORMAL_SIZE shl BIT_INTERCHARACTER_GAP_SIZE

                fun setLarge() {
                    register = enable(BIT_INTERCHARACTER_GAP_SIZE, register)
                }

                fun setNormal() {
                    register = disable(BIT_INTERCHARACTER_GAP_SIZE, register)
                }

                val size: Int
                    get() {
                        return if (isEnable(
                                BIT_INTERCHARACTER_GAP_SIZE,
                                register
                            )
                        ) 1 else 0
                    }
            }

            object Fuzzy1DProcessing {
                const val BIT_1DFUZZY: Int = 28
                val defaultValue: Int = ENABLE shl BIT_1DFUZZY

                fun enable() {
                    register = enable(BIT_1DFUZZY, register)
                }

                fun disable() {
                    register = disable(BIT_1DFUZZY, register)
                }

                val isEnable: Boolean
                    get() {
                        return isEnable(
                            BIT_1DFUZZY,
                            register
                        )
                    }
            }
        }

        object DataFormat {
            //private static int mDataOptionRegisterValue = 0x68A000;
            var register: Int =
                default

            private val default: Int
                get() {
                    return (TransmitCodeID.defaultValue
                            or ScanDataTransmissionFormat.defaultValue
                            or Prefix.defaultValue
                            or Suffix1.defaultValue
                            or Suffix2.defaultValue)
                }

            fun setDefault() {
                register =
                    default
            }

            object TransmitCodeID {
                private const val BIT_DATAOPTION_TRANSMIT_CODEID: Int = 0
                internal val defaultValue: Int = 0 shl BIT_DATAOPTION_TRANSMIT_CODEID
                var MAX_VALUE: Int = 0x3

                var value: Int
                    get() {
                        return getValue(
                            BIT_DATAOPTION_TRANSMIT_CODEID,
                            register,
                            MAX_VALUE
                        )
                    }
                    set(mode) {
                        register =
                            setValue(
                                BIT_DATAOPTION_TRANSMIT_CODEID,
                                mode,
                                register,
                                MAX_VALUE
                            )
                    }
            }

            object ScanDataTransmissionFormat {
                var BIT_DATAOPTION_TRANSMIT_FORMAT: Int = 2
                val defaultValue: Int = 0 shl BIT_DATAOPTION_TRANSMIT_FORMAT
                var MAX_VALUE: Int = 0x7

                var format: Int
                    get() {
                        return getValue(
                            BIT_DATAOPTION_TRANSMIT_FORMAT,
                            register,
                            MAX_VALUE
                        )
                    }
                    set(formatType) {
                        register =
                            setValue(
                                BIT_DATAOPTION_TRANSMIT_FORMAT,
                                formatType,
                                register,
                                MAX_VALUE
                            )
                    }
            }

            object Prefix {
                var BIT_DATAOPTION_PREFIX: Int = 5
                val defaultValue: Int = 0 shl BIT_DATAOPTION_PREFIX
                var MAX_VALUE: Int = 0x7F

                var value: Int
                    get() {
                        return getValue(
                            BIT_DATAOPTION_PREFIX,
                            register,
                            MAX_VALUE
                        )
                    }
                    set(value) {
                        register =
                            setValue(
                                BIT_DATAOPTION_PREFIX,
                                value,
                                register,
                                MAX_VALUE
                            )
                    }
            }

            object Suffix1 {
                var BIT_DATAOPTION_SUFFIX1: Int = 12
                val defaultValue: Int = 0xA shl BIT_DATAOPTION_SUFFIX1
                var MAX_VALUE: Int = 0x7F

                var value: Int
                    get() {
                        return getValue(
                            BIT_DATAOPTION_SUFFIX1,
                            register,
                            MAX_VALUE
                        )
                    }
                    set(value) {
                        register =
                            setValue(
                                BIT_DATAOPTION_SUFFIX1,
                                value,
                                register,
                                MAX_VALUE
                            )
                    }
            }

            object Suffix2 {
                var BIT_DATAOPTION_SUFFIX2: Int = 19
                val defaultValue: Int = 0xD shl BIT_DATAOPTION_SUFFIX2
                var MAX_VALUE: Int = 0x7F

                var value: Int
                    get() {
                        return getValue(
                            BIT_DATAOPTION_SUFFIX2,
                            register,
                            MAX_VALUE
                        )
                    }
                    set(value) {
                        register =
                            setValue(
                                BIT_DATAOPTION_SUFFIX2,
                                value,
                                register,
                                MAX_VALUE
                            )
                    }
            }
        }

        object RedundancyAndSecurityLevel {
            //private static int mRnSRegister = 0x5;
            var register: Int = default

            private val default: Int
                get() {
                    return (Redundancy.defaultValue
                            or Security.defaultValue)
                }

            fun setDefault() {
                register = default
            }

            object Security {
                const val BIT_RNS_SECURITY: Int = 2
                const val MAX_VALUE: Int = 0x3
                val defaultValue: Int = 1 shl BIT_RNS_SECURITY

                fun level0() {
                    register = setValue(BIT_RNS_SECURITY, 0x0, register, MAX_VALUE)
                }

                fun level1() {
                    register = setValue(BIT_RNS_SECURITY, 0x1, register, MAX_VALUE)
                }

                fun level2() {
                    register = setValue(BIT_RNS_SECURITY, 0x2, register, MAX_VALUE)
                }

                fun level3() {
                    register = setValue(BIT_RNS_SECURITY, 0x3, register, MAX_VALUE)
                }

                val value: Int
                    get() {
                        return getValue(
                            BIT_RNS_SECURITY,
                            register,
                            MAX_VALUE
                        )
                    }
            }

            object Redundancy {
                const val BIT_RNS_REDUNDANCY: Int = 0
                const val MAX_VALUE: Int = 0x3
                val defaultValue: Int = 1 shl BIT_RNS_REDUNDANCY

                fun level1() {
                    register = setValue(BIT_RNS_REDUNDANCY, 0x0, register, MAX_VALUE)
                }

                fun level2() {
                    register = setValue(BIT_RNS_REDUNDANCY, 0x1, register, MAX_VALUE)
                }

                fun level3() {
                    register = setValue(BIT_RNS_REDUNDANCY, 0x2, register, MAX_VALUE)
                }

                fun level4() {
                    register = setValue(BIT_RNS_REDUNDANCY, 0x3, register, MAX_VALUE)
                }

                val value: Int
                    get() {
                        return getValue(
                            BIT_RNS_REDUNDANCY,
                            register,
                            MAX_VALUE
                        )
                    }
            }
        }

        object Delimiter {
            //private static int mDelimiterRegister = 0x1A28;
            var register: Int = default

            private val default: Int
                get() {
                    return (TransmissionFormat.defaultValue
                            or Delimiter1.defaultValue
                            or Delimiter2.defaultValue)
                }

            fun setDefault() {
                register = default
            }

            object Delimiter2 {
                var BIT_DELIMITER_DELIMITER2: Int = 9
                val defaultValue: Int = 0xD shl BIT_DELIMITER_DELIMITER2
                var MAX_VALUE: Int = 0x7F

                var value: Int
                    get() {
                        return getValue(
                            BIT_DELIMITER_DELIMITER2,
                            register,
                            MAX_VALUE
                        )
                    }
                    set(value) {
                        register =
                            setValue(
                                BIT_DELIMITER_DELIMITER2,
                                value,
                                register,
                                MAX_VALUE
                            )
                    }
            }

            object Delimiter1 {
                var BIT_DELIMITER_DELIMITER1: Int = 2
                val defaultValue: Int = 0xA shl BIT_DELIMITER_DELIMITER1
                var MAX_VALUE: Int = 0x7F

                var value: Int
                    get() {
                        return getValue(
                            BIT_DELIMITER_DELIMITER1,
                            register,
                            MAX_VALUE
                        )
                    }
                    set(value) {
                        register =
                            setValue(
                                BIT_DELIMITER_DELIMITER1,
                                value,
                                register,
                                MAX_VALUE
                            )
                    }
            }

            object TransmissionFormat {
                const val BIT_RNS_SECURITY: Int = 0
                const val MAX_VALUE: Int = 0x3
                val defaultValue: Int = 0 shl BIT_RNS_SECURITY

                fun dataAsIs() {
                    register = setValue(BIT_RNS_SECURITY, 0x0, register, MAX_VALUE)
                }

                fun setData1_Delimiter1_Data2() {
                    register = setValue(BIT_RNS_SECURITY, 0x1, register, MAX_VALUE)
                }

                fun setData1_Delimiter2_Data2() {
                    register = setValue(BIT_RNS_SECURITY, 0x2, register, MAX_VALUE)
                }

                fun setData1_Delimiter1_Delimiter2_Data2() {
                    register = setValue(BIT_RNS_SECURITY, 0x3, register, MAX_VALUE)
                }

                val value: Int
                    get() {
                        return getValue(
                            BIT_RNS_SECURITY,
                            register,
                            MAX_VALUE
                        )
                    }
            }
        }

        object Symbologies {
            fun setDefault() {
                OneD.setDefault()
                TwoD.setDefault()
                UPCEAN.setDefault()
                AdvancedConfig.OneD.Codabar.setDefault()
                Code11.setDefault()
                Code39.setDefault()
                Code93.setDefault()
                Code128.setDefault()
                Discrete2of5.setDefault()
                AdvancedConfig.OneD.GS1Databar.setDefault()
                Interleaved2of5.setDefault()
                AdvancedConfig.OneD.ISBN.setDefault()
                ISBT.setDefault()
                Matrix2of5.setDefault()
                AdvancedConfig.OneD.MSI.setDefault()
                AdvancedConfig.TwoD.Composite.setDefault()
                AdvancedConfig.TwoD.DataMatrix.setDefault()
                MacroPDF.setDefault()
                MicroPDF417.setDefault()
                PostalCodes.setDefault()
            }

            object OneD {
                //private static int mOneDRegisterValue = 0xE3C79B;
                var register: Int =
                    default

                private val default: Int
                    get() {
                        return ((UPC_A.defaultValue
                                or UPC_E0.defaultValue
                                or UPC_E1.defaultValue
                                or EAN_8.defaultValue
                                or EAN_13.defaultValue
                                or ISBN.defaultValue
                                or ISSN.defaultValue
                                or CODE_128.defaultValue
                                or GS1_128.defaultValue
                                or ISBT_128.defaultValue
                                or CODE_39.defaultValue
                                or Trioptic_39.defaultValue
                                or CODE_93.defaultValue
                                or CODE_11.defaultValue
                                or Interleaved_2of5.defaultValue
                                or Discrete_2of5.defaultValue
                                or Codabar.defaultValue
                                or (MSI.defaultValue)
                                or Chinese_2of5.defaultValue
                                or Matrix_2of5.defaultValue
                                or Korean_3of5.defaultValue
                                or GS1Databar.defaultValue
                                or GS1DatabarLimited.defaultValue
                                or GS1DatabarExpanded.defaultValue))
                    }

                fun setDefault() {
                    register =
                        default
                }

                object UPC_A {
                    const val BIT_UPC_A: Int = 0
                    val defaultValue: Int = ENABLE shl BIT_UPC_A

                    fun enable() {
                        register = enable(BIT_UPC_A, register)
                    }

                    fun disable() {
                        register = disable(BIT_UPC_A, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_UPC_A,
                                register
                            )
                        }
                }

                object UPC_E0 {
                    const val BIT_UPC_E0: Int = 1
                    val defaultValue: Int = ENABLE shl BIT_UPC_E0

                    fun enable() {
                        register = enable(BIT_UPC_E0, register)
                    }

                    fun disable() {
                        register = disable(BIT_UPC_E0, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_UPC_E0,
                                register
                            )
                        }
                }

                object UPC_E1 {
                    const val BIT_UPC_E1: Int = 2
                    val defaultValue: Int = DISABLE shl BIT_UPC_E1

                    fun enable() {
                        register = enable(BIT_UPC_E1, register)
                    }

                    fun disable() {
                        register = disable(BIT_UPC_E1, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_UPC_E1,
                                register
                            )
                        }
                }

                object EAN_8 {
                    const val BIT_EAN_8: Int = 3
                    val defaultValue: Int = ENABLE shl BIT_EAN_8

                    fun enable() {
                        register = enable(BIT_EAN_8, register)
                    }

                    fun disable() {
                        register = disable(BIT_EAN_8, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_EAN_8,
                                register
                            )
                        }
                }

                object EAN_13 {
                    const val BIT_EAN_13: Int = 4
                    val defaultValue: Int = ENABLE shl BIT_EAN_13

                    fun enable() {
                        register = enable(BIT_EAN_13, register)
                    }

                    fun disable() {
                        register = disable(BIT_EAN_13, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_EAN_13,
                                register
                            )
                        }
                }

                object ISBN {
                    const val BIT_ISBN: Int = 5
                    val defaultValue: Int = DISABLE shl BIT_ISBN

                    fun enable() {
                        register = enable(BIT_ISBN, register)
                    }

                    fun disable() {
                        register = disable(BIT_ISBN, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_ISBN,
                                register
                            )
                        }
                }

                object ISSN {
                    const val BIT_ISSN: Int = 6
                    val defaultValue: Int = DISABLE shl BIT_ISSN

                    fun enable() {
                        register = enable(BIT_ISSN, register)
                    }

                    fun disable() {
                        register = disable(BIT_ISSN, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_ISSN,
                                register
                            )
                        }
                }

                object CODE_128 {
                    const val BIT_CODE_128: Int = 7
                    val defaultValue: Int = ENABLE shl BIT_CODE_128

                    fun enable() {
                        register = enable(BIT_CODE_128, register)
                    }

                    fun disable() {
                        register = disable(BIT_CODE_128, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_CODE_128,
                                register
                            )
                        }
                }

                object GS1_128 {
                    const val BIT_GS1_128: Int = 8
                    val defaultValue: Int = ENABLE shl BIT_GS1_128

                    fun enable() {
                        register = enable(BIT_GS1_128, register)
                    }

                    fun disable() {
                        register = disable(BIT_GS1_128, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_GS1_128,
                                register
                            )
                        }
                }

                object ISBT_128 {
                    const val BIT_ISBT_128: Int = 9
                    val defaultValue: Int = ENABLE shl BIT_ISBT_128

                    fun enable() {
                        register = enable(BIT_ISBT_128, register)
                    }

                    fun disable() {
                        register = disable(BIT_ISBT_128, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_ISBT_128,
                                register
                            )
                        }
                }

                object CODE_39 {
                    const val BIT_CODE_39: Int = 10
                    val defaultValue: Int = ENABLE shl BIT_CODE_39

                    fun enable() {
                        register = enable(BIT_CODE_39, register)
                    }

                    fun disable() {
                        register = disable(BIT_CODE_39, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_CODE_39,
                                register
                            )
                        }
                }

                object Trioptic_39 {
                    const val BIT_TRIOPTIC_39: Int = 11
                    val defaultValue: Int = DISABLE shl BIT_TRIOPTIC_39

                    fun enable() {
                        register = enable(BIT_TRIOPTIC_39, register)
                    }

                    fun disable() {
                        register = disable(BIT_TRIOPTIC_39, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_TRIOPTIC_39,
                                register
                            )
                        }
                }

                object CODE_93 {
                    const val BIT_CODE_93: Int = 12
                    val defaultValue: Int = DISABLE shl BIT_CODE_93

                    fun enable() {
                        register = enable(BIT_CODE_93, register)
                    }

                    fun disable() {
                        register = disable(BIT_CODE_93, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_CODE_93,
                                register
                            )
                        }
                }

                object CODE_11 {
                    const val BIT_CODE_11: Int = 13
                    val defaultValue: Int = DISABLE shl BIT_CODE_11

                    fun enable() {
                        register = enable(BIT_CODE_11, register)
                    }

                    fun disable() {
                        register = disable(BIT_CODE_11, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_CODE_11,
                                register
                            )
                        }
                }

                object Interleaved_2of5 {
                    const val BIT_INTERLEAVED_2OF5: Int = 14
                    val defaultValue: Int = ENABLE shl BIT_INTERLEAVED_2OF5

                    fun enable() {
                        register = enable(BIT_INTERLEAVED_2OF5, register)
                    }

                    fun disable() {
                        register = disable(BIT_INTERLEAVED_2OF5, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_INTERLEAVED_2OF5,
                                register
                            )
                        }
                }

                object Discrete_2of5 {
                    const val BIT_DISCRETE_2OF5: Int = 15
                    val defaultValue: Int = ENABLE shl BIT_DISCRETE_2OF5

                    fun enable() {
                        register = enable(BIT_DISCRETE_2OF5, register)
                    }

                    fun disable() {
                        register = disable(BIT_DISCRETE_2OF5, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_DISCRETE_2OF5,
                                register
                            )
                        }
                }

                object Codabar {
                    const val BIT_CODABAR: Int = 16
                    val defaultValue: Int = ENABLE shl BIT_CODABAR

                    fun enable() {
                        register = enable(BIT_CODABAR, register)
                    }

                    fun disable() {
                        register = disable(BIT_CODABAR, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_CODABAR,
                                register
                            )
                        }
                }

                object MSI {
                    const val BIT_MSI: Int = 17
                    val defaultValue: Int = ENABLE shl BIT_MSI

                    fun enable() {
                        register = enable(BIT_MSI, register)
                    }

                    fun disable() {
                        register = disable(BIT_MSI, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_MSI,
                                register
                            )
                        }
                }

                object Chinese_2of5 {
                    val defaultValue: Int = DISABLE
                    const val BIT_CHINESE_2OF5: Int = 18

                    fun enable() {
                        register = enable(BIT_CHINESE_2OF5, register)
                    }

                    fun disable() {
                        register = disable(BIT_CHINESE_2OF5, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_CHINESE_2OF5,
                                register
                            )
                        }
                }

                object Matrix_2of5 {
                    val defaultValue: Int = DISABLE
                    const val BIT_MATRIX_2OF5: Int = 19

                    fun enable() {
                        register = enable(BIT_MATRIX_2OF5, register)
                    }

                    fun disable() {
                        register = disable(BIT_MATRIX_2OF5, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_MATRIX_2OF5,
                                register
                            )
                        }
                }

                object Korean_3of5 {
                    val defaultValue: Int = DISABLE
                    const val BIT_KOREAN_2OF5: Int = 20

                    fun enable() {
                        register = enable(BIT_KOREAN_2OF5, register)
                    }

                    fun disable() {
                        register = disable(BIT_KOREAN_2OF5, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_KOREAN_2OF5,
                                register
                            )
                        }
                }

                object GS1Databar {
                    const val BIT_GS1_DATABAR: Int = 21
                    val defaultValue: Int = ENABLE shl BIT_GS1_DATABAR

                    fun enable() {
                        register = enable(BIT_GS1_DATABAR, register)
                    }

                    fun disable() {
                        register = disable(BIT_GS1_DATABAR, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_GS1_DATABAR,
                                register
                            )
                        }
                }

                object GS1DatabarLimited {
                    const val BIT_GS1_DATABAR_LIMITED: Int = 22
                    val defaultValue: Int = ENABLE shl BIT_GS1_DATABAR_LIMITED

                    fun enable() {
                        register = enable(BIT_GS1_DATABAR_LIMITED, register)
                    }

                    fun disable() {
                        register = disable(BIT_GS1_DATABAR_LIMITED, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_GS1_DATABAR_LIMITED,
                                register
                            )
                        }
                }

                object GS1DatabarExpanded {
                    const val BIT_GS1_DATABAR_EXPANDED: Int = 23
                    val defaultValue: Int = ENABLE shl BIT_GS1_DATABAR_EXPANDED

                    fun enable() {
                        register = enable(BIT_GS1_DATABAR_EXPANDED, register)
                    }

                    fun disable() {
                        register = disable(BIT_GS1_DATABAR_EXPANDED, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_GS1_DATABAR_EXPANDED,
                                register
                            )
                        }
                }
            }

            object TwoD {
                //private static int mTwoDRegisterValue = 0x3B800;
                var register: Int =
                    default

                private val default: Int
                    get() {
                        return (USPostnet.defaultValue
                                or USPlanet.defaultValue
                                or UKPostal.defaultValue
                                or JapanPostal.defaultValue
                                or AustraliaPost.defaultValue
                                or NetherlandsKixCode.defaultValue
                                or InteligentMail.defaultValue
                                or UPU_FICS_Postal.defaultValue
                                or CompositeCC_C.defaultValue
                                or CompositeCC_AB.defaultValue
                                or CompositeTLC_39.defaultValue
                                or PDF_417.defaultValue
                                or MicroPDF_417.defaultValue
                                or DataMatrix.defaultValue
                                or MaxiCode.defaultValue
                                or QRCode.defaultValue
                                or MicroQR.defaultValue
                                or Aztec.defaultValue
                                or HanXin.defaultValue)
                    }

                fun setDefault() {
                    register =
                        default
                }

                object USPostnet {
                    const val BIT_US_POSTNET: Int = 0
                    val defaultValue: Int = DISABLE shl BIT_US_POSTNET

                    fun enable() {
                        register = enable(BIT_US_POSTNET, register)
                    }

                    fun disable() {
                        register = disable(BIT_US_POSTNET, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_US_POSTNET,
                                register
                            )
                        }
                }

                object USPlanet {
                    const val BIT_US_PLANET: Int = 1
                    val defaultValue: Int = DISABLE shl BIT_US_PLANET

                    fun enable() {
                        register = enable(BIT_US_PLANET, register)
                    }

                    fun disable() {
                        register = disable(BIT_US_PLANET, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_US_PLANET,
                                register
                            )
                        }
                }

                object UKPostal {
                    const val BIT_UK_POSTAL: Int = 2
                    val defaultValue: Int = DISABLE shl BIT_UK_POSTAL

                    fun enable() {
                        register = enable(BIT_UK_POSTAL, register)
                    }

                    fun disable() {
                        register = disable(BIT_UK_POSTAL, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_UK_POSTAL,
                                register
                            )
                        }
                }

                object JapanPostal {
                    const val BIT_JAPAN_POSTAL: Int = 3
                    val defaultValue: Int = DISABLE shl BIT_JAPAN_POSTAL

                    fun enable() {
                        register = enable(BIT_JAPAN_POSTAL, register)
                    }

                    fun disable() {
                        register = disable(BIT_JAPAN_POSTAL, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_JAPAN_POSTAL,
                                register
                            )
                        }
                }

                object AustraliaPost {
                    const val BIT_AUSTRALIA_POST: Int = 4
                    val defaultValue: Int = DISABLE shl BIT_AUSTRALIA_POST

                    fun enable() {
                        register = enable(BIT_AUSTRALIA_POST, register)
                    }

                    fun disable() {
                        register = disable(BIT_AUSTRALIA_POST, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_AUSTRALIA_POST,
                                register
                            )
                        }
                }

                object NetherlandsKixCode {
                    const val BIT_NETHERLANDS_KIX_CODE: Int = 5
                    val defaultValue: Int = DISABLE shl BIT_NETHERLANDS_KIX_CODE

                    fun enable() {
                        register = enable(BIT_NETHERLANDS_KIX_CODE, register)
                    }

                    fun disable() {
                        register = disable(BIT_NETHERLANDS_KIX_CODE, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_NETHERLANDS_KIX_CODE,
                                register
                            )
                        }
                }

                object InteligentMail {
                    const val BIT_INTELIGENT_MAIL: Int = 6
                    val defaultValue: Int = DISABLE shl BIT_INTELIGENT_MAIL

                    fun enable() {
                        register = enable(BIT_INTELIGENT_MAIL, register)
                    }

                    fun disable() {
                        register = disable(BIT_INTELIGENT_MAIL, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_INTELIGENT_MAIL,
                                register
                            )
                        }
                }

                object UPU_FICS_Postal {
                    const val BIT_UPU_FICS_POSTAL: Int = 7
                    val defaultValue: Int = DISABLE shl BIT_UPU_FICS_POSTAL

                    fun enable() {
                        register = enable(BIT_UPU_FICS_POSTAL, register)
                    }

                    fun disable() {
                        register = disable(BIT_UPU_FICS_POSTAL, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_UPU_FICS_POSTAL,
                                register
                            )
                        }
                }

                object CompositeCC_C {
                    const val BIT_COMPOSITE_CC_C: Int = 8
                    val defaultValue: Int = DISABLE shl BIT_COMPOSITE_CC_C

                    fun enable() {
                        register = enable(BIT_COMPOSITE_CC_C, register)
                    }

                    fun disable() {
                        register = disable(BIT_COMPOSITE_CC_C, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_COMPOSITE_CC_C,
                                register
                            )
                        }
                }

                object CompositeCC_AB {
                    const val BIT_COMPOSITE_CC_AB: Int = 9
                    val defaultValue: Int = DISABLE shl BIT_COMPOSITE_CC_AB

                    fun enable() {
                        register = enable(BIT_COMPOSITE_CC_AB, register)
                    }

                    fun disable() {
                        register = disable(BIT_COMPOSITE_CC_AB, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_COMPOSITE_CC_AB,
                                register
                            )
                        }
                }

                object CompositeTLC_39 {
                    const val BIT_COMPOSITE_TLC_39: Int = 10
                    val defaultValue: Int = DISABLE shl BIT_COMPOSITE_TLC_39

                    fun enable() {
                        register = enable(BIT_COMPOSITE_TLC_39, register)
                    }

                    fun disable() {
                        register = disable(BIT_COMPOSITE_TLC_39, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_COMPOSITE_TLC_39,
                                register
                            )
                        }
                }

                object PDF_417 {
                    const val BIT_PDF_417: Int = 11
                    val defaultValue: Int = ENABLE shl BIT_PDF_417

                    fun enable() {
                        register = enable(BIT_PDF_417, register)
                    }

                    fun disable() {
                        register = disable(BIT_PDF_417, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_PDF_417,
                                register
                            )
                        }
                }

                object MicroPDF_417 {
                    const val BIT_MICRO_PDF_417: Int = 12
                    val defaultValue: Int = ENABLE shl BIT_MICRO_PDF_417

                    fun enable() {
                        register = enable(BIT_MICRO_PDF_417, register)
                    }

                    fun disable() {
                        register = disable(BIT_MICRO_PDF_417, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_MICRO_PDF_417,
                                register
                            )
                        }
                }

                object DataMatrix {
                    const val BIT_DATA_MATRIX: Int = 13
                    val defaultValue: Int = ENABLE shl BIT_DATA_MATRIX

                    fun enable() {
                        register = enable(BIT_DATA_MATRIX, register)
                    }

                    fun disable() {
                        register = disable(BIT_DATA_MATRIX, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_DATA_MATRIX,
                                register
                            )
                        }
                }

                object MaxiCode {
                    const val BIT_MAXI_CODE: Int = 14
                    val defaultValue: Int = DISABLE shl BIT_MAXI_CODE

                    fun enable() {
                        register = enable(BIT_MAXI_CODE, register)
                    }

                    fun disable() {
                        register = disable(BIT_MAXI_CODE, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_MAXI_CODE,
                                register
                            )
                        }
                }

                object QRCode {
                    const val BIT_QR_CODE: Int = 15
                    val defaultValue: Int = ENABLE shl BIT_QR_CODE

                    fun enable() {
                        register = enable(BIT_QR_CODE, register)
                    }

                    fun disable() {
                        register = disable(BIT_QR_CODE, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_QR_CODE,
                                register
                            )
                        }
                }

                object MicroQR {
                    const val BIT_MICRO_QR: Int = 16
                    val defaultValue: Int = ENABLE shl BIT_MICRO_QR

                    fun enable() {
                        register = enable(BIT_MICRO_QR, register)
                    }

                    fun disable() {
                        register = disable(BIT_MICRO_QR, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_MICRO_QR,
                                register
                            )
                        }
                }

                object Aztec {
                    const val BIT_AZTEC: Int = 17
                    val defaultValue: Int = ENABLE shl BIT_AZTEC

                    fun enable() {
                        register = enable(BIT_AZTEC, register)
                    }

                    fun disable() {
                        register = disable(BIT_AZTEC, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_AZTEC,
                                register
                            )
                        }
                }

                object HanXin {
                    const val BIT_HANXIN: Int = 18
                    val defaultValue: Int = DISABLE shl BIT_HANXIN

                    fun enable() {
                        register = enable(BIT_HANXIN, register)
                    }

                    fun disable() {
                        register = disable(BIT_HANXIN, register)
                    }

                    val isEnable: Boolean
                        get() {
                            return isEnable(
                                BIT_HANXIN,
                                register
                            )
                        }
                }
            }

            class AdvancedConfig {
                class OneD {
                    object UPCEAN {
                        //private static int mUPCEANRegisterValue = 0x42720AF;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (TransmitCheckDigit.UPC_A.defaultValue
                                        or TransmitCheckDigit.UPC_E0.defaultValue
                                        or TransmitCheckDigit.UPC_E1.defaultValue
                                        or Preamble.UPC_A.defaultValue
                                        or Preamble.UPC_E0.defaultValue
                                        or Preamble.UPC_E1.defaultValue
                                        or UPC_E0ToA.defaultValue
                                        or UPC_E1ToA.defaultValue
                                        or UPCReducedQuietZone.defaultValue
                                        or SupplementalRedundancy.defaultValue
                                        or SupplementalAIMIDFormat.defaultValue
                                        or DecodeSupplementals.defaultValue
                                        or EAN8Extend.defaultValue
                                        or UCCCouponExtendedCode.defaultValue
                                        or CouponReport.defaultValue)
                            }

                        fun setDefault() {
                            register = default
                        }

                        class TransmitCheckDigit {
                            object UPC_A {
                                const val BIT_T_UPC_A: Int = 0
                                val defaultValue: Int = ENABLE shl BIT_T_UPC_A

                                fun enable() {
                                    register = enable(BIT_T_UPC_A, register)
                                }

                                fun disable() {
                                    register = disable(BIT_T_UPC_A, register)
                                }

                                val isEnable: Boolean
                                    get() {
                                        return isEnable(
                                            BIT_T_UPC_A,
                                            register
                                        )
                                    }
                            }

                            object UPC_E0 {
                                const val BIT_T_UPC_E0: Int = 1
                                val defaultValue: Int = ENABLE shl BIT_T_UPC_E0

                                fun enable() {
                                    register = enable(BIT_T_UPC_E0, register)
                                }

                                fun disable() {
                                    register = disable(BIT_T_UPC_E0, register)
                                }

                                val isEnable: Boolean
                                    get() {
                                        return isEnable(
                                            BIT_T_UPC_E0,
                                            register
                                        )
                                    }
                            }

                            object UPC_E1 {
                                const val BIT_T_UPC_E1: Int = 2
                                val defaultValue: Int = ENABLE shl BIT_T_UPC_E1

                                fun enable() {
                                    register = enable(BIT_T_UPC_E1, register)
                                }

                                fun disable() {
                                    register = disable(BIT_T_UPC_E1, register)
                                }

                                val isEnable: Boolean
                                    get() {
                                        return isEnable(
                                            BIT_T_UPC_E1,
                                            register
                                        )
                                    }
                            }
                        }

                        class Preamble {
                            object UPC_A {
                                const val BIT_P_UPC_A: Int = 3
                                val defaultValue: Int = 1 shl BIT_P_UPC_A
                                const val MAX_VALUE: Int = 0x3

                                fun setNo() {
                                    register = setValue(BIT_P_UPC_A, 0x0, register, MAX_VALUE)
                                }

                                fun setSystemChar() {
                                    register = setValue(BIT_P_UPC_A, 0x1, register, MAX_VALUE)
                                }

                                fun setSystemCharAndCountryCode() {
                                    register = setValue(BIT_P_UPC_A, 0x2, register, MAX_VALUE)
                                }

                                val value: Int
                                    get() {
                                        return getValue(
                                            BIT_P_UPC_A,
                                            register,
                                            MAX_VALUE
                                        )
                                    }
                            }

                            object UPC_E0 {
                                const val BIT_P_UPC_E0: Int = 5
                                val defaultValue: Int = 1 shl BIT_P_UPC_E0
                                const val MAX_VALUE: Int = 0x3

                                fun setNo() {
                                    register = setValue(BIT_P_UPC_E0, 0x0, register, MAX_VALUE)
                                }

                                fun setSystemChar() {
                                    register = setValue(BIT_P_UPC_E0, 0x1, register, MAX_VALUE)
                                }

                                fun setSystemCharAndCountryCode() {
                                    register = setValue(BIT_P_UPC_E0, 0x2, register, MAX_VALUE)
                                }

                                val value: Int
                                    get() {
                                        return getValue(
                                            BIT_P_UPC_E0,
                                            register,
                                            MAX_VALUE
                                        )
                                    }
                            }

                            object UPC_E1 {
                                const val BIT_P_UPC_E1: Int = 7
                                val defaultValue: Int = 1 shl BIT_P_UPC_E1
                                const val MAX_VALUE: Int = 0x3

                                fun setNo() {
                                    register = setValue(BIT_P_UPC_E1, 0x0, register, MAX_VALUE)
                                }

                                fun setSystemChar() {
                                    register = setValue(BIT_P_UPC_E1, 0x1, register, MAX_VALUE)
                                }

                                fun setSystemCharAndCountryCode() {
                                    register = setValue(BIT_P_UPC_E1, 0x2, register, MAX_VALUE)
                                }

                                val value: Int
                                    get() {
                                        return getValue(
                                            BIT_P_UPC_E1,
                                            register,
                                            MAX_VALUE
                                        )
                                    }
                            }
                        }

                        class Convert {
                            object UPC_E0ToA {
                                const val BIT_T_UPC_E0: Int = 9
                                val defaultValue: Int = DISABLE shl BIT_T_UPC_E0

                                fun enable() {
                                    register = enable(BIT_T_UPC_E0, register)
                                }

                                fun disable() {
                                    register = disable(BIT_T_UPC_E0, register)
                                }

                                val isEnable: Boolean
                                    get() {
                                        return isEnable(
                                            BIT_T_UPC_E0,
                                            register
                                        )
                                    }
                            }

                            object UPC_E1ToA {
                                const val BIT_T_UPC_E1: Int = 10
                                val defaultValue: Int = DISABLE shl BIT_T_UPC_E1

                                fun enable() {
                                    register = enable(BIT_T_UPC_E1, register)
                                }

                                fun disable() {
                                    register = disable(BIT_T_UPC_E1, register)
                                }

                                val isEnable: Boolean
                                    get() {
                                        return isEnable(
                                            BIT_T_UPC_E1,
                                            register
                                        )
                                    }
                            }
                        }

                        object UPCReducedQuietZone {
                            const val BIT_UPC_REDUCE: Int = 11
                            val defaultValue: Int = DISABLE shl BIT_UPC_REDUCE

                            fun enable() {
                                register = enable(BIT_UPC_REDUCE, register)
                            }

                            fun disable() {
                                register = disable(BIT_UPC_REDUCE, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_UPC_REDUCE,
                                        register
                                    )
                                }
                        }

                        object DecodeSupplementals {
                            const val BIT_DECODE_SUPP: Int = 12
                            val defaultValue: Int = 2 shl BIT_DECODE_SUPP
                            const val MAX_VALUE: Int = 0xF

                            fun ignoreSuppData() {
                                register = setValue(BIT_DECODE_SUPP, 0x0, register, MAX_VALUE)
                            }

                            fun onlyReadIncludeSuppData() {
                                register = setValue(BIT_DECODE_SUPP, 0x1, register, MAX_VALUE)
                            }

                            fun readDataNoMatterSupp() {
                                register = setValue(BIT_DECODE_SUPP, 0x2, register, MAX_VALUE)
                            }

                            fun allPrefixEnable() {
                                register = setValue(BIT_DECODE_SUPP, 0x3, register, MAX_VALUE)
                            }

                            fun prefix_378_397_ofEAN13() {
                                register = setValue(BIT_DECODE_SUPP, 0x4, register, MAX_VALUE)
                            }

                            fun prefix_978_979_ofEAN13() {
                                register = setValue(BIT_DECODE_SUPP, 0x5, register, MAX_VALUE)
                            }

                            fun prefix_414_419_434_439_ofEAN13() {
                                register = setValue(BIT_DECODE_SUPP, 0x6, register, MAX_VALUE)
                            }

                            fun prefix_977_ofEAN13() {
                                register = setValue(BIT_DECODE_SUPP, 0x7, register, MAX_VALUE)
                            }

                            fun prefix_491_ofEAN13() {
                                register = setValue(BIT_DECODE_SUPP, 0x8, register, MAX_VALUE)
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_DECODE_SUPP,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }

                        object SupplementalRedundancy {
                            const val BIT_SUPP_REDUNDANCY: Int = 16
                            val defaultValue: Int = 7 shl BIT_SUPP_REDUNDANCY
                            const val MAX_VALUE: Int = 0x1F

                            var redundancy: Int
                                get() {
                                    return getValue(
                                        BIT_SUPP_REDUNDANCY,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_SUPP_REDUNDANCY,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }

                        object SupplementalAIMIDFormat {
                            const val BIT_SUPP_AIM_ID_FORMAT: Int = 21
                            val defaultValue: Int = 1 shl BIT_SUPP_AIM_ID_FORMAT
                            const val MAX_VALUE: Int = 0x3

                            fun setSeparate() {
                                register =
                                    setValue(BIT_SUPP_AIM_ID_FORMAT, 0x0, register, MAX_VALUE)
                            }

                            fun setCombined() {
                                register =
                                    setValue(BIT_SUPP_AIM_ID_FORMAT, 0x1, register, MAX_VALUE)
                            }

                            fun setSeparateTransmissions() {
                                register =
                                    setValue(BIT_SUPP_AIM_ID_FORMAT, 0x2, register, MAX_VALUE)
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_SUPP_AIM_ID_FORMAT,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }

                        object EAN8Extend {
                            const val BIT_EAN8_EXT: Int = 23
                            val defaultValue: Int = DISABLE shl BIT_EAN8_EXT

                            fun enable() {
                                register = enable(BIT_EAN8_EXT, register)
                            }

                            fun disable() {
                                register = disable(BIT_EAN8_EXT, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_EAN8_EXT,
                                        register
                                    )
                                }
                        }

                        object UCCCouponExtendedCode {
                            const val BIT_UCC_COUPON_EXT: Int = 24
                            val defaultValue: Int = DISABLE shl BIT_UCC_COUPON_EXT

                            fun enable() {
                                register = enable(BIT_UCC_COUPON_EXT, register)
                            }

                            fun disable() {
                                register = disable(BIT_UCC_COUPON_EXT, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_UCC_COUPON_EXT,
                                        register
                                    )
                                }
                        }

                        object CouponReport {
                            const val BIT_COUPON_REPORT: Int = 25
                            val defaultValue: Int = 2 shl BIT_COUPON_REPORT
                            const val MAX_VALUE: Int = 0x3

                            fun setOldCouponSymbols() {
                                register = setValue(BIT_COUPON_REPORT, 0x0, register, MAX_VALUE)
                            }

                            fun setNewCouponSymbols() {
                                register = setValue(BIT_COUPON_REPORT, 0x1, register, MAX_VALUE)
                            }

                            fun setBothCouponSymbols() {
                                register = setValue(BIT_COUPON_REPORT, 0x2, register, MAX_VALUE)
                            }

                            val report: Int
                                get() {
                                    return getValue(
                                        BIT_COUPON_REPORT,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }
                    }

                    object ISBN {
                        private const val default: Int = 0

                        //private static int mBooklandRegister = 0;
                        var format: Int =
                            default

                        fun setDefault() {
                            format =
                                default
                        }

                        fun setISBN10() {
                            format = 0
                        }

                        fun setISBN13() {
                            format = 1
                        }
                    }

                    object Code128 {
                        //private static int mCode128Register = 0;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (Length.defaultLengtheadlineMedium
                                        or Length.defaultLength2
                                        or ReducedQuietZone.defaultValue
                                        or IgnoreCode128FNC4.defaultValue)
                            }

                        fun setDefault() {
                            register = default
                        }

                        object IgnoreCode128FNC4 {
                            const val BIT_CODE128_FNC4: Int = 17
                            val defaultValue: Int = DISABLE shl BIT_CODE128_FNC4

                            fun enable() {
                                register = enable(BIT_CODE128_FNC4, register)
                            }

                            fun disable() {
                                register = disable(BIT_CODE128_FNC4, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_CODE128_FNC4,
                                        register
                                    )
                                }
                        }

                        object ReducedQuietZone {
                            const val BIT_CODE128_FNC4: Int = 16
                            val defaultValue: Int = DISABLE shl BIT_CODE128_FNC4

                            fun enable() {
                                register = enable(BIT_CODE128_FNC4, register)
                            }

                            fun disable() {
                                register = disable(BIT_CODE128_FNC4, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_CODE128_FNC4,
                                        register
                                    )
                                }
                        }

                        object Length {
                            const val BIT_LENGTheadlineMedium: Int = 0
                            const val BIT_LENGTH2: Int = 8
                            val defaultLengtheadlineMedium: Int = 1 shl BIT_LENGTheadlineMedium
                            val defaultLength2: Int = 55 shl BIT_LENGTH2
                            const val MAX_VALUE: Int = 0xFF

                            var l1: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTheadlineMedium,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTheadlineMedium,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }

                            var l2: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTH2,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTH2,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }
                    }

                    object ISBT {
                        //private static int mISBTRegister = 0x54;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (ISBTConcatenation.defaultValue
                                        or ISBTConcatenationRedundancy.defaultValue
                                        or CheckISBTTable.defaultValue)
                            }

                        fun setDefault() {
                            register = default
                        }

                        object ISBTConcatenationRedundancy {
                            const val BIT_ISBT_REDUNDANCY: Int = 3
                            val defaultValue: Int = 10 shl BIT_ISBT_REDUNDANCY
                            const val MAX_VALUE: Int = 0x1F

                            var value: Int
                                get() {
                                    return getValue(
                                        BIT_ISBT_REDUNDANCY,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_ISBT_REDUNDANCY,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }

                        object CheckISBTTable {
                            const val BIT_ISBT_TABLE: Int = 2
                            val defaultValue: Int = ENABLE shl BIT_ISBT_TABLE

                            fun enable() {
                                register = enable(BIT_ISBT_TABLE, register)
                            }

                            fun disable() {
                                register = disable(BIT_ISBT_TABLE, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_ISBT_TABLE,
                                        register
                                    )
                                }
                        }

                        object ISBTConcatenation {
                            const val BIT_ISBT_CONCATENATION: Int = 0
                            val defaultValue: Int = 0 shl BIT_ISBT_CONCATENATION
                            const val MAX_VALUE: Int = 0x3

                            fun disable() {
                                register =
                                    setValue(BIT_ISBT_CONCATENATION, 0x0, register, MAX_VALUE)
                            }

                            fun enable() {
                                register =
                                    setValue(BIT_ISBT_CONCATENATION, 0x1, register, MAX_VALUE)
                            }

                            fun setAutodiscriminate() {
                                register =
                                    setValue(BIT_ISBT_CONCATENATION, 0x2, register, MAX_VALUE)
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_ISBT_CONCATENATION,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }
                    }

                    object Code39 {
                        //private static int mCode39RegisterValue = 0x80000;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (ConvertCode39to32.defaultValue
                                        or Code32AddPrefix_A.defaultValue
                                        or Length.defaultLengtheadlineMedium
                                        or Length.defaultLength2
                                        or CheckDigitVerification.defaultValue
                                        or Code39FullASCIIConversion.defaultValue
                                        or Code39BufferingScanStore.defaultValue
                                        or Code39ReducedQuietZone.defaultValue)
                            }

                        fun setDefault() {
                            register = default
                        }

                        object ConvertCode39to32 {
                            const val BIT_CONVERT_CODE39_TO_32: Int = 0
                            val defaultValue: Int = DISABLE shl BIT_CONVERT_CODE39_TO_32

                            fun enable() {
                                register = enable(BIT_CONVERT_CODE39_TO_32, register)
                            }

                            fun disable() {
                                register = disable(BIT_CONVERT_CODE39_TO_32, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_CONVERT_CODE39_TO_32,
                                        register
                                    )
                                }
                        }

                        object Code32AddPrefix_A {
                            const val BIT_CODE32_ADDPREFIX_A: Int = 1
                            val defaultValue: Int = DISABLE shl BIT_CODE32_ADDPREFIX_A

                            fun enable() {
                                register = enable(BIT_CODE32_ADDPREFIX_A, register)
                            }

                            fun disable() {
                                register = disable(BIT_CODE32_ADDPREFIX_A, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_CODE32_ADDPREFIX_A,
                                        register
                                    )
                                }
                        }

                        object Length {
                            const val BIT_LENGTheadlineMedium: Int = 2
                            const val BIT_LENGTH2: Int = 10
                            val defaultLengtheadlineMedium: Int = 1 shl BIT_LENGTheadlineMedium
                            val defaultLength2: Int = 55 shl BIT_LENGTH2
                            const val MAX_VALUE: Int = 0xFF

                            var l1: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTheadlineMedium,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTheadlineMedium,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }

                            var l2: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTH2,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTH2,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }

                        object CheckDigitVerification {
                            const val BIT_CODE39_VERIFICATION_CHECKDIGIT: Int = 18
                            val defaultValue: Int = DISABLE shl BIT_CODE39_VERIFICATION_CHECKDIGIT
                            const val MAX_VALUE: Int = 0x3

                            fun disable() {
                                register = setValue(
                                    BIT_CODE39_VERIFICATION_CHECKDIGIT,
                                    0x0,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            fun setDoNotTransmit() {
                                register = setValue(
                                    BIT_CODE39_VERIFICATION_CHECKDIGIT,
                                    0x1,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            fun setTransmit() {
                                register = setValue(
                                    BIT_CODE39_VERIFICATION_CHECKDIGIT,
                                    0x2,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_CODE39_VERIFICATION_CHECKDIGIT,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }

                        object Code39FullASCIIConversion {
                            const val BIT_CODE39_ASCII_CONVERSION: Int = 20
                            val defaultValue: Int = DISABLE shl BIT_CODE39_ASCII_CONVERSION

                            fun enable() {
                                register = enable(BIT_CODE39_ASCII_CONVERSION, register)
                            }

                            fun disable() {
                                register = disable(BIT_CODE39_ASCII_CONVERSION, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_CODE39_ASCII_CONVERSION,
                                        register
                                    )
                                }
                        }

                        object Code39BufferingScanStore {
                            const val BIT_CODE39_BUFFERING: Int = 21
                            val defaultValue: Int = DISABLE shl BIT_CODE39_BUFFERING

                            fun enable() {
                                register = enable(BIT_CODE39_BUFFERING, register)
                            }

                            fun disable() {
                                register = disable(BIT_CODE39_BUFFERING, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_CODE39_BUFFERING,
                                        register
                                    )
                                }
                        }

                        object Code39ReducedQuietZone {
                            const val BIT_CODE39_QUIET_ZONE: Int = 22
                            val defaultValue: Int = DISABLE shl BIT_CODE39_QUIET_ZONE

                            fun enable() {
                                register = enable(BIT_CODE39_QUIET_ZONE, register)
                            }

                            fun disable() {
                                register = disable(BIT_CODE39_QUIET_ZONE, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_CODE39_QUIET_ZONE,
                                        register
                                    )
                                }
                        }
                    }

                    object Code93 {
                        //private static int mCode93RegisterValue = 0;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (Length.defaultLengtheadlineMedium
                                        or Length.defaultLength2)
                            }

                        fun setDefault() {
                            register = default
                        }

                        object Length {
                            const val BIT_LENGTheadlineMedium: Int = 0
                            const val BIT_LENGTH2: Int = 8
                            val defaultLengtheadlineMedium: Int = 1 shl BIT_LENGTheadlineMedium
                            val defaultLength2: Int = 55 shl BIT_LENGTH2
                            const val MAX_VALUE: Int = 0xFF

                            var l1: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTheadlineMedium,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTheadlineMedium,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }

                            var l2: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTH2,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTH2,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }
                    }

                    object Code11 {
                        //private static int mCode11RegisterValue = 0x40000;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (Length.defaultLengtheadlineMedium
                                        or Length.defaultLength2
                                        or CheckDigitVerification.defaultValue
                                        or TransmitCheckDigit.defaultValue)
                            }

                        fun setDefault() {
                            register = default
                        }

                        object Length {
                            const val BIT_LENGTheadlineMedium: Int = 0
                            const val BIT_LENGTH2: Int = 8
                            val defaultLengtheadlineMedium: Int = 1 shl BIT_LENGTheadlineMedium
                            val defaultLength2: Int = 55 shl BIT_LENGTH2
                            const val MAX_VALUE: Int = 0xFF

                            var l1: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTheadlineMedium,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTheadlineMedium,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }

                            var l2: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTH2,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTH2,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }

                        object CheckDigitVerification {
                            const val BIT_CODE11_VERIFICATION_CHECKDIGIT: Int = 16
                            val defaultValue: Int = DISABLE shl BIT_CODE11_VERIFICATION_CHECKDIGIT
                            const val MAX_VALUE: Int = 0x3

                            fun disable() {
                                register = setValue(
                                    BIT_CODE11_VERIFICATION_CHECKDIGIT,
                                    0x0,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            fun oneCheckDigit() {
                                register = setValue(
                                    BIT_CODE11_VERIFICATION_CHECKDIGIT,
                                    0x1,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            fun twoCheckDigit() {
                                register = setValue(
                                    BIT_CODE11_VERIFICATION_CHECKDIGIT,
                                    0x2,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_CODE11_VERIFICATION_CHECKDIGIT,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }

                        object TransmitCheckDigit {
                            const val BIT_CODE11_TRANSMIT_CHECKDIGIT: Int = 18
                            val defaultValue: Int = ENABLE shl BIT_CODE11_TRANSMIT_CHECKDIGIT

                            fun enable() {
                                register = enable(BIT_CODE11_TRANSMIT_CHECKDIGIT, register)
                            }

                            fun disable() {
                                register = disable(BIT_CODE11_TRANSMIT_CHECKDIGIT, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_CODE11_TRANSMIT_CHECKDIGIT,
                                        register
                                    )
                                }
                        }
                    }

                    object Interleaved2of5 {
                        //private static int mI2of5RegisterValue = 0x140000;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (Length.defaultLengtheadlineMedium
                                        or Length.defaultLength2
                                        or CheckDigitVerification.defaultValue
                                        or ConvertI2of5ToEAN13.defaultValue
                                        or SecurityLevel.defaultValue
                                        or ReducedQuietZone.defaultValue)
                            }

                        fun setDefault() {
                            register = default
                        }

                        object Length {
                            const val BIT_LENGTheadlineMedium: Int = 0
                            const val BIT_LENGTH2: Int = 8
                            val defaultLengtheadlineMedium: Int = 1 shl BIT_LENGTheadlineMedium
                            val defaultLength2: Int = 55 shl BIT_LENGTH2
                            const val MAX_VALUE: Int = 0xFF

                            var l1: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTheadlineMedium,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTheadlineMedium,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }

                            var l2: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTH2,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTH2,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }

                        object CheckDigitVerification {
                            const val BIT_I2OF5_VERIFICATION_CHECKDIGIT: Int = 16
                            val defaultValue: Int = 0 shl BIT_I2OF5_VERIFICATION_CHECKDIGIT
                            const val MAX_VALUE: Int = 0x7

                            fun disable() {
                                register = setValue(
                                    BIT_I2OF5_VERIFICATION_CHECKDIGIT,
                                    0x0,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            fun setDoNotTransmitUSS() {
                                register = setValue(
                                    BIT_I2OF5_VERIFICATION_CHECKDIGIT,
                                    0x1,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            fun setTransmitUSS() {
                                register = setValue(
                                    BIT_I2OF5_VERIFICATION_CHECKDIGIT,
                                    0x2,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            fun setDoNotTransmitOPCC() {
                                register = setValue(
                                    BIT_I2OF5_VERIFICATION_CHECKDIGIT,
                                    0x3,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            fun setTransmitOPCC() {
                                register = setValue(
                                    BIT_I2OF5_VERIFICATION_CHECKDIGIT,
                                    0x4,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_I2OF5_VERIFICATION_CHECKDIGIT,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }

                        object ConvertI2of5ToEAN13 {
                            const val BIT_I2OF5_TO_EAN13: Int = 19
                            val defaultValue: Int = DISABLE shl BIT_I2OF5_TO_EAN13

                            fun enable() {
                                register = enable(BIT_I2OF5_TO_EAN13, register)
                            }

                            fun disable() {
                                register = disable(BIT_I2OF5_TO_EAN13, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_I2OF5_TO_EAN13,
                                        register
                                    )
                                }
                        }

                        object SecurityLevel {
                            const val BIT_I2OF5_SECURITY: Int = 20
                            val defaultValue: Int = 1 shl BIT_I2OF5_SECURITY
                            const val MAX_VALUE: Int = 0x3

                            fun level0() {
                                register = setValue(BIT_I2OF5_SECURITY, 0x0, register, MAX_VALUE)
                            }

                            fun level1() {
                                register = setValue(BIT_I2OF5_SECURITY, 0x1, register, MAX_VALUE)
                            }

                            fun level2() {
                                register = setValue(BIT_I2OF5_SECURITY, 0x2, register, MAX_VALUE)
                            }

                            fun level3() {
                                register = setValue(BIT_I2OF5_SECURITY, 0x3, register, MAX_VALUE)
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_I2OF5_SECURITY,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }

                        object ReducedQuietZone {
                            const val BIT_I2OF5_QUIET_ZONE: Int = 22
                            val defaultValue: Int = DISABLE shl BIT_I2OF5_QUIET_ZONE

                            fun enable() {
                                register = enable(BIT_I2OF5_QUIET_ZONE, register)
                            }

                            fun disable() {
                                register = disable(BIT_I2OF5_QUIET_ZONE, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_I2OF5_QUIET_ZONE,
                                        register
                                    )
                                }
                        }
                    }

                    object Discrete2of5 {
                        //private static int mD2of5RegisterValue = 0;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (Length.defaultLengtheadlineMedium
                                        or Length.defaultLength2)
                            }

                        fun setDefault() {
                            register = default
                        }

                        object Length {
                            const val BIT_LENGTheadlineMedium: Int = 0
                            const val BIT_LENGTH2: Int = 8
                            val defaultLengtheadlineMedium: Int = 1 shl BIT_LENGTheadlineMedium
                            val defaultLength2: Int = 55 shl BIT_LENGTH2
                            const val MAX_VALUE: Int = 0xFF

                            var l1: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTheadlineMedium,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTheadlineMedium,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }

                            var l2: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTH2,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTH2,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }
                    }

                    object Codabar {
                        //private static int mCodabarRegisterValue = 0x40000;
                        var register: Int =
                            default

                        private val default: Int
                            get() {
                                return (Length.defaultLengtheadlineMedium
                                        or Length.defaultLength2
                                        or CLSIEditing.defaultValue
                                        or NOTISEditing.defaultValue
                                        or StartStopCharacters.defaultValue)
                            }

                        fun setDefault() {
                            register =
                                default
                        }

                        object Length {
                            const val BIT_LENGTheadlineMedium: Int = 0
                            const val BIT_LENGTH2: Int = 8
                            val defaultLengtheadlineMedium: Int = 1 shl BIT_LENGTheadlineMedium
                            val defaultLength2: Int = 55 shl BIT_LENGTH2
                            const val MAX_VALUE: Int = 0xFF

                            var l1: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTheadlineMedium,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTheadlineMedium,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }

                            var l2: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTH2,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTH2,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }

                        object CLSIEditing {
                            const val BIT_CLSI_EDIDING: Int = 16
                            val defaultValue: Int = DISABLE shl BIT_CLSI_EDIDING

                            fun enable() {
                                register = enable(BIT_CLSI_EDIDING, register)
                            }

                            fun disable() {
                                register = disable(BIT_CLSI_EDIDING, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_CLSI_EDIDING,
                                        register
                                    )
                                }
                        }

                        object NOTISEditing {
                            const val BIT_NOTIS_EDIDING: Int = 17
                            val defaultValue: Int = DISABLE shl BIT_NOTIS_EDIDING

                            fun enable() {
                                register = enable(BIT_NOTIS_EDIDING, register)
                            }

                            fun disable() {
                                register = disable(BIT_NOTIS_EDIDING, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_NOTIS_EDIDING,
                                        register
                                    )
                                }
                        }

                        object StartStopCharacters {
                            const val BIT_STARTSTOP_CHAR: Int = 18
                            val defaultValue: Int = 1 shl BIT_STARTSTOP_CHAR

                            fun setUpperCase() {
                                register = disable(BIT_STARTSTOP_CHAR, register)
                            }

                            fun setLowerCase() {
                                register = enable(BIT_STARTSTOP_CHAR, register)
                            }

                            val isLowerCase: Boolean
                                get() {
                                    return isEnable(
                                        BIT_STARTSTOP_CHAR,
                                        register
                                    )
                                }
                        }
                    }

                    object MSI {
                        //private static int mMSIRegisterValue = 0x60000;
                        var register: Int =
                            default

                        private val default: Int
                            get() {
                                return (Length.defaultLengtheadlineMedium
                                        or Length.defaultLength2
                                        or CheckDigitVerification.defaultValue
                                        or TransmitCheckDigit.defaultValue)
                            }

                        fun setDefault() {
                            register =
                                default
                        }

                        object Length {
                            const val BIT_LENGTheadlineMedium: Int = 0
                            const val BIT_LENGTH2: Int = 8
                            val defaultLengtheadlineMedium: Int = 1 shl BIT_LENGTheadlineMedium
                            val defaultLength2: Int = 55 shl BIT_LENGTH2
                            const val MAX_VALUE: Int = 0xFF

                            var l1: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTheadlineMedium,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTheadlineMedium,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }

                            var l2: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTH2,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTH2,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }

                        object CheckDigitVerification {
                            const val BIT_CHECKDIGIT_ALGORITHM: Int = 16
                            val defaultValue: Int = 0 shl BIT_CHECKDIGIT_ALGORITHM
                            const val MAX_VALUE: Int = 0x3

                            fun setOneCheckDigitMOD10() {
                                register =
                                    setValue(BIT_CHECKDIGIT_ALGORITHM, 0x0, register, MAX_VALUE)
                            }

                            fun setTwoCheckDigitMOD10MOD10() {
                                register =
                                    setValue(BIT_CHECKDIGIT_ALGORITHM, 0x1, register, MAX_VALUE)
                            }

                            fun setTwoCheckDigitMOD10MOD11() {
                                register =
                                    setValue(BIT_CHECKDIGIT_ALGORITHM, 0x2, register, MAX_VALUE)
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_CHECKDIGIT_ALGORITHM,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }

                        object TransmitCheckDigit {
                            const val BIT_TRANSMIT_CHECKDIGIT: Int = 18
                            val defaultValue: Int = ENABLE shl BIT_TRANSMIT_CHECKDIGIT

                            fun disable() {
                                register = disable(BIT_TRANSMIT_CHECKDIGIT, register)
                            }

                            fun enable() {
                                register = enable(BIT_TRANSMIT_CHECKDIGIT, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_TRANSMIT_CHECKDIGIT,
                                        register
                                    )
                                }
                        }
                    }

                    object Matrix2of5 {
                        //private static int mM2of5RegisterValue = 0x20000;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (Length.defaultLengtheadlineMedium
                                        or Length.defaultLength2
                                        or CheckDigitVerification.defaultValue
                                        or Redundancy.defaultValue)
                            }

                        fun setDefault() {
                            register = default
                        }

                        object Length {
                            const val BIT_LENGTheadlineMedium: Int = 0
                            const val BIT_LENGTH2: Int = 8
                            val defaultLengtheadlineMedium: Int = 1 shl BIT_LENGTheadlineMedium
                            val defaultLength2: Int = 55 shl BIT_LENGTH2
                            const val MAX_VALUE: Int = 0xFF

                            var l1: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTheadlineMedium,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTheadlineMedium,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }

                            var l2: Int
                                get() {
                                    return getValue(
                                        BIT_LENGTH2,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                                set(value) {
                                    register =
                                        setValue(
                                            BIT_LENGTH2,
                                            value,
                                            register,
                                            MAX_VALUE
                                        )
                                }
                        }

                        object CheckDigitVerification {
                            const val BIT_CHECK_DIGIT_VERIFICATION: Int = 16
                            val defaultValue: Int = DISABLE shl BIT_CHECK_DIGIT_VERIFICATION
                            const val MAX_VALUE: Int = 0x3

                            fun disable() {
                                register =
                                    setValue(BIT_CHECK_DIGIT_VERIFICATION, 0x0, register, MAX_VALUE)
                            }

                            fun setDoNotTransmit() {
                                register =
                                    setValue(BIT_CHECK_DIGIT_VERIFICATION, 0x1, register, MAX_VALUE)
                            }

                            fun setTransmit() {
                                register =
                                    setValue(BIT_CHECK_DIGIT_VERIFICATION, 0x2, register, MAX_VALUE)
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_CHECK_DIGIT_VERIFICATION,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }

                        object Redundancy {
                            const val BIT_REDUNDANCY: Int = 18
                            val defaultValue: Int = DISABLE shl BIT_REDUNDANCY

                            fun disable() {
                                register = disable(BIT_REDUNDANCY, register)
                            }

                            fun enable() {
                                register = enable(BIT_REDUNDANCY, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_REDUNDANCY,
                                        register
                                    )
                                }
                        }
                    }

                    object GS1Databar {
                        //private static int mGs1DatabarRegisterValue = 0x2;
                        var register: Int =
                            default

                        private val default: Int
                            get() {
                                return (SecurityLevel.defaultValue
                                        or ConvertGS1DatabarToUPCEAN.defaultValue)
                            }

                        fun setDefault() {
                            register =
                                default
                        }

                        object SecurityLevel {
                            const val BIT_SECURITY: Int = 0
                            val defaultValue: Int = 2 shl BIT_SECURITY
                            const val MAX_VALUE: Int = 0x3

                            fun level1() {
                                register = setValue(BIT_SECURITY, 0x0, register, MAX_VALUE)
                            }

                            fun level2() {
                                register = setValue(BIT_SECURITY, 0x1, register, MAX_VALUE)
                            }

                            fun level3() {
                                register = setValue(BIT_SECURITY, 0x2, register, MAX_VALUE)
                            }

                            fun level4() {
                                register = setValue(BIT_SECURITY, 0x3, register, MAX_VALUE)
                            }

                            val level: Int
                                get() {
                                    return getValue(
                                        BIT_SECURITY,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }

                        object ConvertGS1DatabarToUPCEAN {
                            const val BIT_GS1DATABAR_TO_UPCEAN: Int = 2
                            val defaultValue: Int = DISABLE shl BIT_GS1DATABAR_TO_UPCEAN

                            fun disable() {
                                register = disable(BIT_GS1DATABAR_TO_UPCEAN, register)
                            }

                            fun enable() {
                                register = enable(BIT_GS1DATABAR_TO_UPCEAN, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_GS1DATABAR_TO_UPCEAN,
                                        register
                                    )
                                }
                        }
                    }
                }

                class TwoD {
                    object PostalCodes {
                        //private static int mPotalCodeRegisterValue = 0x3;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (TransmitUKCheckDigit.defaultValue
                                        or TransmitUSCheckDigit.defaultValue
                                        or AustraliaPostFormat.defaultValue)
                            }

                        fun setDefault() {
                            register = default
                        }

                        object TransmitUSCheckDigit {
                            const val BIT_2D_POSTAL_TRANSMIT_US: Int = 0
                            val defaultValue: Int = ENABLE shl BIT_2D_POSTAL_TRANSMIT_US

                            fun enable() {
                                register = enable(BIT_2D_POSTAL_TRANSMIT_US, register)
                            }

                            fun disable() {
                                register = disable(BIT_2D_POSTAL_TRANSMIT_US, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_2D_POSTAL_TRANSMIT_US,
                                        register
                                    )
                                }
                        }

                        object TransmitUKCheckDigit {
                            const val BIT_2D_POSTAL_TRANSMIT_UK: Int = 1
                            val defaultValue: Int = ENABLE shl BIT_2D_POSTAL_TRANSMIT_UK

                            fun enable() {
                                register = enable(BIT_2D_POSTAL_TRANSMIT_UK, register)
                            }

                            fun disable() {
                                register = disable(BIT_2D_POSTAL_TRANSMIT_UK, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_2D_POSTAL_TRANSMIT_UK,
                                        register
                                    )
                                }
                        }

                        object AustraliaPostFormat {
                            const val BIT_2D_POSTAL_FORMAT: Int = 2
                            val defaultValue: Int = 0 shl BIT_2D_POSTAL_FORMAT
                            const val MAX_VALUE: Int = 0x3

                            fun auto() {
                                register = setValue(BIT_2D_POSTAL_FORMAT, 0x0, register, MAX_VALUE)
                            }

                            fun raw() {
                                register = setValue(BIT_2D_POSTAL_FORMAT, 0x1, register, MAX_VALUE)
                            }

                            fun alphanemeric() {
                                register = setValue(BIT_2D_POSTAL_FORMAT, 0x2, register, MAX_VALUE)
                            }

                            fun numeric() {
                                register = setValue(BIT_2D_POSTAL_FORMAT, 0x3, register, MAX_VALUE)
                            }

                            val format: Int
                                get() {
                                    return getValue(
                                        BIT_2D_POSTAL_FORMAT,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }
                    }

                    object Composite {
                        //private static int mCompositeRegisterValue = 0x1;
                        var register: Int =
                            default

                        private val default: Int
                            get() {
                                return (UPCCompositeMode.defaultValue
                                        or GS1128EmulationMode.defaultValue)
                            }

                        fun setDefault() {
                            register =
                                default
                        }

                        object GS1128EmulationMode {
                            const val BIT_2D_COMPOSITE_GS1_128_EMUL: Int = 2
                            val defaultValue: Int = DISABLE shl BIT_2D_COMPOSITE_GS1_128_EMUL

                            fun enable() {
                                register = enable(BIT_2D_COMPOSITE_GS1_128_EMUL, register)
                            }

                            fun disable() {
                                register = disable(BIT_2D_COMPOSITE_GS1_128_EMUL, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_2D_COMPOSITE_GS1_128_EMUL,
                                        register
                                    )
                                }
                        }

                        object UPCCompositeMode {
                            const val BIT_2D_COMPOSITE_UPC_MODE: Int = 0
                            val defaultValue: Int = 1 shl BIT_2D_COMPOSITE_UPC_MODE
                            const val MAX_VALUE: Int = 0x3

                            fun setNeverLinked() {
                                register =
                                    setValue(BIT_2D_COMPOSITE_UPC_MODE, 0x0, register, MAX_VALUE)
                            }

                            fun setAlwaysLinked() {
                                register =
                                    setValue(BIT_2D_COMPOSITE_UPC_MODE, 0x1, register, MAX_VALUE)
                            }

                            fun setAutoDiscriminate() {
                                register =
                                    setValue(BIT_2D_COMPOSITE_UPC_MODE, 0x2, register, MAX_VALUE)
                            }

                            val format: Int
                                get() {
                                    return getValue(
                                        BIT_2D_COMPOSITE_UPC_MODE,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }
                    }

                    object MicroPDF417 {
                        //private static int mMPDF417RegisterValue = 0x0;
                        var register: Int = MicroPDF417.getDefault()

                        fun getDefault(): Int {
                            return Code128Emulation.default
                        }

                        fun setDefault() {
                            register = MicroPDF417.getDefault()
                        }

                        object Code128Emulation {
                            const val BIT_2D_MPDF417_CODE128_EMUL: Int = 0
                            const val default: Int = DISABLE shl BIT_2D_MPDF417_CODE128_EMUL

                            fun enable() {
                                register = enable(BIT_2D_MPDF417_CODE128_EMUL, register)
                            }

                            fun disable() {
                                register = disable(BIT_2D_MPDF417_CODE128_EMUL, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_2D_MPDF417_CODE128_EMUL,
                                        register
                                    )
                                }
                        }
                    }

                    object MacroPDF {
                        //private static int mMacroPdfRegisterValue = 0x6;
                        var register: Int = default

                        private val default: Int
                            get() {
                                return (TransmitDecodeModeSymbols.defaultValue
                                        or TransmitControlHeader.defaultValue
                                        or EscapeCharacter.defaultValue)
                            }

                        fun setDefault() {
                            register = default
                        }

                        object TransmitDecodeModeSymbols {
                            const val BIT_2D_MACROPDF_MODE_SYMBOL: Int = 0
                            val defaultValue: Int = 2 shl BIT_2D_MACROPDF_MODE_SYMBOL
                            const val MAX_VALUE: Int = 0x3

                            fun setBufferAllSymbols() {
                                register =
                                    setValue(BIT_2D_MACROPDF_MODE_SYMBOL, 0x0, register, MAX_VALUE)
                            }

                            fun setAnySymbolNoOrder() {
                                register =
                                    setValue(BIT_2D_MACROPDF_MODE_SYMBOL, 0x1, register, MAX_VALUE)
                            }

                            fun setPassthroughAllSymbols() {
                                register =
                                    setValue(BIT_2D_MACROPDF_MODE_SYMBOL, 0x2, register, MAX_VALUE)
                            }

                            val mode: Int
                                get() {
                                    return getValue(
                                        BIT_2D_MACROPDF_MODE_SYMBOL,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }

                        object TransmitControlHeader {
                            const val BIT_2D_MACRO417_CONTROL_HEADER: Int = 2
                            val defaultValue: Int = ENABLE shl BIT_2D_MACRO417_CONTROL_HEADER

                            fun enable() {
                                register = enable(BIT_2D_MACRO417_CONTROL_HEADER, register)
                            }

                            fun disable() {
                                register = disable(BIT_2D_MACRO417_CONTROL_HEADER, register)
                            }

                            val isEnable: Boolean
                                get() {
                                    return isEnable(
                                        BIT_2D_MACRO417_CONTROL_HEADER,
                                        register
                                    )
                                }
                        }

                        object EscapeCharacter {
                            const val BIT_2D_MACRO417_ESC_CHAR: Int = 3
                            val defaultValue: Int = 0 shl BIT_2D_MACRO417_ESC_CHAR

                            fun setNone() {
                                register = disable(BIT_2D_MACRO417_ESC_CHAR, register)
                            }

                            fun setGLIProtocol() {
                                register = enable(BIT_2D_MACRO417_ESC_CHAR, register)
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_2D_MACRO417_ESC_CHAR,
                                        register,
                                        0x1
                                    )
                                }
                        }
                    }

                    object DataMatrix {
                        //private static int mDataMatrixRegisterValue = 0x2;
                        var register: Int = getDefault()

                        private fun getDefault(): Int {
                            return DecodeMirrorImages.default
                        }

                        fun setDefault() {
                            register = getDefault()
                        }

                        object DecodeMirrorImages {
                            const val BIT_2D_MATRIX_DECODE_MIRROR_IMG: Int = 0
                            val default: Int = 2 shl BIT_2D_MATRIX_DECODE_MIRROR_IMG
                            const val MAX_VALUE: Int = 0x3

                            fun setUnMirror() {
                                register = setValue(
                                    BIT_2D_MATRIX_DECODE_MIRROR_IMG,
                                    0x0,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            fun setOnly() {
                                register = setValue(
                                    BIT_2D_MATRIX_DECODE_MIRROR_IMG,
                                    0x1,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            fun setBoth() {
                                register = setValue(
                                    BIT_2D_MATRIX_DECODE_MIRROR_IMG,
                                    0x2,
                                    register,
                                    MAX_VALUE
                                )
                            }

                            val value: Int
                                get() {
                                    return getValue(
                                        BIT_2D_MATRIX_DECODE_MIRROR_IMG,
                                        register,
                                        MAX_VALUE
                                    )
                                }
                        }
                    }
                }
            }
        }
    }

    class Device {
        object Bluetooth {
            var isConnect: Boolean = false
                private set
            var connectedDeviceMacAddress: String? = null
                private set
            var connectedDeviceName: String? = null
                private set
            var prevDeviceMacAddress: String? = null
                private set
            var prevDeviceName: String? = null
                private set

            fun connect() {
                isConnect = true
            }

            fun disconnect() {
                isConnect = false
            }

            fun setConnectedDevice(sMacAddress: String?, sDeviceName: String?) {
                connectedDeviceMacAddress = sMacAddress
                connectedDeviceName = sDeviceName
            }

            fun backupConnectedDevice() {
                prevDeviceMacAddress = connectedDeviceMacAddress
                prevDeviceName = connectedDeviceName
            }

            object Power {
                private val bluetoothAdapter: BluetoothAdapter? =
                    BluetoothAdapter.getDefaultAdapter()

                val isOn: Boolean
                    get() {
                        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
                    }

                fun setOn() {
                    bluetoothAdapter!!.enable()
                }

                fun setOff() {
                    bluetoothAdapter!!.disable()
                }
            }
        }

        class App {
            class Mode {
                object Tab {
                    const val TAB_CONFIG: Int = 0
                    const val TAB_SCAN: Int = 1
                    const val TAB_APP: Int = 2
                    var tabMode: Int = TAB_CONFIG
                }

                object ActivityName {
                    var currentActivity: String? = null
                }

                object Scan {
                    const val SCAN_DATA_RFID: Int = 0
                    const val SCAN_DATA_BARCODE: Int = 1
                    var isRunning: Boolean = false
                        private set
                    var dataType: Int = SCAN_DATA_RFID
                        private set

                    fun run(DataType: Int) {
                        isRunning = true
                        dataType = DataType
                    }

                    fun stop() {
                        isRunning = false
                    }
                }
            }
        }

        object Config {
            fun setDefault() {
                Buzzer.mode = Buzzer.BUZZER_HIGH
                PowerOffDelay.time = 0
                Sync.mode = Sync.SYNC_FROM_DEVICE_TO_APP
            }

            object Battery {
                var level: Int = 0
            }

            object Buzzer {
                const val BUZZER_HIGH: Int = 2
                const val BUZZER_LOW: Int = 1
                const val BUZZER_MUTE: Int = 0
                var mode: Int = BUZZER_HIGH
            }

            object Vibration {
                var isEnable: Boolean = false
                    private set

                fun enable() {
                    isEnable = true
                }

                fun disable() {
                    isEnable = false
                }
            }

            object PowerOffDelay {
                var time: Int = 0
            }

            object Sync {
                const val SYNC_FROM_DEVICE_TO_APP: Int = 0
                const val SYNC_FROM_APP_TO_DEVICE: Int = 1
                var mode: Int = SYNC_FROM_DEVICE_TO_APP
            }
        }

        class Info {
            object Firmware {
                var firmwareVersion: String? = null
            }
        }
    }

    class RFID {
        object Config {
            fun setDefault() {
                TxCycle.offTime = 40
                TxCycle.mOnTime = 160
                TxCycle.percent = 20
                RadioPower.maxPower = 30
                RadioPower.attenuatePower = 0
                Queue.value = 5
                Target.mode = Target.TARGET_A
                Session.session = Session.SESSION_1
                FastID.isEnable = false
                TagFocus.isEnable = true
                Inventory.Mode.mode = Inventory.Mode.INVENTORY_CONTINUOUS_MODE
                Inventory.Timeout.timeout = Inventory.Timeout.INVENTORY_TIMEOUT_INFINITE
                LinkProfile.profile = LinkProfile.LINK_PROFILE_1
            }

            object Session {
                const val SESSION_0: Int = 0
                const val SESSION_1: Int = 1
                const val SESSION_2: Int = 2
                const val SESSION_3: Int = 3
                var session: Int = SESSION_1
            }

            object Queue {
                var value: Int = 5
            }

            object Target {
                const val TARGET_A: Int = 0
                const val TARGET_B: Int = 1
                const val TARGET_AB: Int = 2
                var mode: Int = TARGET_A

                fun setTargetA() {
                    mode = TARGET_A
                }

                fun setTargetB() {
                    mode = TARGET_B
                }

                fun setTargetAB() {
                    mode = TARGET_AB
                }
            }

            object RadioPower {
                var attenuatePower: Int = 0
                var maxPower: Int = 30

                val power: Int
                    get() {
                        return maxPower + attenuatePower
                    }
            }

            object TxCycle {
                private const val MAX_TIME: Int = 200

                //!< On TIme : 40ms = 20%, 50ms = 25%, 60ms = 30%, ... 190ms = 95%, 200ms = 100%;
                //!< Off Time : 40ms/160ms, 50ms/150ms ... 190ms/10ms, 200ms/0ms
                //!< ( On TIme / (On Time + Off Time) ) *100 = TxCycle Percent
                var mOnTime: Int = 40
                var offTime: Int = 160
                var percent: Int = 20

                var onTime: Int
                    get() {
                        return mOnTime
                    }
                    set(Time) {
                        mOnTime =
                            Time
                        offTime =
                            MAX_TIME - mOnTime
                        percent =
                            ((mOnTime.toFloat() / (mOnTime.toFloat() + offTime.toFloat())) * 100).toInt()
                    }
            }

            class Inventory {
                object Mode {
                    const val INVENTORY_CONTINUOUS_MODE: Int = 0
                    const val INVENTORY_SINGLE_MODE: Int = 1
                    var mode: Int = INVENTORY_CONTINUOUS_MODE

                    fun setContinuousMode() {
                        mode = INVENTORY_CONTINUOUS_MODE
                    }

                    fun setSingleMode() {
                        mode = INVENTORY_SINGLE_MODE
                    }
                }

                object Timeout {
                    const val INVENTORY_TIMEOUT_INFINITE: Int = 0
                    var timeout: Int = INVENTORY_TIMEOUT_INFINITE
                }

                class Report {
                    object Time {
                        var isEnable: Boolean = false
                            private set

                        fun enable() {
                            isEnable = true
                        }

                        fun disable() {
                            isEnable = false
                        }
                    }

                    object RSSI {
                        var isEnable: Boolean = false
                            private set

                        fun enable() {
                            isEnable = true
                        }

                        fun disable() {
                            isEnable = false
                        }
                    }
                }
            }

            object TagFocus {
                var isEnable: Boolean = true

                fun enable() {
                    isEnable = true
                }

                fun disable() {
                    isEnable = false
                }
            }

            object FastID {
                var isEnable: Boolean = false

                fun enable() {
                    isEnable = true
                }

                fun disable() {
                    isEnable = false
                }
            }

            object LinkProfile {
                const val LINK_PROFILE_0: Int = 0
                const val LINK_PROFILE_1: Int = 1
                const val LINK_PROFILE_2: Int = 2
                const val LINK_PROFILE_3: Int = 3
                var profile: Int = LINK_PROFILE_1
            }

            object Format {
                fun setDefault() {
                    DataFormat.format = 0
                    FixDataFormat.format = 0
                    Prefix.data = 0
                    Suffix1.data = 0
                    Suffix2.data = 0
                }

                object DataFormat {
                    const val PC_EPC_CRC: Int = 0
                    const val PC_EPC: Int = 1
                    const val EPC_CRC: Int = 2
                    const val EPC_ONLY: Int = 3
                    var format: Int = 0
                }

                object FixDataFormat {
                    const val TAG_DATA: Int = 0
                    const val DATA_SUFFIX1: Int = 1
                    const val DATA_SUFFFIX2: Int = 2
                    const val DATA_SUFFIX1_SUFFIX2: Int = 3
                    const val PREFIX_DATA: Int = 4
                    const val PREFIX_DATA_SUFFIX1: Int = 5
                    const val PREFIX_DATA_SUFFIX2: Int = 6
                    const val PREFIX_DATA_SUFFIX1_SUFFIX2: Int = 7
                    var format: Int = 0
                }

                object Prefix {
                    var data: Int = 0
                }

                object Suffix1 {
                    var data: Int = 0
                }

                object Suffix2 {
                    var data: Int = 0
                }
            }
        }
    }
}
