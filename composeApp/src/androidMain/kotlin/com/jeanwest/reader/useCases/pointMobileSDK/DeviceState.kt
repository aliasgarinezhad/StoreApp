package com.jeanwest.reader.useCases.pointMobileSDK;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by NG on 2016-04-28.
 */
public class DeviceState {
    private static final int ENABLE = 1;
    private static final int DISABLE = 0;

    public static class Scanner {
        public static void setDefault() {
            ScanningPreferences.setDefault();
            Symbologies.setDefault();
            DataFormat.setDefault();
            RedundancyAndSecurityLevel.setDefault();
            Delimiter.setDefault();
        }

        private static int enable(int bitNum, int registerValue) {
            return (registerValue | (0x1 << bitNum));
        }

        private static int disable(int bitNum, int registerValue) {
            return (registerValue & (~(0x1 << bitNum)));
        }

        private static boolean isEnable(int bitNum, int registerValue) {
            return (registerValue & (0x1 << bitNum)) > 0;
        }

        private static int setValue(int bitNum, int nData, int registerValue, int nMaxData) {
            registerValue = registerValue & (~(nMaxData << bitNum));
            registerValue = registerValue | (nData << bitNum);
            return registerValue;
        }

        private static int getValue(int bitNum, int nRegister, int nMaxData) {
            return (nRegister & (nMaxData << bitNum)) >> bitNum;
        }

        public static class Firmware {
            private static String firmwareString;

            public static String getFirmwareVersion() {
                return firmwareString;
            }

            public static void setFirmwareVersion(String firmware) {
                firmwareString = firmware;
            }
        }

        public static class Type {
            public static final int NO_SCANNER = 0;
            public static final int SCANNER_1D_SE655 = 1;
            public static final int SCANNER_2D_SE4750 = 2;
            public static final int SCANNER_2D_EM3396 = 3;
            private static int scannerType = NO_SCANNER;

            public static int getType() {
                return scannerType;
            }

            public static void setType(int nType) {
                scannerType = nType;
            }
        }

        public static class ScanningPreferences {
            //private static int registerValue = 0x13900063;
            private static int registerValue = setDefault();

            public static int setDefault() {
                return DecodeSessionTimeout.defaultValue
                        | Fnc1.defaultValue
                        | Inverse.defaultValue
                        | PickList.defaultValue
                        | MirroredImage.defaultValue
                        | MobileDisplayMode.defaultValue
                        | DecodingIllumination.defaultValue
                        | DecodingAimingPattern.defaultValue
                        | OneDQuietZoneLevel.defaultValue
                        | IntercharacterGapSize.defaultValue
                        | Fuzzy1DProcessing.defaultValue;
            }

            public static int getRegister() {
                return registerValue;
            }

            public static void setRegister(int value) {
                registerValue = value;
            }

            public static class DecodeSessionTimeout {
                static final int MAX_VALUE = 0xFF;
                private static final int BIT_DECODESESSION = 0;
                private static final int defaultValue = 99 << BIT_DECODESESSION;

                public static int getTimeout() {
                    return Scanner.getValue(BIT_DECODESESSION, registerValue, MAX_VALUE);
                }

                public static void setTimeout(int nTimeout) {
                    if (nTimeout > 99)
                        nTimeout = 99;
                    else if (nTimeout < 5)
                        nTimeout = 5;
                    registerValue = Scanner.setValue(BIT_DECODESESSION, nTimeout, registerValue, MAX_VALUE);
                }
            }

            public static class Fnc1 {
                public static final String S_KEY_ENABLE = "Enable";
                public static final String S_KEY_ASCII = "Ascii";
                private static final int BIT_FNC1_ENABLE = 8;
                private static final int BIT_FNC1_VALUE = 9;
                private static final int defaultValue = (DISABLE << BIT_FNC1_ENABLE) | (0 << BIT_FNC1_VALUE);
                private static final int FNC1_MAX_VALUE = 0x7F;

                public static void enable() {
                    registerValue = Scanner.enable(BIT_FNC1_ENABLE, registerValue);
                }

                public static void disable() {
                    registerValue = Scanner.disable(BIT_FNC1_ENABLE, registerValue);
                }

                public static boolean isEnable() {
                    return Scanner.isEnable(BIT_FNC1_ENABLE, registerValue);
                }

                public static int getAsciiCode() {
                    return Scanner.getValue(BIT_FNC1_VALUE, registerValue, FNC1_MAX_VALUE);
                }

                public static void setAsciiCode(int nCode) {
                    registerValue = Scanner.setValue(BIT_FNC1_VALUE, nCode, registerValue, FNC1_MAX_VALUE);
                }
            }

            public static class Inverse {
                public static final int MODE_REGULAR = 0;
                public static final int MODE_INVERSE_ONLY = 1;
                public static final int MODE_INVERSE_AUTO = 2;
                static final int MAX_VALUE = 3;
                private static final int BIT_INVERSE_1D = 16;
                private static final int BIT_INVERSE_2D = 18;
                private static final int defaultValue = (MODE_REGULAR << BIT_INVERSE_1D) | (MODE_REGULAR << BIT_INVERSE_2D);

                public static class OneD {
                    public static int getMode() {
                        return Scanner.getValue(BIT_INVERSE_1D, registerValue, MAX_VALUE);
                    }

                    public static void setMode(int nMode) {
                        registerValue = Scanner.setValue(BIT_INVERSE_1D, nMode, registerValue, MAX_VALUE);
                    }
                }

                public static class TwoD {
                    public static int getMode() {
                        return Scanner.getValue(BIT_INVERSE_2D, registerValue, MAX_VALUE);
                    }

                    public static void setMode(int nMode) {
                        registerValue = Scanner.setValue(BIT_INVERSE_2D, nMode, registerValue, MAX_VALUE);
                    }
                }
            }

            public static class PickList {
                private static final int BIT_PICKLIST = 20;
                private static final int defaultValue = ENABLE << BIT_PICKLIST;

                public static void enable() {
                    registerValue = Scanner.enable(BIT_PICKLIST, registerValue);
                }

                public static void disable() {
                    registerValue = Scanner.disable(BIT_PICKLIST, registerValue);
                }

                public static boolean isEnable() {
                    return Scanner.isEnable(BIT_PICKLIST, registerValue);
                }
            }

            public static class MirroredImage {
                private static final int BIT_MIRRORED_IMAGE = 21;
                private static final int defaultValue = DISABLE << BIT_MIRRORED_IMAGE;

                public static void enable() {
                    registerValue = Scanner.enable(BIT_MIRRORED_IMAGE, registerValue);
                }

                public static void disable() {
                    registerValue = Scanner.disable(BIT_MIRRORED_IMAGE, registerValue);
                }

                public static boolean isEnable() {
                    return Scanner.isEnable(BIT_MIRRORED_IMAGE, registerValue);
                }
            }

            public static class MobileDisplayMode {
                private static final int BIT_MOBILE_DISPLAY_MODE = 22;
                private static final int defaultValue = DISABLE << BIT_MOBILE_DISPLAY_MODE;

                public static void enable() {
                    registerValue = Scanner.enable(BIT_MOBILE_DISPLAY_MODE, registerValue);
                }

                public static void disable() {
                    registerValue = Scanner.disable(BIT_MOBILE_DISPLAY_MODE, registerValue);
                }

                public static boolean isEnable() {
                    return Scanner.isEnable(BIT_MOBILE_DISPLAY_MODE, registerValue);
                }
            }

            public static class DecodingIllumination {
                private static final int BIT_DECODING_ILLUMINATION = 23;
                private static final int defaultValue = ENABLE << BIT_DECODING_ILLUMINATION;

                public static void enable() {
                    registerValue = Scanner.enable(BIT_DECODING_ILLUMINATION, registerValue);
                }

                public static void disable() {
                    registerValue = Scanner.disable(BIT_DECODING_ILLUMINATION, registerValue);
                }

                public static boolean isEnable() {
                    return Scanner.isEnable(BIT_DECODING_ILLUMINATION, registerValue);
                }
            }

            public static class DecodingAimingPattern {
                private static final int BIT_DECODING_AIMING_PATTERN = 24;
                private static final int defaultValue = ENABLE << BIT_DECODING_AIMING_PATTERN;

                public static void enable() {
                    registerValue = Scanner.enable(BIT_DECODING_AIMING_PATTERN, registerValue);
                }

                public static void disable() {
                    registerValue = Scanner.disable(BIT_DECODING_AIMING_PATTERN, registerValue);
                }

                public static boolean isEnable() {
                    return Scanner.isEnable(BIT_DECODING_AIMING_PATTERN, registerValue);
                }
            }

            public static class OneDQuietZoneLevel {
                public static final int MODE_NORMALLY = 0;
                public static final int MODE_MORE_AGGRESSIVELY = 1;
                public static final int MODE_ONE_SIDE_EB = 2;
                public static final int MODE_ANYTHING = 3;

                private static final int BIT_QUIETZONE_LEVEL = 25;
                private static final int defaultValue = 1 << BIT_QUIETZONE_LEVEL;
                private static final int MAX_VALUE = 3;

                public static int getMode() {
                    return Scanner.getValue(BIT_QUIETZONE_LEVEL, registerValue, MAX_VALUE);
                }

                public static void setMode(int nMode) {
                    registerValue = Scanner.setValue(BIT_QUIETZONE_LEVEL, nMode, registerValue, MAX_VALUE);
                }
            }

            public static class IntercharacterGapSize {
                public static final int LARGE_SIZE = 1;
                public static final int NORMAL_SIZE = 0;
                private static final int BIT_INTERCHARACTER_GAP_SIZE = 27;
                private static final int defaultValue = NORMAL_SIZE << BIT_INTERCHARACTER_GAP_SIZE;

                public static void setLarge() {
                    registerValue = Scanner.enable(BIT_INTERCHARACTER_GAP_SIZE, registerValue);
                }

                public static void setNormal() {
                    registerValue = Scanner.disable(BIT_INTERCHARACTER_GAP_SIZE, registerValue);
                }

                public static int getSize() {
                    return Scanner.isEnable(BIT_INTERCHARACTER_GAP_SIZE, registerValue) ? 1 : 0;
                }
            }

            public static class Fuzzy1DProcessing {
                static final int BIT_1DFUZZY = 28;
                private static final int defaultValue = ENABLE << BIT_1DFUZZY;

                public static void enable() {
                    registerValue = Scanner.enable(BIT_1DFUZZY, registerValue);
                }

                public static void disable() {
                    registerValue = Scanner.disable(BIT_1DFUZZY, registerValue);
                }

                public static boolean isEnable() {
                    return Scanner.isEnable(BIT_1DFUZZY, registerValue);
                }
            }
        }

        public static class DataFormat {
            //private static int mDataOptionRegisterValue = 0x68A000;
            private static int mDataOptionRegisterValue = getDefault();

            private static int getDefault() {
                return TransmitCodeID.defaultValue
                        | ScanDataTransmissionFormat.defaultValue
                        | Prefix.defaultValue
                        | Suffix1.defaultValue
                        | Suffix2.defaultValue;
            }

            public static int getRegister() {
                return mDataOptionRegisterValue;
            }

            public static void setRegister(int value) {
                mDataOptionRegisterValue = value;
            }

            public static void setDefault() {
                mDataOptionRegisterValue = getDefault();
            }

            public static class TransmitCodeID {
                private static final int BIT_DATAOPTION_TRANSMIT_CODEID = 0;
                private static final int defaultValue = 0 << BIT_DATAOPTION_TRANSMIT_CODEID;
                static int MAX_VALUE = 0x3;

                public static int getValue() {
                    return Scanner.getValue(BIT_DATAOPTION_TRANSMIT_CODEID, mDataOptionRegisterValue, MAX_VALUE);
                }

                public static void setValue(int mode) {
                    mDataOptionRegisterValue = Scanner.setValue(BIT_DATAOPTION_TRANSMIT_CODEID, mode, mDataOptionRegisterValue, MAX_VALUE);
                }
            }

            public static class ScanDataTransmissionFormat {
                static int BIT_DATAOPTION_TRANSMIT_FORMAT = 2;
                private static final int defaultValue = 0 << BIT_DATAOPTION_TRANSMIT_FORMAT;
                static int MAX_VALUE = 0x7;

                public static int getFormat() {
                    return Scanner.getValue(BIT_DATAOPTION_TRANSMIT_FORMAT, mDataOptionRegisterValue, MAX_VALUE);
                }

                public static void setFormat(int formatType) {
                    mDataOptionRegisterValue = Scanner.setValue(BIT_DATAOPTION_TRANSMIT_FORMAT, formatType, mDataOptionRegisterValue, MAX_VALUE);
                }
            }

            public static class Prefix {
                static int BIT_DATAOPTION_PREFIX = 5;
                private static final int defaultValue = 0 << BIT_DATAOPTION_PREFIX;
                static int MAX_VALUE = 0x7F;

                public static int getValue() {
                    return Scanner.getValue(BIT_DATAOPTION_PREFIX, mDataOptionRegisterValue, MAX_VALUE);
                }

                public static void setValue(int value) {
                    mDataOptionRegisterValue = Scanner.setValue(BIT_DATAOPTION_PREFIX, value, mDataOptionRegisterValue, MAX_VALUE);
                }
            }

            public static class Suffix1 {
                static int BIT_DATAOPTION_SUFFIX1 = 12;
                private static final int defaultValue = 0xA << BIT_DATAOPTION_SUFFIX1;
                static int MAX_VALUE = 0x7F;

                public static int getValue() {
                    return Scanner.getValue(BIT_DATAOPTION_SUFFIX1, mDataOptionRegisterValue, MAX_VALUE);
                }

                public static void setValue(int value) {
                    mDataOptionRegisterValue = Scanner.setValue(BIT_DATAOPTION_SUFFIX1, value, mDataOptionRegisterValue, MAX_VALUE);
                }
            }

            public static class Suffix2 {
                static int BIT_DATAOPTION_SUFFIX2 = 19;
                private static final int defaultValue = 0xD << BIT_DATAOPTION_SUFFIX2;
                static int MAX_VALUE = 0x7F;

                public static int getValue() {
                    return Scanner.getValue(BIT_DATAOPTION_SUFFIX2, mDataOptionRegisterValue, MAX_VALUE);
                }

                public static void setValue(int value) {
                    mDataOptionRegisterValue = Scanner.setValue(BIT_DATAOPTION_SUFFIX2, value, mDataOptionRegisterValue, MAX_VALUE);
                }
            }
        }

        public static class RedundancyAndSecurityLevel {
            //private static int mRnSRegister = 0x5;
            private static int mRnSRegister = getDefault();

            private static int getDefault() {
                return Redundancy.defaultValue
                        | Security.defaultValue;
            }

            public static int getRegister() {
                return mRnSRegister;
            }

            public static void setRegister(int value) {
                mRnSRegister = value;
            }

            public static void setDefault() {
                mRnSRegister = getDefault();
            }

            public static class Security {
                static final int BIT_RNS_SECURITY = 2;
                static final int MAX_VALUE = 0x3;
                private static final int defaultValue = 1 << BIT_RNS_SECURITY;

                public static void level0() {
                    mRnSRegister = Scanner.setValue(BIT_RNS_SECURITY, 0x0, mRnSRegister, MAX_VALUE);
                }

                public static void level1() {
                    mRnSRegister = Scanner.setValue(BIT_RNS_SECURITY, 0x1, mRnSRegister, MAX_VALUE);
                }

                public static void level2() {
                    mRnSRegister = Scanner.setValue(BIT_RNS_SECURITY, 0x2, mRnSRegister, MAX_VALUE);
                }

                public static void level3() {
                    mRnSRegister = Scanner.setValue(BIT_RNS_SECURITY, 0x3, mRnSRegister, MAX_VALUE);
                }

                public static int getValue() {
                    return Scanner.getValue(BIT_RNS_SECURITY, mRnSRegister, MAX_VALUE);
                }
            }

            public static class Redundancy {
                static final int BIT_RNS_REDUNDANCY = 0;
                static final int MAX_VALUE = 0x3;
                private static final int defaultValue = 1 << BIT_RNS_REDUNDANCY;

                public static void level1() {
                    mRnSRegister = Scanner.setValue(BIT_RNS_REDUNDANCY, 0x0, mRnSRegister, MAX_VALUE);
                }

                public static void level2() {
                    mRnSRegister = Scanner.setValue(BIT_RNS_REDUNDANCY, 0x1, mRnSRegister, MAX_VALUE);
                }

                public static void level3() {
                    mRnSRegister = Scanner.setValue(BIT_RNS_REDUNDANCY, 0x2, mRnSRegister, MAX_VALUE);
                }

                public static void level4() {
                    mRnSRegister = Scanner.setValue(BIT_RNS_REDUNDANCY, 0x3, mRnSRegister, MAX_VALUE);
                }

                public static int getValue() {
                    return Scanner.getValue(BIT_RNS_REDUNDANCY, mRnSRegister, MAX_VALUE);
                }
            }
        }

        public static class Delimiter {
            //private static int mDelimiterRegister = 0x1A28;
            private static int mDelimiterRegister = getDefault();

            private static int getDefault() {
                return TransmissionFormat.defaultValue
                        | Delimiter1.defaultValue
                        | Delimiter2.defaultValue;
            }

            public static int getRegister() {
                return mDelimiterRegister;
            }

            public static void setRegister(int value) {
                mDelimiterRegister = value;
            }

            public static void setDefault() {
                mDelimiterRegister = getDefault();
            }

            public static class Delimiter2 {
                static int BIT_DELIMITER_DELIMITER2 = 9;
                private static final int defaultValue = 0xD << BIT_DELIMITER_DELIMITER2;
                static int MAX_VALUE = 0x7F;

                public static int getValue() {
                    return Scanner.getValue(BIT_DELIMITER_DELIMITER2, mDelimiterRegister, MAX_VALUE);
                }

                public static void setValue(int value) {
                    mDelimiterRegister = Scanner.setValue(BIT_DELIMITER_DELIMITER2, value, mDelimiterRegister, MAX_VALUE);
                }
            }

            public static class Delimiter1 {
                static int BIT_DELIMITER_DELIMITER1 = 2;
                private static final int defaultValue = 0xA << BIT_DELIMITER_DELIMITER1;
                static int MAX_VALUE = 0x7F;

                public static int getValue() {
                    return Scanner.getValue(BIT_DELIMITER_DELIMITER1, mDelimiterRegister, MAX_VALUE);
                }

                public static void setValue(int value) {
                    mDelimiterRegister = Scanner.setValue(BIT_DELIMITER_DELIMITER1, value, mDelimiterRegister, MAX_VALUE);
                }
            }

            public static class TransmissionFormat {
                static final int BIT_RNS_SECURITY = 0;
                static final int MAX_VALUE = 0x3;
                private static final int defaultValue = 0 << BIT_RNS_SECURITY;

                public static void dataAsIs() {
                    mDelimiterRegister = Scanner.setValue(BIT_RNS_SECURITY, 0x0, mDelimiterRegister, MAX_VALUE);
                }

                public static void setData1_Delimiter1_Data2() {
                    mDelimiterRegister = Scanner.setValue(BIT_RNS_SECURITY, 0x1, mDelimiterRegister, MAX_VALUE);
                }

                public static void setData1_Delimiter2_Data2() {
                    mDelimiterRegister = Scanner.setValue(BIT_RNS_SECURITY, 0x2, mDelimiterRegister, MAX_VALUE);
                }

                public static void setData1_Delimiter1_Delimiter2_Data2() {
                    mDelimiterRegister = Scanner.setValue(BIT_RNS_SECURITY, 0x3, mDelimiterRegister, MAX_VALUE);
                }

                public static int getValue() {
                    return Scanner.getValue(BIT_RNS_SECURITY, mDelimiterRegister, MAX_VALUE);
                }
            }
        }

        public static class Symbologies {

            public static void setDefault() {
                OneD.setDefault();
                TwoD.setDefault();
                AdvancedConfig.OneD.UPCEAN.setDefault();
                AdvancedConfig.OneD.Codabar.setDefault();
                AdvancedConfig.OneD.Code11.setDefault();
                AdvancedConfig.OneD.Code39.setDefault();
                AdvancedConfig.OneD.Code93.setDefault();
                AdvancedConfig.OneD.Code128.setDefault();
                AdvancedConfig.OneD.Discrete2of5.setDefault();
                AdvancedConfig.OneD.GS1Databar.setDefault();
                AdvancedConfig.OneD.Interleaved2of5.setDefault();
                AdvancedConfig.OneD.ISBN.setDefault();
                AdvancedConfig.OneD.ISBT.setDefault();
                AdvancedConfig.OneD.Matrix2of5.setDefault();
                AdvancedConfig.OneD.MSI.setDefault();
                AdvancedConfig.TwoD.Composite.setDefault();
                AdvancedConfig.TwoD.DataMatrix.setDefault();
                AdvancedConfig.TwoD.MacroPDF.setDefault();
                AdvancedConfig.TwoD.MicroPDF417.setDefault();
                AdvancedConfig.TwoD.PostalCodes.setDefault();
            }

            public static class OneD {
                //private static int mOneDRegisterValue = 0xE3C79B;
                private static int mOneDRegisterValue = getDefault();

                private static int getDefault() {
                    return (UPC_A.defaultValue
                            | UPC_E0.defaultValue
                            | UPC_E1.defaultValue
                            | EAN_8.defaultValue
                            | EAN_13.defaultValue
                            | ISBN.defaultValue
                            | ISSN.defaultValue
                            | CODE_128.defaultValue
                            | GS1_128.defaultValue
                            | ISBT_128.defaultValue
                            | CODE_39.defaultValue
                            | Trioptic_39.defaultValue
                            | CODE_93.defaultValue
                            | CODE_11.defaultValue
                            | Interleaved_2of5.defaultValue
                            | Discrete_2of5.defaultValue
                            | Codabar.defaultValue
                            | (MSI.defaultValue)
                            | Chinese_2of5.defaultValue
                            | Matrix_2of5.defaultValue
                            | Korean_3of5.defaultValue
                            | GS1Databar.defaultValue
                            | GS1DatabarLimited.defaultValue
                            | GS1DatabarExpanded.defaultValue);
                }

                public static void setDefault() {
                    mOneDRegisterValue = getDefault();
                }

                public static int getRegister() {
                    return mOneDRegisterValue;
                }

                public static void setRegister(int value) {
                    mOneDRegisterValue = value;
                }

                public static class UPC_A {
                    static final int BIT_UPC_A = 0;
                    static final int defaultValue = ENABLE << BIT_UPC_A;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_UPC_A, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_UPC_A, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_UPC_A, mOneDRegisterValue);
                    }
                }

                public static class UPC_E0 {
                    static final int BIT_UPC_E0 = 1;
                    static final int defaultValue = ENABLE << BIT_UPC_E0;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_UPC_E0, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_UPC_E0, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_UPC_E0, mOneDRegisterValue);
                    }
                }

                public static class UPC_E1 {
                    static final int BIT_UPC_E1 = 2;
                    static final int defaultValue = DISABLE << BIT_UPC_E1;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_UPC_E1, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_UPC_E1, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_UPC_E1, mOneDRegisterValue);
                    }
                }

                public static class EAN_8 {
                    static final int BIT_EAN_8 = 3;
                    static final int defaultValue = ENABLE << BIT_EAN_8;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_EAN_8, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_EAN_8, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_EAN_8, mOneDRegisterValue);
                    }
                }

                public static class EAN_13 {
                    static final int BIT_EAN_13 = 4;
                    static final int defaultValue = ENABLE << BIT_EAN_13;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_EAN_13, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_EAN_13, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_EAN_13, mOneDRegisterValue);
                    }
                }

                public static class ISBN {
                    static final int BIT_ISBN = 5;
                    static final int defaultValue = DISABLE << BIT_ISBN;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_ISBN, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_ISBN, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_ISBN, mOneDRegisterValue);
                    }
                }

                public static class ISSN {
                    static final int BIT_ISSN = 6;
                    static final int defaultValue = DISABLE << BIT_ISSN;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_ISSN, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_ISSN, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_ISSN, mOneDRegisterValue);
                    }
                }

                public static class CODE_128 {
                    static final int BIT_CODE_128 = 7;
                    static final int defaultValue = ENABLE << BIT_CODE_128;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_CODE_128, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_CODE_128, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_CODE_128, mOneDRegisterValue);
                    }
                }

                public static class GS1_128 {
                    static final int BIT_GS1_128 = 8;
                    static final int defaultValue = ENABLE << BIT_GS1_128;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_GS1_128, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_GS1_128, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_GS1_128, mOneDRegisterValue);
                    }
                }

                public static class ISBT_128 {
                    static final int BIT_ISBT_128 = 9;
                    static final int defaultValue = ENABLE << BIT_ISBT_128;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_ISBT_128, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_ISBT_128, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_ISBT_128, mOneDRegisterValue);
                    }
                }

                public static class CODE_39 {
                    static final int BIT_CODE_39 = 10;
                    static final int defaultValue = ENABLE << BIT_CODE_39;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_CODE_39, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_CODE_39, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_CODE_39, mOneDRegisterValue);
                    }
                }

                public static class Trioptic_39 {
                    static final int BIT_TRIOPTIC_39 = 11;
                    static final int defaultValue = DISABLE << BIT_TRIOPTIC_39;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_TRIOPTIC_39, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_TRIOPTIC_39, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_TRIOPTIC_39, mOneDRegisterValue);
                    }
                }

                public static class CODE_93 {
                    static final int BIT_CODE_93 = 12;
                    static final int defaultValue = DISABLE << BIT_CODE_93;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_CODE_93, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_CODE_93, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_CODE_93, mOneDRegisterValue);
                    }
                }

                public static class CODE_11 {
                    static final int BIT_CODE_11 = 13;
                    static final int defaultValue = DISABLE << BIT_CODE_11;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_CODE_11, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_CODE_11, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_CODE_11, mOneDRegisterValue);
                    }
                }

                public static class Interleaved_2of5 {
                    static final int BIT_INTERLEAVED_2OF5 = 14;
                    static final int defaultValue = ENABLE << BIT_INTERLEAVED_2OF5;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_INTERLEAVED_2OF5, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_INTERLEAVED_2OF5, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_INTERLEAVED_2OF5, mOneDRegisterValue);
                    }
                }

                public static class Discrete_2of5 {
                    static final int BIT_DISCRETE_2OF5 = 15;
                    static final int defaultValue = ENABLE << BIT_DISCRETE_2OF5;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_DISCRETE_2OF5, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_DISCRETE_2OF5, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_DISCRETE_2OF5, mOneDRegisterValue);
                    }
                }

                public static class Codabar {
                    static final int BIT_CODABAR = 16;
                    static final int defaultValue = ENABLE << BIT_CODABAR;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_CODABAR, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_CODABAR, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_CODABAR, mOneDRegisterValue);
                    }
                }

                public static class MSI {
                    static final int BIT_MSI = 17;
                    static final int defaultValue = ENABLE << BIT_MSI;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_MSI, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_MSI, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_MSI, mOneDRegisterValue);
                    }
                }

                public static class Chinese_2of5 {
                    static final int defaultValue = DISABLE;
                    static final int BIT_CHINESE_2OF5 = 18;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_CHINESE_2OF5, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_CHINESE_2OF5, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_CHINESE_2OF5, mOneDRegisterValue);
                    }
                }

                public static class Matrix_2of5 {
                    static final int defaultValue = DISABLE;
                    static final int BIT_MATRIX_2OF5 = 19;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_MATRIX_2OF5, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_MATRIX_2OF5, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_MATRIX_2OF5, mOneDRegisterValue);
                    }
                }

                public static class Korean_3of5 {
                    static final int defaultValue = DISABLE;
                    static final int BIT_KOREAN_2OF5 = 20;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_KOREAN_2OF5, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_KOREAN_2OF5, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_KOREAN_2OF5, mOneDRegisterValue);
                    }
                }

                public static class GS1Databar {
                    static final int BIT_GS1_DATABAR = 21;
                    static final int defaultValue = ENABLE << BIT_GS1_DATABAR;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_GS1_DATABAR, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_GS1_DATABAR, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_GS1_DATABAR, mOneDRegisterValue);
                    }
                }

                public static class GS1DatabarLimited {
                    static final int BIT_GS1_DATABAR_LIMITED = 22;
                    static final int defaultValue = ENABLE << BIT_GS1_DATABAR_LIMITED;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_GS1_DATABAR_LIMITED, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_GS1_DATABAR_LIMITED, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_GS1_DATABAR_LIMITED, mOneDRegisterValue);
                    }
                }

                public static class GS1DatabarExpanded {
                    static final int BIT_GS1_DATABAR_EXPANDED = 23;
                    static final int defaultValue = ENABLE << BIT_GS1_DATABAR_EXPANDED;

                    public static void enable() {
                        mOneDRegisterValue = Scanner.enable(BIT_GS1_DATABAR_EXPANDED, mOneDRegisterValue);
                    }

                    public static void disable() {
                        mOneDRegisterValue = Scanner.disable(BIT_GS1_DATABAR_EXPANDED, mOneDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_GS1_DATABAR_EXPANDED, mOneDRegisterValue);
                    }
                }
            }

            public static class TwoD {
                //private static int mTwoDRegisterValue = 0x3B800;
                private static int mTwoDRegisterValue = getDefault();

                private static int getDefault() {
                    return USPostnet.defaultValue
                            | USPlanet.defaultValue
                            | UKPostal.defaultValue
                            | JapanPostal.defaultValue
                            | AustraliaPost.defaultValue
                            | NetherlandsKixCode.defaultValue
                            | InteligentMail.defaultValue
                            | UPU_FICS_Postal.defaultValue
                            | CompositeCC_C.defaultValue
                            | CompositeCC_AB.defaultValue
                            | CompositeTLC_39.defaultValue
                            | PDF_417.defaultValue
                            | MicroPDF_417.defaultValue
                            | DataMatrix.defaultValue
                            | MaxiCode.defaultValue
                            | QRCode.defaultValue
                            | MicroQR.defaultValue
                            | Aztec.defaultValue
                            | HanXin.defaultValue;
                }

                public static void setDefault() {
                    mTwoDRegisterValue = getDefault();
                }

                public static int getRegister() {
                    return mTwoDRegisterValue;
                }

                public static void setRegister(int value) {
                    mTwoDRegisterValue = value;
                }

                public static class USPostnet {
                    static final int BIT_US_POSTNET = 0;
                    static final int defaultValue = DISABLE << BIT_US_POSTNET;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_US_POSTNET, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_US_POSTNET, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_US_POSTNET, mTwoDRegisterValue);
                    }
                }

                public static class USPlanet {
                    static final int BIT_US_PLANET = 1;
                    static final int defaultValue = DISABLE << BIT_US_PLANET;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_US_PLANET, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_US_PLANET, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_US_PLANET, mTwoDRegisterValue);
                    }
                }

                public static class UKPostal {
                    static final int BIT_UK_POSTAL = 2;
                    static final int defaultValue = DISABLE << BIT_UK_POSTAL;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_UK_POSTAL, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_UK_POSTAL, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_UK_POSTAL, mTwoDRegisterValue);
                    }
                }

                public static class JapanPostal {
                    static final int BIT_JAPAN_POSTAL = 3;
                    static final int defaultValue = DISABLE << BIT_JAPAN_POSTAL;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_JAPAN_POSTAL, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_JAPAN_POSTAL, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_JAPAN_POSTAL, mTwoDRegisterValue);
                    }
                }

                public static class AustraliaPost {
                    static final int BIT_AUSTRALIA_POST = 4;
                    static final int defaultValue = DISABLE << BIT_AUSTRALIA_POST;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_AUSTRALIA_POST, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_AUSTRALIA_POST, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_AUSTRALIA_POST, mTwoDRegisterValue);
                    }
                }

                public static class NetherlandsKixCode {
                    static final int BIT_NETHERLANDS_KIX_CODE = 5;
                    static final int defaultValue = DISABLE << BIT_NETHERLANDS_KIX_CODE;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_NETHERLANDS_KIX_CODE, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_NETHERLANDS_KIX_CODE, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_NETHERLANDS_KIX_CODE, mTwoDRegisterValue);
                    }
                }

                public static class InteligentMail {
                    static final int BIT_INTELIGENT_MAIL = 6;
                    static final int defaultValue = DISABLE << BIT_INTELIGENT_MAIL;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_INTELIGENT_MAIL, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_INTELIGENT_MAIL, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_INTELIGENT_MAIL, mTwoDRegisterValue);
                    }
                }

                public static class UPU_FICS_Postal {
                    static final int BIT_UPU_FICS_POSTAL = 7;
                    static final int defaultValue = DISABLE << BIT_UPU_FICS_POSTAL;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_UPU_FICS_POSTAL, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_UPU_FICS_POSTAL, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_UPU_FICS_POSTAL, mTwoDRegisterValue);
                    }
                }

                public static class CompositeCC_C {
                    static final int BIT_COMPOSITE_CC_C = 8;
                    static final int defaultValue = DISABLE << BIT_COMPOSITE_CC_C;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_COMPOSITE_CC_C, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_COMPOSITE_CC_C, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_COMPOSITE_CC_C, mTwoDRegisterValue);
                    }
                }

                public static class CompositeCC_AB {
                    static final int BIT_COMPOSITE_CC_AB = 9;
                    static final int defaultValue = DISABLE << BIT_COMPOSITE_CC_AB;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_COMPOSITE_CC_AB, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_COMPOSITE_CC_AB, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_COMPOSITE_CC_AB, mTwoDRegisterValue);
                    }
                }

                public static class CompositeTLC_39 {
                    static final int BIT_COMPOSITE_TLC_39 = 10;
                    static final int defaultValue = DISABLE << BIT_COMPOSITE_TLC_39;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_COMPOSITE_TLC_39, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_COMPOSITE_TLC_39, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_COMPOSITE_TLC_39, mTwoDRegisterValue);
                    }
                }

                public static class PDF_417 {
                    static final int BIT_PDF_417 = 11;
                    static final int defaultValue = ENABLE << BIT_PDF_417;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_PDF_417, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_PDF_417, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_PDF_417, mTwoDRegisterValue);
                    }
                }

                public static class MicroPDF_417 {
                    static final int BIT_MICRO_PDF_417 = 12;
                    static final int defaultValue = ENABLE << BIT_MICRO_PDF_417;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_MICRO_PDF_417, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_MICRO_PDF_417, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_MICRO_PDF_417, mTwoDRegisterValue);
                    }
                }

                public static class DataMatrix {
                    static final int BIT_DATA_MATRIX = 13;
                    static final int defaultValue = ENABLE << BIT_DATA_MATRIX;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_DATA_MATRIX, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_DATA_MATRIX, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_DATA_MATRIX, mTwoDRegisterValue);
                    }
                }

                public static class MaxiCode {
                    static final int BIT_MAXI_CODE = 14;
                    static final int defaultValue = DISABLE << BIT_MAXI_CODE;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_MAXI_CODE, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_MAXI_CODE, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_MAXI_CODE, mTwoDRegisterValue);
                    }
                }

                public static class QRCode {
                    static final int BIT_QR_CODE = 15;
                    static final int defaultValue = ENABLE << BIT_QR_CODE;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_QR_CODE, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_QR_CODE, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_QR_CODE, mTwoDRegisterValue);
                    }
                }

                public static class MicroQR {
                    static final int BIT_MICRO_QR = 16;
                    static final int defaultValue = ENABLE << BIT_MICRO_QR;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_MICRO_QR, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_MICRO_QR, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_MICRO_QR, mTwoDRegisterValue);
                    }
                }

                public static class Aztec {
                    static final int BIT_AZTEC = 17;
                    static final int defaultValue = ENABLE << BIT_AZTEC;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_AZTEC, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_AZTEC, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_AZTEC, mTwoDRegisterValue);
                    }
                }

                public static class HanXin {
                    static final int BIT_HANXIN = 18;
                    static final int defaultValue = DISABLE << BIT_HANXIN;

                    public static void enable() {
                        mTwoDRegisterValue = Scanner.enable(BIT_HANXIN, mTwoDRegisterValue);
                    }

                    public static void disable() {
                        mTwoDRegisterValue = Scanner.disable(BIT_HANXIN, mTwoDRegisterValue);
                    }

                    public static boolean isEnable() {
                        return Scanner.isEnable(BIT_HANXIN, mTwoDRegisterValue);
                    }
                }
            }

            public static class AdvancedConfig {
                public static class OneD {
                    public static class UPCEAN {
                        //private static int mUPCEANRegisterValue = 0x42720AF;
                        private static int mUPCEANRegisterValue = getDefault();

                        private static int getDefault() {
                            return TransmitCheckDigit.UPC_A.defaultValue
                                    | TransmitCheckDigit.UPC_E0.defaultValue
                                    | TransmitCheckDigit.UPC_E1.defaultValue
                                    | Preamble.UPC_A.defaultValue
                                    | Preamble.UPC_E0.defaultValue
                                    | Preamble.UPC_E1.defaultValue
                                    | Convert.UPC_E0ToA.defaultValue
                                    | Convert.UPC_E1ToA.defaultValue
                                    | UPCReducedQuietZone.defaultValue
                                    | SupplementalRedundancy.defaultValue
                                    | SupplementalAIMIDFormat.defaultValue
                                    | DecodeSupplementals.defaultValue
                                    | EAN8Extend.defaultValue
                                    | UCCCouponExtendedCode.defaultValue
                                    | CouponReport.defaultValue;
                        }

                        public static void setDefault() {
                            mUPCEANRegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mUPCEANRegisterValue;
                        }

                        public static void setRegister(int value) {
                            mUPCEANRegisterValue = value;
                        }

                        public static class TransmitCheckDigit {
                            public static class UPC_A {
                                static final int BIT_T_UPC_A = 0;
                                static final int defaultValue = ENABLE << BIT_T_UPC_A;

                                public static void enable() {
                                    mUPCEANRegisterValue = Scanner.enable(BIT_T_UPC_A, mUPCEANRegisterValue);
                                }

                                public static void disable() {
                                    mUPCEANRegisterValue = Scanner.disable(BIT_T_UPC_A, mUPCEANRegisterValue);
                                }

                                public static boolean isEnable() {
                                    return Scanner.isEnable(BIT_T_UPC_A, mUPCEANRegisterValue);
                                }
                            }

                            public static class UPC_E0 {
                                static final int BIT_T_UPC_E0 = 1;
                                static final int defaultValue = ENABLE << BIT_T_UPC_E0;

                                public static void enable() {
                                    mUPCEANRegisterValue = Scanner.enable(BIT_T_UPC_E0, mUPCEANRegisterValue);
                                }

                                public static void disable() {
                                    mUPCEANRegisterValue = Scanner.disable(BIT_T_UPC_E0, mUPCEANRegisterValue);
                                }

                                public static boolean isEnable() {
                                    return Scanner.isEnable(BIT_T_UPC_E0, mUPCEANRegisterValue);
                                }
                            }

                            public static class UPC_E1 {
                                static final int BIT_T_UPC_E1 = 2;
                                static final int defaultValue = ENABLE << BIT_T_UPC_E1;

                                public static void enable() {
                                    mUPCEANRegisterValue = Scanner.enable(BIT_T_UPC_E1, mUPCEANRegisterValue);
                                }

                                public static void disable() {
                                    mUPCEANRegisterValue = Scanner.disable(BIT_T_UPC_E1, mUPCEANRegisterValue);
                                }

                                public static boolean isEnable() {
                                    return Scanner.isEnable(BIT_T_UPC_E1, mUPCEANRegisterValue);
                                }
                            }
                        }

                        public static class Preamble {
                            public static class UPC_A {
                                static final int BIT_P_UPC_A = 3;
                                static final int defaultValue = 1 << BIT_P_UPC_A;
                                static final int MAX_VALUE = 0x3;

                                public static void setNo() {
                                    mUPCEANRegisterValue = Scanner.setValue(BIT_P_UPC_A, 0x0, mUPCEANRegisterValue, MAX_VALUE);
                                }

                                public static void setSystemChar() {
                                    mUPCEANRegisterValue = Scanner.setValue(BIT_P_UPC_A, 0x1, mUPCEANRegisterValue, MAX_VALUE);
                                }

                                public static void setSystemCharAndCountryCode() {
                                    mUPCEANRegisterValue = Scanner.setValue(BIT_P_UPC_A, 0x2, mUPCEANRegisterValue, MAX_VALUE);
                                }

                                public static int getValue() {
                                    return Scanner.getValue(BIT_P_UPC_A, mUPCEANRegisterValue, MAX_VALUE);
                                }
                            }

                            public static class UPC_E0 {
                                static final int BIT_P_UPC_E0 = 5;
                                static final int defaultValue = 1 << BIT_P_UPC_E0;
                                static final int MAX_VALUE = 0x3;

                                public static void setNo() {
                                    mUPCEANRegisterValue = Scanner.setValue(BIT_P_UPC_E0, 0x0, mUPCEANRegisterValue, MAX_VALUE);
                                }

                                public static void setSystemChar() {
                                    mUPCEANRegisterValue = Scanner.setValue(BIT_P_UPC_E0, 0x1, mUPCEANRegisterValue, MAX_VALUE);
                                }

                                public static void setSystemCharAndCountryCode() {
                                    mUPCEANRegisterValue = Scanner.setValue(BIT_P_UPC_E0, 0x2, mUPCEANRegisterValue, MAX_VALUE);
                                }

                                public static int getValue() {
                                    return Scanner.getValue(BIT_P_UPC_E0, mUPCEANRegisterValue, MAX_VALUE);
                                }
                            }

                            public static class UPC_E1 {
                                static final int BIT_P_UPC_E1 = 7;
                                static final int defaultValue = 1 << BIT_P_UPC_E1;
                                static final int MAX_VALUE = 0x3;

                                public static void setNo() {
                                    mUPCEANRegisterValue = Scanner.setValue(BIT_P_UPC_E1, 0x0, mUPCEANRegisterValue, MAX_VALUE);
                                }

                                public static void setSystemChar() {
                                    mUPCEANRegisterValue = Scanner.setValue(BIT_P_UPC_E1, 0x1, mUPCEANRegisterValue, MAX_VALUE);
                                }

                                public static void setSystemCharAndCountryCode() {
                                    mUPCEANRegisterValue = Scanner.setValue(BIT_P_UPC_E1, 0x2, mUPCEANRegisterValue, MAX_VALUE);
                                }

                                public static int getValue() {
                                    return Scanner.getValue(BIT_P_UPC_E1, mUPCEANRegisterValue, MAX_VALUE);
                                }
                            }
                        }

                        public static class Convert {
                            public static class UPC_E0ToA {
                                static final int BIT_T_UPC_E0 = 9;
                                static final int defaultValue = DISABLE << BIT_T_UPC_E0;

                                public static void enable() {
                                    mUPCEANRegisterValue = Scanner.enable(BIT_T_UPC_E0, mUPCEANRegisterValue);
                                }

                                public static void disable() {
                                    mUPCEANRegisterValue = Scanner.disable(BIT_T_UPC_E0, mUPCEANRegisterValue);
                                }

                                public static boolean isEnable() {
                                    return Scanner.isEnable(BIT_T_UPC_E0, mUPCEANRegisterValue);
                                }
                            }

                            public static class UPC_E1ToA {
                                static final int BIT_T_UPC_E1 = 10;
                                static final int defaultValue = DISABLE << BIT_T_UPC_E1;

                                public static void enable() {
                                    mUPCEANRegisterValue = Scanner.enable(BIT_T_UPC_E1, mUPCEANRegisterValue);
                                }

                                public static void disable() {
                                    mUPCEANRegisterValue = Scanner.disable(BIT_T_UPC_E1, mUPCEANRegisterValue);
                                }

                                public static boolean isEnable() {
                                    return Scanner.isEnable(BIT_T_UPC_E1, mUPCEANRegisterValue);
                                }
                            }
                        }

                        public static class UPCReducedQuietZone {
                            static final int BIT_UPC_REDUCE = 11;
                            static final int defaultValue = DISABLE << BIT_UPC_REDUCE;

                            public static void enable() {
                                mUPCEANRegisterValue = Scanner.enable(BIT_UPC_REDUCE, mUPCEANRegisterValue);
                            }

                            public static void disable() {
                                mUPCEANRegisterValue = Scanner.disable(BIT_UPC_REDUCE, mUPCEANRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_UPC_REDUCE, mUPCEANRegisterValue);
                            }
                        }

                        public static class DecodeSupplementals {
                            static final int BIT_DECODE_SUPP = 12;
                            static final int defaultValue = 2 << BIT_DECODE_SUPP;
                            static final int MAX_VALUE = 0xF;

                            public static void ignoreSuppData() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_DECODE_SUPP, 0x0, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void onlyReadIncludeSuppData() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_DECODE_SUPP, 0x1, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void readDataNoMatterSupp() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_DECODE_SUPP, 0x2, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void allPrefixEnable() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_DECODE_SUPP, 0x3, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void prefix_378_397_ofEAN13() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_DECODE_SUPP, 0x4, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void prefix_978_979_ofEAN13() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_DECODE_SUPP, 0x5, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void prefix_414_419_434_439_ofEAN13() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_DECODE_SUPP, 0x6, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void prefix_977_ofEAN13() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_DECODE_SUPP, 0x7, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void prefix_491_ofEAN13() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_DECODE_SUPP, 0x8, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_DECODE_SUPP, mUPCEANRegisterValue, MAX_VALUE);
                            }
                        }

                        public static class SupplementalRedundancy {
                            static final int BIT_SUPP_REDUNDANCY = 16;
                            static final int defaultValue = 7 << BIT_SUPP_REDUNDANCY;
                            static final int MAX_VALUE = 0x1F;

                            public static int getRedundancy() {
                                return Scanner.getValue(BIT_SUPP_REDUNDANCY, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void setRedundancy(int value) {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_SUPP_REDUNDANCY, value, mUPCEANRegisterValue, MAX_VALUE);
                            }
                        }

                        public static class SupplementalAIMIDFormat {
                            static final int BIT_SUPP_AIM_ID_FORMAT = 21;
                            static final int defaultValue = 1 << BIT_SUPP_AIM_ID_FORMAT;
                            static final int MAX_VALUE = 0x3;

                            public static void setSeparate() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_SUPP_AIM_ID_FORMAT, 0x0, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void setCombined() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_SUPP_AIM_ID_FORMAT, 0x1, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void setSeparateTransmissions() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_SUPP_AIM_ID_FORMAT, 0x2, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_SUPP_AIM_ID_FORMAT, mUPCEANRegisterValue, MAX_VALUE);
                            }
                        }

                        public static class EAN8Extend {
                            static final int BIT_EAN8_EXT = 23;
                            static final int defaultValue = DISABLE << BIT_EAN8_EXT;

                            public static void enable() {
                                mUPCEANRegisterValue = Scanner.enable(BIT_EAN8_EXT, mUPCEANRegisterValue);
                            }

                            public static void disable() {
                                mUPCEANRegisterValue = Scanner.disable(BIT_EAN8_EXT, mUPCEANRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_EAN8_EXT, mUPCEANRegisterValue);
                            }
                        }

                        public static class UCCCouponExtendedCode {
                            static final int BIT_UCC_COUPON_EXT = 24;
                            static final int defaultValue = DISABLE << BIT_UCC_COUPON_EXT;

                            public static void enable() {
                                mUPCEANRegisterValue = Scanner.enable(BIT_UCC_COUPON_EXT, mUPCEANRegisterValue);
                            }

                            public static void disable() {
                                mUPCEANRegisterValue = Scanner.disable(BIT_UCC_COUPON_EXT, mUPCEANRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_UCC_COUPON_EXT, mUPCEANRegisterValue);
                            }
                        }

                        public static class CouponReport {
                            static final int BIT_COUPON_REPORT = 25;
                            static final int defaultValue = 2 << BIT_COUPON_REPORT;
                            static final int MAX_VALUE = 0x3;

                            public static void setOldCouponSymbols() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_COUPON_REPORT, 0x0, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void setNewCouponSymbols() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_COUPON_REPORT, 0x1, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static void setBothCouponSymbols() {
                                mUPCEANRegisterValue = Scanner.setValue(BIT_COUPON_REPORT, 0x2, mUPCEANRegisterValue, MAX_VALUE);
                            }

                            public static int getReport() {
                                return Scanner.getValue(BIT_COUPON_REPORT, mUPCEANRegisterValue, MAX_VALUE);
                            }
                        }
                    }

                    public static class ISBN {
                        static final int defaultValue = 0;
                        //private static int mBooklandRegister = 0;
                        private static int mBooklandRegister = getDefault();

                        private static int getDefault() {
                            return defaultValue;
                        }

                        public static void setDefault() {
                            mBooklandRegister = getDefault();
                        }

                        public static int getRegister() {
                            return mBooklandRegister;
                        }

                        public static void setRegister(int value) {
                            mBooklandRegister = value;
                        }

                        public static void setISBN10() {
                            mBooklandRegister = 0;
                        }

                        public static void setISBN13() {
                            mBooklandRegister = 1;
                        }

                        public static int getFormat() {
                            return mBooklandRegister;
                        }
                    }

                    public static class Code128 {
                        //private static int mCode128Register = 0;
                        private static int mCode128Register = getDefault();

                        private static int getDefault() {
                            return Length.defaultLengtheadlineMedium
                                    | Length.defaultLength2
                                    | ReducedQuietZone.defaultValue
                                    | IgnoreCode128FNC4.defaultValue;
                        }

                        public static void setDefault() {
                            mCode128Register = getDefault();
                        }

                        public static int getRegister() {
                            return mCode128Register;
                        }

                        public static void setRegister(int value) {
                            mCode128Register = value;
                        }

                        public static class IgnoreCode128FNC4 {
                            static final int BIT_CODE128_FNC4 = 17;
                            static final int defaultValue = DISABLE << BIT_CODE128_FNC4;

                            public static void enable() {
                                mCode128Register = Scanner.enable(BIT_CODE128_FNC4, mCode128Register);
                            }

                            public static void disable() {
                                mCode128Register = Scanner.disable(BIT_CODE128_FNC4, mCode128Register);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_CODE128_FNC4, mCode128Register);
                            }
                        }

                        public static class ReducedQuietZone {
                            static final int BIT_CODE128_FNC4 = 16;
                            static final int defaultValue = DISABLE << BIT_CODE128_FNC4;

                            public static void enable() {
                                mCode128Register = Scanner.enable(BIT_CODE128_FNC4, mCode128Register);
                            }

                            public static void disable() {
                                mCode128Register = Scanner.disable(BIT_CODE128_FNC4, mCode128Register);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_CODE128_FNC4, mCode128Register);
                            }
                        }

                        public static class Length {
                            static final int BIT_LENGTheadlineMedium = 0;
                            static final int BIT_LENGTH2 = 8;
                            static final int defaultLengtheadlineMedium = 1 << BIT_LENGTheadlineMedium;
                            static final int defaultLength2 = 55 << BIT_LENGTH2;
                            static final int MAX_VALUE = 0xFF;

                            public static int getL1() {
                                return Scanner.getValue(BIT_LENGTheadlineMedium, mCode128Register, MAX_VALUE);
                            }

                            public static void setL1(int value) {
                                mCode128Register = Scanner.setValue(BIT_LENGTheadlineMedium, value, mCode128Register, MAX_VALUE);
                            }

                            public static int getL2() {
                                return Scanner.getValue(BIT_LENGTH2, mCode128Register, MAX_VALUE);
                            }

                            public static void setL2(int value) {
                                mCode128Register = Scanner.setValue(BIT_LENGTH2, value, mCode128Register, MAX_VALUE);
                            }
                        }
                    }

                    public static class ISBT {
                        //private static int mISBTRegister = 0x54;
                        private static int mISBTRegister = getDefault();

                        private static int getDefault() {
                            return ISBTConcatenation.defaultValue
                                    | ISBTConcatenationRedundancy.defaultValue
                                    | CheckISBTTable.defaultValue;
                        }

                        public static void setDefault() {
                            mISBTRegister = getDefault();
                        }

                        public static int getRegister() {
                            return mISBTRegister;
                        }

                        public static void setRegister(int value) {
                            mISBTRegister = value;
                        }

                        public static class ISBTConcatenationRedundancy {
                            static final int BIT_ISBT_REDUNDANCY = 3;
                            static final int defaultValue = 10 << BIT_ISBT_REDUNDANCY;
                            static final int MAX_VALUE = 0x1F;

                            public static int getValue() {
                                return Scanner.getValue(BIT_ISBT_REDUNDANCY, mISBTRegister, MAX_VALUE);
                            }

                            public static void setValue(int value) {
                                mISBTRegister = Scanner.setValue(BIT_ISBT_REDUNDANCY, value, mISBTRegister, MAX_VALUE);
                            }
                        }

                        public static class CheckISBTTable {
                            static final int BIT_ISBT_TABLE = 2;
                            static final int defaultValue = ENABLE << BIT_ISBT_TABLE;

                            public static void enable() {
                                mISBTRegister = Scanner.enable(BIT_ISBT_TABLE, mISBTRegister);
                            }

                            public static void disable() {
                                mISBTRegister = Scanner.disable(BIT_ISBT_TABLE, mISBTRegister);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_ISBT_TABLE, mISBTRegister);
                            }
                        }

                        public static class ISBTConcatenation {
                            static final int BIT_ISBT_CONCATENATION = 0;
                            static final int defaultValue = 0 << BIT_ISBT_CONCATENATION;
                            static final int MAX_VALUE = 0x3;

                            public static void disable() {
                                mISBTRegister = Scanner.setValue(BIT_ISBT_CONCATENATION, 0x0, mISBTRegister, MAX_VALUE);
                            }

                            public static void enable() {
                                mISBTRegister = Scanner.setValue(BIT_ISBT_CONCATENATION, 0x1, mISBTRegister, MAX_VALUE);
                            }

                            public static void setAutodiscriminate() {
                                mISBTRegister = Scanner.setValue(BIT_ISBT_CONCATENATION, 0x2, mISBTRegister, MAX_VALUE);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_ISBT_CONCATENATION, mISBTRegister, MAX_VALUE);
                            }
                        }
                    }

                    public static class Code39 {
                        //private static int mCode39RegisterValue = 0x80000;
                        private static int mCode39RegisterValue = getDefault();

                        private static int getDefault() {
                            return ConvertCode39to32.defaultValue
                                    | Code32AddPrefix_A.defaultValue
                                    | Length.defaultLengtheadlineMedium
                                    | Length.defaultLength2
                                    | CheckDigitVerification.defaultValue
                                    | Code39FullASCIIConversion.defaultValue
                                    | Code39BufferingScanStore.defaultValue
                                    | Code39ReducedQuietZone.defaultValue;
                        }

                        public static void setDefault() {
                            mCode39RegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mCode39RegisterValue;
                        }

                        public static void setRegister(int value) {
                            mCode39RegisterValue = value;
                        }

                        public static class ConvertCode39to32 {
                            static final int BIT_CONVERT_CODE39_TO_32 = 0;
                            static final int defaultValue = DISABLE << BIT_CONVERT_CODE39_TO_32;

                            public static void enable() {
                                mCode39RegisterValue = Scanner.enable(BIT_CONVERT_CODE39_TO_32, mCode39RegisterValue);
                            }

                            public static void disable() {
                                mCode39RegisterValue = Scanner.disable(BIT_CONVERT_CODE39_TO_32, mCode39RegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_CONVERT_CODE39_TO_32, mCode39RegisterValue);
                            }
                        }

                        public static class Code32AddPrefix_A {
                            static final int BIT_CODE32_ADDPREFIX_A = 1;
                            static final int defaultValue = DISABLE << BIT_CODE32_ADDPREFIX_A;

                            public static void enable() {
                                mCode39RegisterValue = Scanner.enable(BIT_CODE32_ADDPREFIX_A, mCode39RegisterValue);
                            }

                            public static void disable() {
                                mCode39RegisterValue = Scanner.disable(BIT_CODE32_ADDPREFIX_A, mCode39RegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_CODE32_ADDPREFIX_A, mCode39RegisterValue);
                            }

                        }

                        public static class Length {
                            static final int BIT_LENGTheadlineMedium = 2;
                            static final int BIT_LENGTH2 = 10;
                            static final int defaultLengtheadlineMedium = 1 << BIT_LENGTheadlineMedium;
                            static final int defaultLength2 = 55 << BIT_LENGTH2;
                            static final int MAX_VALUE = 0xFF;

                            public static int getL1() {
                                return Scanner.getValue(BIT_LENGTheadlineMedium, mCode39RegisterValue, MAX_VALUE);
                            }

                            public static void setL1(int value) {
                                mCode39RegisterValue = Scanner.setValue(BIT_LENGTheadlineMedium, value, mCode39RegisterValue, MAX_VALUE);
                            }

                            public static int getL2() {
                                return Scanner.getValue(BIT_LENGTH2, mCode39RegisterValue, MAX_VALUE);
                            }

                            public static void setL2(int value) {
                                mCode39RegisterValue = Scanner.setValue(BIT_LENGTH2, value, mCode39RegisterValue, MAX_VALUE);
                            }
                        }

                        public static class CheckDigitVerification {
                            static final int BIT_CODE39_VERIFICATION_CHECKDIGIT = 18;
                            static final int defaultValue = DISABLE << BIT_CODE39_VERIFICATION_CHECKDIGIT;
                            static final int MAX_VALUE = 0x3;

                            public static void disable() {
                                mCode39RegisterValue = Scanner.setValue(BIT_CODE39_VERIFICATION_CHECKDIGIT, 0x0, mCode39RegisterValue, MAX_VALUE);
                            }

                            public static void setDoNotTransmit() {
                                mCode39RegisterValue = Scanner.setValue(BIT_CODE39_VERIFICATION_CHECKDIGIT, 0x1, mCode39RegisterValue, MAX_VALUE);
                            }

                            public static void setTransmit() {
                                mCode39RegisterValue = Scanner.setValue(BIT_CODE39_VERIFICATION_CHECKDIGIT, 0x2, mCode39RegisterValue, MAX_VALUE);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_CODE39_VERIFICATION_CHECKDIGIT, mCode39RegisterValue, MAX_VALUE);
                            }
                        }

                        public static class Code39FullASCIIConversion {
                            static final int BIT_CODE39_ASCII_CONVERSION = 20;
                            static final int defaultValue = DISABLE << BIT_CODE39_ASCII_CONVERSION;

                            public static void enable() {
                                mCode39RegisterValue = Scanner.enable(BIT_CODE39_ASCII_CONVERSION, mCode39RegisterValue);
                            }

                            public static void disable() {
                                mCode39RegisterValue = Scanner.disable(BIT_CODE39_ASCII_CONVERSION, mCode39RegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_CODE39_ASCII_CONVERSION, mCode39RegisterValue);
                            }
                        }

                        public static class Code39BufferingScanStore {
                            static final int BIT_CODE39_BUFFERING = 21;
                            static final int defaultValue = DISABLE << BIT_CODE39_BUFFERING;

                            public static void enable() {
                                mCode39RegisterValue = Scanner.enable(BIT_CODE39_BUFFERING, mCode39RegisterValue);
                            }

                            public static void disable() {
                                mCode39RegisterValue = Scanner.disable(BIT_CODE39_BUFFERING, mCode39RegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_CODE39_BUFFERING, mCode39RegisterValue);
                            }
                        }

                        public static class Code39ReducedQuietZone {
                            static final int BIT_CODE39_QUIET_ZONE = 22;
                            static final int defaultValue = DISABLE << BIT_CODE39_QUIET_ZONE;

                            public static void enable() {
                                mCode39RegisterValue = Scanner.enable(BIT_CODE39_QUIET_ZONE, mCode39RegisterValue);
                            }

                            public static void disable() {
                                mCode39RegisterValue = Scanner.disable(BIT_CODE39_QUIET_ZONE, mCode39RegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_CODE39_QUIET_ZONE, mCode39RegisterValue);
                            }
                        }
                    }

                    public static class Code93 {
                        //private static int mCode93RegisterValue = 0;
                        private static int mCode93RegisterValue = getDefault();

                        private static int getDefault() {
                            return Length.defaultLengtheadlineMedium
                                    | Length.defaultLength2;
                        }

                        public static void setDefault() {
                            mCode93RegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mCode93RegisterValue;
                        }

                        public static void setRegister(int value) {
                            mCode93RegisterValue = value;
                        }

                        public static class Length {
                            static final int BIT_LENGTheadlineMedium = 0;
                            static final int BIT_LENGTH2 = 8;
                            static final int defaultLengtheadlineMedium = 1 << BIT_LENGTheadlineMedium;
                            static final int defaultLength2 = 55 << BIT_LENGTH2;
                            static final int MAX_VALUE = 0xFF;

                            public static int getL1() {
                                return Scanner.getValue(BIT_LENGTheadlineMedium, mCode93RegisterValue, MAX_VALUE);
                            }

                            public static void setL1(int value) {
                                mCode93RegisterValue = Scanner.setValue(BIT_LENGTheadlineMedium, value, mCode93RegisterValue, MAX_VALUE);
                            }

                            public static int getL2() {
                                return Scanner.getValue(BIT_LENGTH2, mCode93RegisterValue, MAX_VALUE);
                            }

                            public static void setL2(int value) {
                                mCode93RegisterValue = Scanner.setValue(BIT_LENGTH2, value, mCode93RegisterValue, MAX_VALUE);
                            }
                        }
                    }

                    public static class Code11 {
                        //private static int mCode11RegisterValue = 0x40000;
                        private static int mCode11RegisterValue = getDefault();

                        private static int getDefault() {
                            return Length.defaultLengtheadlineMedium
                                    | Length.defaultLength2
                                    | CheckDigitVerification.defaultValue
                                    | TransmitCheckDigit.defaultValue;
                        }

                        public static void setDefault() {
                            mCode11RegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mCode11RegisterValue;
                        }

                        public static void setRegister(int value) {
                            mCode11RegisterValue = value;
                        }

                        public static class Length {
                            static final int BIT_LENGTheadlineMedium = 0;
                            static final int BIT_LENGTH2 = 8;
                            static final int defaultLengtheadlineMedium = 1 << BIT_LENGTheadlineMedium;
                            static final int defaultLength2 = 55 << BIT_LENGTH2;
                            static final int MAX_VALUE = 0xFF;

                            public static int getL1() {
                                return Scanner.getValue(BIT_LENGTheadlineMedium, mCode11RegisterValue, MAX_VALUE);
                            }

                            public static void setL1(int value) {
                                mCode11RegisterValue = Scanner.setValue(BIT_LENGTheadlineMedium, value, mCode11RegisterValue, MAX_VALUE);
                            }

                            public static int getL2() {
                                return Scanner.getValue(BIT_LENGTH2, mCode11RegisterValue, MAX_VALUE);
                            }

                            public static void setL2(int value) {
                                mCode11RegisterValue = Scanner.setValue(BIT_LENGTH2, value, mCode11RegisterValue, MAX_VALUE);
                            }
                        }

                        public static class CheckDigitVerification {
                            static final int BIT_CODE11_VERIFICATION_CHECKDIGIT = 16;
                            static final int defaultValue = DISABLE << BIT_CODE11_VERIFICATION_CHECKDIGIT;
                            static final int MAX_VALUE = 0x3;

                            public static void disable() {
                                mCode11RegisterValue = Scanner.setValue(BIT_CODE11_VERIFICATION_CHECKDIGIT, 0x0, mCode11RegisterValue, MAX_VALUE);
                            }

                            public static void oneCheckDigit() {
                                mCode11RegisterValue = Scanner.setValue(BIT_CODE11_VERIFICATION_CHECKDIGIT, 0x1, mCode11RegisterValue, MAX_VALUE);
                            }

                            public static void twoCheckDigit() {
                                mCode11RegisterValue = Scanner.setValue(BIT_CODE11_VERIFICATION_CHECKDIGIT, 0x2, mCode11RegisterValue, MAX_VALUE);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_CODE11_VERIFICATION_CHECKDIGIT, mCode11RegisterValue, MAX_VALUE);
                            }
                        }

                        public static class TransmitCheckDigit {
                            static final int BIT_CODE11_TRANSMIT_CHECKDIGIT = 18;
                            static final int defaultValue = ENABLE << BIT_CODE11_TRANSMIT_CHECKDIGIT;

                            public static void enable() {
                                mCode11RegisterValue = Scanner.enable(BIT_CODE11_TRANSMIT_CHECKDIGIT, mCode11RegisterValue);
                            }

                            public static void disable() {
                                mCode11RegisterValue = Scanner.disable(BIT_CODE11_TRANSMIT_CHECKDIGIT, mCode11RegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_CODE11_TRANSMIT_CHECKDIGIT, mCode11RegisterValue);
                            }
                        }
                    }

                    public static class Interleaved2of5 {
                        //private static int mI2of5RegisterValue = 0x140000;
                        private static int mI2of5RegisterValue = getDefault();

                        private static int getDefault() {
                            return Length.defaultLengtheadlineMedium
                                    | Length.defaultLength2
                                    | CheckDigitVerification.defaultValue
                                    | ConvertI2of5ToEAN13.defaultValue
                                    | SecurityLevel.defaultValue
                                    | ReducedQuietZone.defaultValue;
                        }

                        public static void setDefault() {
                            mI2of5RegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mI2of5RegisterValue;
                        }

                        public static void setRegister(int value) {
                            mI2of5RegisterValue = value;
                        }

                        public static class Length {
                            static final int BIT_LENGTheadlineMedium = 0;
                            static final int BIT_LENGTH2 = 8;
                            static final int defaultLengtheadlineMedium = 1 << BIT_LENGTheadlineMedium;
                            static final int defaultLength2 = 55 << BIT_LENGTH2;
                            static final int MAX_VALUE = 0xFF;

                            public static int getL1() {
                                return Scanner.getValue(BIT_LENGTheadlineMedium, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setL1(int value) {
                                mI2of5RegisterValue = Scanner.setValue(BIT_LENGTheadlineMedium, value, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static int getL2() {
                                return Scanner.getValue(BIT_LENGTH2, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setL2(int value) {
                                mI2of5RegisterValue = Scanner.setValue(BIT_LENGTH2, value, mI2of5RegisterValue, MAX_VALUE);
                            }
                        }

                        public static class CheckDigitVerification {
                            static final int BIT_I2OF5_VERIFICATION_CHECKDIGIT = 16;
                            static final int defaultValue = 0 << BIT_I2OF5_VERIFICATION_CHECKDIGIT;
                            static final int MAX_VALUE = 0x7;

                            public static void disable() {
                                mI2of5RegisterValue = Scanner.setValue(BIT_I2OF5_VERIFICATION_CHECKDIGIT, 0x0, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setDoNotTransmitUSS() {
                                mI2of5RegisterValue = Scanner.setValue(BIT_I2OF5_VERIFICATION_CHECKDIGIT, 0x1, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setTransmitUSS() {
                                mI2of5RegisterValue = Scanner.setValue(BIT_I2OF5_VERIFICATION_CHECKDIGIT, 0x2, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setDoNotTransmitOPCC() {
                                mI2of5RegisterValue = Scanner.setValue(BIT_I2OF5_VERIFICATION_CHECKDIGIT, 0x3, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setTransmitOPCC() {
                                mI2of5RegisterValue = Scanner.setValue(BIT_I2OF5_VERIFICATION_CHECKDIGIT, 0x4, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_I2OF5_VERIFICATION_CHECKDIGIT, mI2of5RegisterValue, MAX_VALUE);
                            }
                        }

                        public static class ConvertI2of5ToEAN13 {
                            static final int BIT_I2OF5_TO_EAN13 = 19;
                            static final int defaultValue = DISABLE << BIT_I2OF5_TO_EAN13;

                            public static void enable() {
                                mI2of5RegisterValue = Scanner.enable(BIT_I2OF5_TO_EAN13, mI2of5RegisterValue);
                            }

                            public static void disable() {
                                mI2of5RegisterValue = Scanner.disable(BIT_I2OF5_TO_EAN13, mI2of5RegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_I2OF5_TO_EAN13, mI2of5RegisterValue);
                            }
                        }

                        public static class SecurityLevel {
                            static final int BIT_I2OF5_SECURITY = 20;
                            static final int defaultValue = 1 << BIT_I2OF5_SECURITY;
                            static final int MAX_VALUE = 0x3;

                            public static void level0() {
                                mI2of5RegisterValue = Scanner.setValue(BIT_I2OF5_SECURITY, 0x0, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static void level1() {
                                mI2of5RegisterValue = Scanner.setValue(BIT_I2OF5_SECURITY, 0x1, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static void level2() {
                                mI2of5RegisterValue = Scanner.setValue(BIT_I2OF5_SECURITY, 0x2, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static void level3() {
                                mI2of5RegisterValue = Scanner.setValue(BIT_I2OF5_SECURITY, 0x3, mI2of5RegisterValue, MAX_VALUE);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_I2OF5_SECURITY, mI2of5RegisterValue, MAX_VALUE);
                            }
                        }

                        public static class ReducedQuietZone {
                            static final int BIT_I2OF5_QUIET_ZONE = 22;
                            static final int defaultValue = DISABLE << BIT_I2OF5_QUIET_ZONE;

                            public static void enable() {
                                mI2of5RegisterValue = Scanner.enable(BIT_I2OF5_QUIET_ZONE, mI2of5RegisterValue);
                            }

                            public static void disable() {
                                mI2of5RegisterValue = Scanner.disable(BIT_I2OF5_QUIET_ZONE, mI2of5RegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_I2OF5_QUIET_ZONE, mI2of5RegisterValue);
                            }
                        }
                    }

                    public static class Discrete2of5 {
                        //private static int mD2of5RegisterValue = 0;
                        private static int mD2of5RegisterValue = getDefault();

                        private static int getDefault() {
                            return Length.defaultLengtheadlineMedium
                                    | Length.defaultLength2;
                        }

                        public static void setDefault() {
                            mD2of5RegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mD2of5RegisterValue;
                        }

                        public static void setRegister(int value) {
                            mD2of5RegisterValue = value;
                        }

                        public static class Length {
                            static final int BIT_LENGTheadlineMedium = 0;
                            static final int BIT_LENGTH2 = 8;
                            static final int defaultLengtheadlineMedium = 1 << BIT_LENGTheadlineMedium;
                            static final int defaultLength2 = 55 << BIT_LENGTH2;
                            static final int MAX_VALUE = 0xFF;

                            public static int getL1() {
                                return Scanner.getValue(BIT_LENGTheadlineMedium, mD2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setL1(int value) {
                                mD2of5RegisterValue = Scanner.setValue(BIT_LENGTheadlineMedium, value, mD2of5RegisterValue, MAX_VALUE);
                            }

                            public static int getL2() {
                                return Scanner.getValue(BIT_LENGTH2, mD2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setL2(int value) {
                                mD2of5RegisterValue = Scanner.setValue(BIT_LENGTH2, value, mD2of5RegisterValue, MAX_VALUE);
                            }
                        }
                    }

                    public static class Codabar {
                        //private static int mCodabarRegisterValue = 0x40000;
                        private static int mCodabarRegisterValue = getDefault();

                        private static int getDefault() {
                            return Length.defaultLengtheadlineMedium
                                    | Length.defaultLength2
                                    | CLSIEditing.defaultValue
                                    | NOTISEditing.defaultValue
                                    | StartStopCharacters.defaultValue;
                        }

                        public static void setDefault() {
                            mCodabarRegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mCodabarRegisterValue;
                        }

                        public static void setRegister(int value) {
                            mCodabarRegisterValue = value;
                        }

                        public static class Length {
                            static final int BIT_LENGTheadlineMedium = 0;
                            static final int BIT_LENGTH2 = 8;
                            static final int defaultLengtheadlineMedium = 1 << BIT_LENGTheadlineMedium;
                            static final int defaultLength2 = 55 << BIT_LENGTH2;
                            static final int MAX_VALUE = 0xFF;

                            public static int getL1() {
                                return Scanner.getValue(BIT_LENGTheadlineMedium, mCodabarRegisterValue, MAX_VALUE);
                            }

                            public static void setL1(int value) {
                                mCodabarRegisterValue = Scanner.setValue(BIT_LENGTheadlineMedium, value, mCodabarRegisterValue, MAX_VALUE);
                            }

                            public static int getL2() {
                                return Scanner.getValue(BIT_LENGTH2, mCodabarRegisterValue, MAX_VALUE);
                            }

                            public static void setL2(int value) {
                                mCodabarRegisterValue = Scanner.setValue(BIT_LENGTH2, value, mCodabarRegisterValue, MAX_VALUE);
                            }
                        }

                        public static class CLSIEditing {
                            final static int BIT_CLSI_EDIDING = 16;
                            static final int defaultValue = DISABLE << BIT_CLSI_EDIDING;

                            public static void enable() {
                                mCodabarRegisterValue = Scanner.enable(BIT_CLSI_EDIDING, mCodabarRegisterValue);
                            }

                            public static void disable() {
                                mCodabarRegisterValue = Scanner.disable(BIT_CLSI_EDIDING, mCodabarRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_CLSI_EDIDING, mCodabarRegisterValue);
                            }
                        }

                        public static class NOTISEditing {
                            final static int BIT_NOTIS_EDIDING = 17;
                            static final int defaultValue = DISABLE << BIT_NOTIS_EDIDING;

                            public static void enable() {
                                mCodabarRegisterValue = Scanner.enable(BIT_NOTIS_EDIDING, mCodabarRegisterValue);
                            }

                            public static void disable() {
                                mCodabarRegisterValue = Scanner.disable(BIT_NOTIS_EDIDING, mCodabarRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_NOTIS_EDIDING, mCodabarRegisterValue);
                            }
                        }

                        public static class StartStopCharacters {
                            final static int BIT_STARTSTOP_CHAR = 18;
                            static final int defaultValue = 1 << BIT_STARTSTOP_CHAR;

                            public static void setUpperCase() {
                                mCodabarRegisterValue = Scanner.disable(BIT_STARTSTOP_CHAR, mCodabarRegisterValue);
                            }

                            public static void setLowerCase() {
                                mCodabarRegisterValue = Scanner.enable(BIT_STARTSTOP_CHAR, mCodabarRegisterValue);
                            }

                            public static boolean isLowerCase() {
                                return Scanner.isEnable(BIT_STARTSTOP_CHAR, mCodabarRegisterValue);
                            }
                        }
                    }

                    public static class MSI {
                        //private static int mMSIRegisterValue = 0x60000;
                        private static int mMSIRegisterValue = getDefault();

                        private static int getDefault() {
                            return Length.defaultLengtheadlineMedium
                                    | Length.defaultLength2
                                    | CheckDigitVerification.defaultValue
                                    | TransmitCheckDigit.defaultValue;
                        }

                        public static void setDefault() {
                            mMSIRegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mMSIRegisterValue;
                        }

                        public static void setRegister(int value) {
                            mMSIRegisterValue = value;
                        }

                        public static class Length {
                            static final int BIT_LENGTheadlineMedium = 0;
                            static final int BIT_LENGTH2 = 8;
                            static final int defaultLengtheadlineMedium = 1 << BIT_LENGTheadlineMedium;
                            static final int defaultLength2 = 55 << BIT_LENGTH2;
                            static final int MAX_VALUE = 0xFF;

                            public static int getL1() {
                                return Scanner.getValue(BIT_LENGTheadlineMedium, mMSIRegisterValue, MAX_VALUE);
                            }

                            public static void setL1(int value) {
                                mMSIRegisterValue = Scanner.setValue(BIT_LENGTheadlineMedium, value, mMSIRegisterValue, MAX_VALUE);
                            }

                            public static int getL2() {
                                return Scanner.getValue(BIT_LENGTH2, mMSIRegisterValue, MAX_VALUE);
                            }

                            public static void setL2(int value) {
                                mMSIRegisterValue = Scanner.setValue(BIT_LENGTH2, value, mMSIRegisterValue, MAX_VALUE);
                            }
                        }

                        public static class CheckDigitVerification {
                            static final int BIT_CHECKDIGIT_ALGORITHM = 16;
                            static final int defaultValue = 0 << BIT_CHECKDIGIT_ALGORITHM;
                            static final int MAX_VALUE = 0x3;

                            public static void setOneCheckDigitMOD10() {
                                mMSIRegisterValue = Scanner.setValue(BIT_CHECKDIGIT_ALGORITHM, 0x0, mMSIRegisterValue, MAX_VALUE);
                            }

                            public static void setTwoCheckDigitMOD10MOD10() {
                                mMSIRegisterValue = Scanner.setValue(BIT_CHECKDIGIT_ALGORITHM, 0x1, mMSIRegisterValue, MAX_VALUE);
                            }

                            public static void setTwoCheckDigitMOD10MOD11() {
                                mMSIRegisterValue = Scanner.setValue(BIT_CHECKDIGIT_ALGORITHM, 0x2, mMSIRegisterValue, MAX_VALUE);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_CHECKDIGIT_ALGORITHM, mMSIRegisterValue, MAX_VALUE);
                            }
                        }

                        public static class TransmitCheckDigit {
                            static final int BIT_TRANSMIT_CHECKDIGIT = 18;
                            static final int defaultValue = ENABLE << BIT_TRANSMIT_CHECKDIGIT;

                            public static void disable() {
                                mMSIRegisterValue = Scanner.disable(BIT_TRANSMIT_CHECKDIGIT, mMSIRegisterValue);
                            }

                            public static void enable() {
                                mMSIRegisterValue = Scanner.enable(BIT_TRANSMIT_CHECKDIGIT, mMSIRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_TRANSMIT_CHECKDIGIT, mMSIRegisterValue);
                            }
                        }
                    }

                    public static class Matrix2of5 {
                        //private static int mM2of5RegisterValue = 0x20000;
                        private static int mM2of5RegisterValue = getDefault();

                        private static int getDefault() {
                            return Length.defaultLengtheadlineMedium
                                    | Length.defaultLength2
                                    | CheckDigitVerification.defaultValue
                                    | Redundancy.defaultValue;
                        }

                        public static void setDefault() {
                            mM2of5RegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mM2of5RegisterValue;
                        }

                        public static void setRegister(int value) {
                            mM2of5RegisterValue = value;
                        }

                        public static class Length {
                            static final int BIT_LENGTheadlineMedium = 0;
                            static final int BIT_LENGTH2 = 8;
                            static final int defaultLengtheadlineMedium = 1 << BIT_LENGTheadlineMedium;
                            static final int defaultLength2 = 55 << BIT_LENGTH2;
                            static final int MAX_VALUE = 0xFF;

                            public static int getL1() {
                                return Scanner.getValue(BIT_LENGTheadlineMedium, mM2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setL1(int value) {
                                mM2of5RegisterValue = Scanner.setValue(BIT_LENGTheadlineMedium, value, mM2of5RegisterValue, MAX_VALUE);
                            }

                            public static int getL2() {
                                return Scanner.getValue(BIT_LENGTH2, mM2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setL2(int value) {
                                mM2of5RegisterValue = Scanner.setValue(BIT_LENGTH2, value, mM2of5RegisterValue, MAX_VALUE);
                            }
                        }

                        public static class CheckDigitVerification {
                            static final int BIT_CHECK_DIGIT_VERIFICATION = 16;
                            static final int defaultValue = DISABLE << BIT_CHECK_DIGIT_VERIFICATION;
                            static final int MAX_VALUE = 0x3;

                            public static void disable() {
                                mM2of5RegisterValue = Scanner.setValue(BIT_CHECK_DIGIT_VERIFICATION, 0x0, mM2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setDoNotTransmit() {
                                mM2of5RegisterValue = Scanner.setValue(BIT_CHECK_DIGIT_VERIFICATION, 0x1, mM2of5RegisterValue, MAX_VALUE);
                            }

                            public static void setTransmit() {
                                mM2of5RegisterValue = Scanner.setValue(BIT_CHECK_DIGIT_VERIFICATION, 0x2, mM2of5RegisterValue, MAX_VALUE);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_CHECK_DIGIT_VERIFICATION, mM2of5RegisterValue, MAX_VALUE);
                            }
                        }

                        public static class Redundancy {
                            static final int BIT_REDUNDANCY = 18;
                            static final int defaultValue = DISABLE << BIT_REDUNDANCY;

                            public static void disable() {
                                mM2of5RegisterValue = Scanner.disable(BIT_REDUNDANCY, mM2of5RegisterValue);
                            }

                            public static void enable() {
                                mM2of5RegisterValue = Scanner.enable(BIT_REDUNDANCY, mM2of5RegisterValue);

                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_REDUNDANCY, mM2of5RegisterValue);
                            }
                        }
                    }

                    public static class GS1Databar {
                        //private static int mGs1DatabarRegisterValue = 0x2;
                        private static int mGs1DatabarRegisterValue = getDefault();

                        private static int getDefault() {
                            return SecurityLevel.defaultValue
                                    | ConvertGS1DatabarToUPCEAN.defaultValue;
                        }

                        public static void setDefault() {
                            mGs1DatabarRegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mGs1DatabarRegisterValue;
                        }

                        public static void setRegister(int value) {
                            mGs1DatabarRegisterValue = value;
                        }

                        public static class SecurityLevel {
                            static final int BIT_SECURITY = 0;
                            static final int defaultValue = 2 << BIT_SECURITY;
                            static final int MAX_VALUE = 0x3;

                            public static void level1() {
                                mGs1DatabarRegisterValue = Scanner.setValue(BIT_SECURITY, 0x0, mGs1DatabarRegisterValue, MAX_VALUE);
                            }

                            public static void level2() {
                                mGs1DatabarRegisterValue = Scanner.setValue(BIT_SECURITY, 0x1, mGs1DatabarRegisterValue, MAX_VALUE);
                            }

                            public static void level3() {
                                mGs1DatabarRegisterValue = Scanner.setValue(BIT_SECURITY, 0x2, mGs1DatabarRegisterValue, MAX_VALUE);
                            }

                            public static void level4() {
                                mGs1DatabarRegisterValue = Scanner.setValue(BIT_SECURITY, 0x3, mGs1DatabarRegisterValue, MAX_VALUE);
                            }

                            public static int getLevel() {
                                return Scanner.getValue(BIT_SECURITY, mGs1DatabarRegisterValue, MAX_VALUE);
                            }
                        }

                        public static class ConvertGS1DatabarToUPCEAN {
                            static final int BIT_GS1DATABAR_TO_UPCEAN = 2;
                            static final int defaultValue = DISABLE << BIT_GS1DATABAR_TO_UPCEAN;

                            public static void disable() {
                                mGs1DatabarRegisterValue = Scanner.disable(BIT_GS1DATABAR_TO_UPCEAN, mGs1DatabarRegisterValue);
                            }

                            public static void enable() {
                                mGs1DatabarRegisterValue = Scanner.enable(BIT_GS1DATABAR_TO_UPCEAN, mGs1DatabarRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_GS1DATABAR_TO_UPCEAN, mGs1DatabarRegisterValue);
                            }
                        }
                    }
                }

                public static class TwoD {
                    public static class PostalCodes {
                        //private static int mPotalCodeRegisterValue = 0x3;
                        private static int mPotalCodeRegisterValue = getDefault();

                        private static int getDefault() {
                            return TransmitUKCheckDigit.defaultValue
                                    | TransmitUSCheckDigit.defaultValue
                                    | AustraliaPostFormat.defaultValue;
                        }

                        public static void setDefault() {
                            mPotalCodeRegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mPotalCodeRegisterValue;
                        }

                        public static void setRegister(int value) {
                            mPotalCodeRegisterValue = value;
                        }

                        public static class TransmitUSCheckDigit {
                            static final int BIT_2D_POSTAL_TRANSMIT_US = 0;
                            static final int defaultValue = ENABLE << BIT_2D_POSTAL_TRANSMIT_US;

                            public static void enable() {
                                mPotalCodeRegisterValue = Scanner.enable(BIT_2D_POSTAL_TRANSMIT_US, mPotalCodeRegisterValue);
                            }

                            public static void disable() {
                                mPotalCodeRegisterValue = Scanner.disable(BIT_2D_POSTAL_TRANSMIT_US, mPotalCodeRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_2D_POSTAL_TRANSMIT_US, mPotalCodeRegisterValue);
                            }
                        }

                        public static class TransmitUKCheckDigit {
                            static final int BIT_2D_POSTAL_TRANSMIT_UK = 1;
                            static final int defaultValue = ENABLE << BIT_2D_POSTAL_TRANSMIT_UK;

                            public static void enable() {
                                mPotalCodeRegisterValue = Scanner.enable(BIT_2D_POSTAL_TRANSMIT_UK, mPotalCodeRegisterValue);
                            }

                            public static void disable() {
                                mPotalCodeRegisterValue = Scanner.disable(BIT_2D_POSTAL_TRANSMIT_UK, mPotalCodeRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_2D_POSTAL_TRANSMIT_UK, mPotalCodeRegisterValue);
                            }
                        }

                        public static class AustraliaPostFormat {
                            static final int BIT_2D_POSTAL_FORMAT = 2;
                            static final int defaultValue = 0 << BIT_2D_POSTAL_FORMAT;
                            static final int MAX_VALUE = 0x3;

                            public static void auto() {
                                mPotalCodeRegisterValue = Scanner.setValue(BIT_2D_POSTAL_FORMAT, 0x0, mPotalCodeRegisterValue, MAX_VALUE);
                            }

                            public static void raw() {
                                mPotalCodeRegisterValue = Scanner.setValue(BIT_2D_POSTAL_FORMAT, 0x1, mPotalCodeRegisterValue, MAX_VALUE);
                            }

                            public static void alphanemeric() {
                                mPotalCodeRegisterValue = Scanner.setValue(BIT_2D_POSTAL_FORMAT, 0x2, mPotalCodeRegisterValue, MAX_VALUE);
                            }

                            public static void numeric() {
                                mPotalCodeRegisterValue = Scanner.setValue(BIT_2D_POSTAL_FORMAT, 0x3, mPotalCodeRegisterValue, MAX_VALUE);
                            }

                            public static int getFormat() {
                                return Scanner.getValue(BIT_2D_POSTAL_FORMAT, mPotalCodeRegisterValue, MAX_VALUE);
                            }
                        }
                    }

                    public static class Composite {
                        //private static int mCompositeRegisterValue = 0x1;
                        private static int mCompositeRegisterValue = getDefault();

                        private static int getDefault() {
                            return UPCCompositeMode.defaultValue
                                    | GS1128EmulationMode.defaultValue;
                        }

                        public static void setDefault() {
                            mCompositeRegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mCompositeRegisterValue;
                        }

                        public static void setRegister(int value) {
                            mCompositeRegisterValue = value;
                        }

                        public static class GS1128EmulationMode {
                            static final int BIT_2D_COMPOSITE_GS1_128_EMUL = 2;
                            static final int defaultValue = DISABLE << BIT_2D_COMPOSITE_GS1_128_EMUL;

                            public static void enable() {
                                mCompositeRegisterValue = Scanner.enable(BIT_2D_COMPOSITE_GS1_128_EMUL, mCompositeRegisterValue);
                            }

                            public static void disable() {
                                mCompositeRegisterValue = Scanner.disable(BIT_2D_COMPOSITE_GS1_128_EMUL, mCompositeRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_2D_COMPOSITE_GS1_128_EMUL, mCompositeRegisterValue);
                            }
                        }

                        public static class UPCCompositeMode {
                            static final int BIT_2D_COMPOSITE_UPC_MODE = 0;
                            static final int defaultValue = 1 << BIT_2D_COMPOSITE_UPC_MODE;
                            static final int MAX_VALUE = 0x3;

                            public static void setNeverLinked() {
                                mCompositeRegisterValue = Scanner.setValue(BIT_2D_COMPOSITE_UPC_MODE, 0x0, mCompositeRegisterValue, MAX_VALUE);
                            }

                            public static void setAlwaysLinked() {
                                mCompositeRegisterValue = Scanner.setValue(BIT_2D_COMPOSITE_UPC_MODE, 0x1, mCompositeRegisterValue, MAX_VALUE);
                            }

                            public static void setAutoDiscriminate() {
                                mCompositeRegisterValue = Scanner.setValue(BIT_2D_COMPOSITE_UPC_MODE, 0x2, mCompositeRegisterValue, MAX_VALUE);
                            }

                            public static int getFormat() {
                                return Scanner.getValue(BIT_2D_COMPOSITE_UPC_MODE, mCompositeRegisterValue, MAX_VALUE);
                            }
                        }
                    }

                    public static class MicroPDF417 {
                        //private static int mMPDF417RegisterValue = 0x0;
                        private static int mMPDF417RegisterValue = getDefault();

                        private static int getDefault() {
                            return Code128Emulation.defaultValue;
                        }

                        public static void setDefault() {
                            mMPDF417RegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mMPDF417RegisterValue;
                        }

                        public static void setRegister(int value) {
                            mMPDF417RegisterValue = value;
                        }

                        public static class Code128Emulation {
                            static final int BIT_2D_MPDF417_CODE128_EMUL = 0;
                            static final int defaultValue = DISABLE << BIT_2D_MPDF417_CODE128_EMUL;

                            public static void enable() {
                                mMPDF417RegisterValue = Scanner.enable(BIT_2D_MPDF417_CODE128_EMUL, mMPDF417RegisterValue);
                            }

                            public static void disable() {
                                mMPDF417RegisterValue = Scanner.disable(BIT_2D_MPDF417_CODE128_EMUL, mMPDF417RegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_2D_MPDF417_CODE128_EMUL, mMPDF417RegisterValue);
                            }
                        }
                    }

                    public static class MacroPDF {
                        //private static int mMacroPdfRegisterValue = 0x6;
                        private static int mMacroPdfRegisterValue = getDefault();

                        private static int getDefault() {
                            return TransmitDecodeModeSymbols.defaultValue
                                    | TransmitControlHeader.defaultValue
                                    | EscapeCharacter.defaultValue;
                        }

                        public static void setDefault() {
                            mMacroPdfRegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mMacroPdfRegisterValue;
                        }

                        public static void setRegister(int value) {
                            mMacroPdfRegisterValue = value;
                        }

                        public static class TransmitDecodeModeSymbols {
                            static final int BIT_2D_MACROPDF_MODE_SYMBOL = 0;
                            static final int defaultValue = 2 << BIT_2D_MACROPDF_MODE_SYMBOL;
                            static final int MAX_VALUE = 0x3;

                            public static void setBufferAllSymbols() {
                                mMacroPdfRegisterValue = Scanner.setValue(BIT_2D_MACROPDF_MODE_SYMBOL, 0x0, mMacroPdfRegisterValue, MAX_VALUE);
                            }

                            public static void setAnySymbolNoOrder() {
                                mMacroPdfRegisterValue = Scanner.setValue(BIT_2D_MACROPDF_MODE_SYMBOL, 0x1, mMacroPdfRegisterValue, MAX_VALUE);
                            }

                            public static void setPassthroughAllSymbols() {
                                mMacroPdfRegisterValue = Scanner.setValue(BIT_2D_MACROPDF_MODE_SYMBOL, 0x2, mMacroPdfRegisterValue, MAX_VALUE);
                            }

                            public static int getMode() {
                                return Scanner.getValue(BIT_2D_MACROPDF_MODE_SYMBOL, mMacroPdfRegisterValue, MAX_VALUE);
                            }
                        }

                        public static class TransmitControlHeader {
                            static final int BIT_2D_MACRO417_CONTROL_HEADER = 2;
                            static final int defaultValue = ENABLE << BIT_2D_MACRO417_CONTROL_HEADER;

                            public static void enable() {
                                mMacroPdfRegisterValue = Scanner.enable(BIT_2D_MACRO417_CONTROL_HEADER, mMacroPdfRegisterValue);
                            }

                            public static void disable() {
                                mMacroPdfRegisterValue = Scanner.disable(BIT_2D_MACRO417_CONTROL_HEADER, mMacroPdfRegisterValue);
                            }

                            public static boolean isEnable() {
                                return Scanner.isEnable(BIT_2D_MACRO417_CONTROL_HEADER, mMacroPdfRegisterValue);
                            }
                        }

                        public static class EscapeCharacter {
                            static final int BIT_2D_MACRO417_ESC_CHAR = 3;
                            static final int defaultValue = 0 << BIT_2D_MACRO417_ESC_CHAR;

                            public static void setNone() {
                                mMacroPdfRegisterValue = Scanner.disable(BIT_2D_MACRO417_ESC_CHAR, mMacroPdfRegisterValue);
                            }

                            public static void setGLIProtocol() {
                                mMacroPdfRegisterValue = Scanner.enable(BIT_2D_MACRO417_ESC_CHAR, mMacroPdfRegisterValue);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_2D_MACRO417_ESC_CHAR, mMacroPdfRegisterValue, 0x1);
                            }
                        }
                    }

                    public static class DataMatrix {
                        //private static int mDataMatrixRegisterValue = 0x2;
                        private static int mDataMatrixRegisterValue = getDefault();

                        private static int getDefault() {
                            return DecodeMirrorImages.defaultValue;
                        }

                        public static void setDefault() {
                            mDataMatrixRegisterValue = getDefault();
                        }

                        public static int getRegister() {
                            return mDataMatrixRegisterValue;
                        }

                        public static void setRegister(int value) {
                            mDataMatrixRegisterValue = value;
                        }

                        public static class DecodeMirrorImages {
                            static final int BIT_2D_MATRIX_DECODE_MIRROR_IMG = 0;
                            static final int defaultValue = 2 << BIT_2D_MATRIX_DECODE_MIRROR_IMG;
                            static final int MAX_VALUE = 0x3;

                            public static void setUnMirror() {
                                mDataMatrixRegisterValue = Scanner.setValue(BIT_2D_MATRIX_DECODE_MIRROR_IMG, 0x0, mDataMatrixRegisterValue, MAX_VALUE);
                            }

                            public static void setOnly() {
                                mDataMatrixRegisterValue = Scanner.setValue(BIT_2D_MATRIX_DECODE_MIRROR_IMG, 0x1, mDataMatrixRegisterValue, MAX_VALUE);
                            }

                            public static void setBoth() {
                                mDataMatrixRegisterValue = Scanner.setValue(BIT_2D_MATRIX_DECODE_MIRROR_IMG, 0x2, mDataMatrixRegisterValue, MAX_VALUE);
                            }

                            public static int getValue() {
                                return Scanner.getValue(BIT_2D_MATRIX_DECODE_MIRROR_IMG, mDataMatrixRegisterValue, MAX_VALUE);
                            }
                        }
                    }
                }
            }
        }
    }

    public static class Device {
        public static class Bluetooth {
            private static boolean isConnected = false;
            private static String mCurrentDeviceMacAddress = null;
            private static String mCurrentDeviceName = null;
            private static String mPrevDeviceMacAddress = null;
            private static String mPrevDeviceName = null;

            public static void connect() {
                isConnected = true;
            }

            public static void disconnect() {
                isConnected = false;
            }

            public static boolean isConnect() {
                return isConnected;
            }

            public static void setConnectedDevice(String sMacAddress, String sDeviceName) {
                mCurrentDeviceMacAddress = sMacAddress;
                mCurrentDeviceName = sDeviceName;
            }

            public static void backupConnectedDevice() {
                mPrevDeviceMacAddress = mCurrentDeviceMacAddress;
                mPrevDeviceName = mCurrentDeviceName;
            }

            public static String getConnectedDeviceMacAddress() {
                return mCurrentDeviceMacAddress;
            }

            public static String getConnectedDeviceName() {
                return mCurrentDeviceName;
            }

            public static String getPrevDeviceMacAddress() {
                return mPrevDeviceMacAddress;
            }

            public static String getPrevDeviceName() {
                return mPrevDeviceName;
            }

            public static class Power {
                private static final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                public static boolean isOn() {
                    return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
                }

                public static void setOn() {
                    bluetoothAdapter.enable();
                }

                public static void setOff() {
                    bluetoothAdapter.disable();
                }
            }
        }

        public static class App {
            public static class Mode {
                public static class Tab {
                    public static final int TAB_CONFIG = 0;
                    public static final int TAB_SCAN = 1;
                    public static final int TAB_APP = 2;
                    private static int mTabMode = TAB_CONFIG;

                    public static int getTabMode() {
                        return mTabMode;
                    }

                    public static void setTabMode(int tabMode) {
                        mTabMode = tabMode;
                    }
                }

                public static class ActivityName {
                    private static String activityName = null;

                    public static String getCurrentActivity() {
                        return activityName;
                    }

                    public static void setCurrentActivity(String name) {
                        activityName = name;
                    }
                }

                public static class Scan {
                    public static final int SCAN_DATA_RFID = 0;
                    public static final int SCAN_DATA_BARCODE = 1;
                    private static boolean mIsInventoryRunning = false;
                    private static int mInventoryDataType = SCAN_DATA_RFID;

                    public static void run(int DataType) {
                        mIsInventoryRunning = true;
                        mInventoryDataType = DataType;
                    }

                    public static void stop() {
                        mIsInventoryRunning = false;
                    }

                    public static boolean isRunning() {
                        return mIsInventoryRunning;
                    }

                    public static int getDataType() {
                        return mInventoryDataType;
                    }
                }
            }
        }

        public static class Config {
            public static void setDefault() {
                Buzzer.mBuzzerMode = Buzzer.BUZZER_HIGH;
                PowerOffDelay.mPowerOffDelayTime = 0;
                Sync.mSyncMode = Sync.SYNC_FROM_DEVICE_TO_APP;
            }

            public static class Battery {
                private static int mBatteryLevel = 0;

                public static int getLevel() {
                    return mBatteryLevel;
                }

                public static void setLevel(int Level) {
                    mBatteryLevel = Level;
                }
            }

            public static class Buzzer {
                public static final int BUZZER_HIGH = 2;
                public static final int BUZZER_LOW = 1;
                public static final int BUZZER_MUTE = 0;
                private static int mBuzzerMode = BUZZER_HIGH;

                public static int getMode() {
                    return mBuzzerMode;
                }

                public static void setMode(int Mode) {
                    mBuzzerMode = Mode;
                }
            }

            public static class Vibration {
                private static boolean isVibratorOn = false;

                public static void enable() {
                    isVibratorOn = true;
                }

                public static void disable() {
                    isVibratorOn = false;
                }

                public static boolean isEnable() {
                    return isVibratorOn;
                }
            }

            public static class PowerOffDelay {
                private static int mPowerOffDelayTime = 0;

                public static int getTime() {
                    return mPowerOffDelayTime;
                }

                public static void setTime(int Time) {
                    mPowerOffDelayTime = Time;
                }
            }

            public static class Sync {
                public static final int SYNC_FROM_DEVICE_TO_APP = 0;
                public static final int SYNC_FROM_APP_TO_DEVICE = 1;
                private static int mSyncMode = SYNC_FROM_DEVICE_TO_APP;

                public static int getMode() {
                    return mSyncMode;
                }

                public static void setMode(int Mode) {
                    mSyncMode = Mode;
                }
            }
        }

        public static class Info {
            public static class Firmware {
                private static String firmwareString;

                public static String getFirmwareVersion() {
                    return firmwareString;
                }

                public static void setFirmwareVersion(String firmware) {
                    firmwareString = firmware;
                }
            }
        }
    }

    public static class RFID {
        public static class Config {
            public static void setDefault() {
                TxCycle.mOffTime = 40;
                TxCycle.mOnTime = 160;
                TxCycle.mPercent = 20;
                RadioPower.mMaxRadioPower = 30;
                RadioPower.mRadioPower = 0;
                Queue.mQueue = 5;
                Target.mTargetMode = Target.TARGET_A;
                Session.mSessionMode = Session.SESSION_1;
                FastID.mFastID = false;
                TagFocus.mTagFocus = true;
                Inventory.Mode.mInventoryMode = Inventory.Mode.INVENTORY_CONTINUOUS_MODE;
                Inventory.Timeout.mInventoryTimeOut = Inventory.Timeout.INVENTORY_TIMEOUT_INFINITE;
                LinkProfile.mLinkProfile = LinkProfile.LINK_PROFILE_1;
            }

            public static class Session {
                public static final int SESSION_0 = 0;
                public static final int SESSION_1 = 1;
                public static final int SESSION_2 = 2;
                public static final int SESSION_3 = 3;
                private static int mSessionMode = SESSION_1;

                public static int getSession() {
                    return mSessionMode;
                }

                public static void setSession(int Mode) {
                    mSessionMode = Mode;
                }
            }

            public static class Queue {
                private static int mQueue = 5;

                public static int getValue() {
                    return mQueue;
                }

                public static void setValue(int Value) {
                    mQueue = Value;
                }
            }

            public static class Target {
                public static final int TARGET_A = 0;
                public static final int TARGET_B = 1;
                public static final int TARGET_AB = 2;
                private static int mTargetMode = TARGET_A;

                public static void setTargetA() {
                    mTargetMode = TARGET_A;
                }

                public static void setTargetB() {
                    mTargetMode = TARGET_B;
                }

                public static void setTargetAB() {
                    mTargetMode = TARGET_AB;
                }

                public static int getMode() {
                    return mTargetMode;
                }

                public static void setMode(int Mode) {
                    mTargetMode = Mode;
                }
            }

            public static class RadioPower {
                private static int mRadioPower = 0;
                private static int mMaxRadioPower = 30;

                public static int getMaxPower() {
                    return mMaxRadioPower;
                }

                public static void setMaxPower(int Power) {
                    mMaxRadioPower = Power;
                }

                public static int getAttenuatePower() {
                    return mRadioPower;
                }

                public static void setAttenuatePower(int Power) {
                    mRadioPower = Power;
                }

                public static int getPower() {
                    return mMaxRadioPower + mRadioPower;
                }
            }

            public static class TxCycle {
                private static final int MAX_TIME = 200;
                //!< On TIme : 40ms = 20%, 50ms = 25%, 60ms = 30%, ... 190ms = 95%, 200ms = 100%;
                //!< Off Time : 40ms/160ms, 50ms/150ms ... 190ms/10ms, 200ms/0ms
                //!< ( On TIme / (On Time + Off Time) ) *100 = TxCycle Percent
                private static int mOnTime = 40;
                private static int mOffTime = 160;
                private static int mPercent = 20;

                public static int getOnTime() {
                    return mOnTime;
                }

                public static void setOnTime(int Time) {
                    mOnTime = Time;
                    mOffTime = MAX_TIME - mOnTime;
                    mPercent = (int) (((float) mOnTime / ((float) mOnTime + (float) mOffTime)) * 100);
                }

                public static int getOffTime() {
                    return mOffTime;
                }

                public static int getPercent() {
                    return mPercent;
                }
            }

            public static class Inventory {
                public static class Mode {
                    public static final int INVENTORY_CONTINUOUS_MODE = 0;
                    public static final int INVENTORY_SINGLE_MODE = 1;
                    private static int mInventoryMode = INVENTORY_CONTINUOUS_MODE;

                    public static void setContinuousMode() {
                        mInventoryMode = INVENTORY_CONTINUOUS_MODE;
                    }

                    public static void setSingleMode() {
                        mInventoryMode = INVENTORY_SINGLE_MODE;
                    }

                    public static int getMode() {
                        return mInventoryMode;
                    }

                    public static void setMode(int Mode) {
                        mInventoryMode = Mode;
                    }
                }

                public static class Timeout {
                    public static final int INVENTORY_TIMEOUT_INFINITE = 0;
                    private static int mInventoryTimeOut = INVENTORY_TIMEOUT_INFINITE;

                    public static int getTimeout() {
                        return mInventoryTimeOut;
                    }

                    public static void setTimeout(int Time) {
                        mInventoryTimeOut = Time;
                    }
                }

                public static class Report {
                    public static class Time {
                        private static boolean isTimeReport = false;

                        public static void enable() {
                            isTimeReport = true;
                        }

                        public static void disable() {
                            isTimeReport = false;
                        }

                        public static boolean isEnable() {
                            return isTimeReport;
                        }
                    }

                    public static class RSSI {
                        private static boolean isRssiReport = false;

                        public static void enable() {
                            isRssiReport = true;
                        }

                        public static void disable() {
                            isRssiReport = false;
                        }

                        public static boolean isEnable() {
                            return isRssiReport;
                        }
                    }
                }
            }

            public static class TagFocus {
                private static boolean mTagFocus = true;

                public static void enable() {
                    mTagFocus = true;
                }

                public static void disable() {
                    mTagFocus = false;
                }

                public static boolean isEnable() {
                    return mTagFocus;
                }
            }

            public static class FastID {
                private static boolean mFastID = false;

                public static void enable() {
                    mFastID = true;
                }

                public static void disable() {
                    mFastID = false;
                }

                public static boolean isEnable() {
                    return mFastID;
                }
            }

            public static class LinkProfile {
                public static final int LINK_PROFILE_0 = 0;
                public static final int LINK_PROFILE_1 = 1;
                public static final int LINK_PROFILE_2 = 2;
                public static final int LINK_PROFILE_3 = 3;
                private static int mLinkProfile = LINK_PROFILE_1;

                public static int getProfile() {
                    return mLinkProfile;
                }

                public static void setProfile(int Profile) {
                    mLinkProfile = Profile;
                }
            }

            public static class Format {
                public static void setDefault() {
                    DataFormat.dataFormatType = 0;
                    FixDataFormat.fixDataFormatType = 0;
                    Prefix.prefixData = 0;
                    Suffix1.suffix1Data = 0;
                    Suffix2.suffix2Data = 0;
                }

                public static class DataFormat {
                    public static final int PC_EPC_CRC = 0;
                    public static final int PC_EPC = 1;
                    public static final int EPC_CRC = 2;
                    public static final int EPC_ONLY = 3;
                    private static int dataFormatType = 0;

                    public static int getFormat() {
                        return dataFormatType;
                    }

                    public static void setFormat(int type) {
                        dataFormatType = type;
                    }
                }

                public static class FixDataFormat {
                    public static final int TAG_DATA = 0;
                    public static final int DATA_SUFFIX1 = 1;
                    public static final int DATA_SUFFFIX2 = 2;
                    public static final int DATA_SUFFIX1_SUFFIX2 = 3;
                    public static final int PREFIX_DATA = 4;
                    public static final int PREFIX_DATA_SUFFIX1 = 5;
                    public static final int PREFIX_DATA_SUFFIX2 = 6;
                    public static final int PREFIX_DATA_SUFFIX1_SUFFIX2 = 7;
                    private static int fixDataFormatType = 0;

                    public static int getFormat() {
                        return fixDataFormatType;
                    }

                    public static void setFormat(int type) {
                        fixDataFormatType = type;
                    }
                }

                public static class Prefix {
                    private static int prefixData = 0;

                    public static int getData() {
                        return prefixData;
                    }

                    public static void setData(int asciiData) {
                        prefixData = asciiData;
                    }
                }

                public static class Suffix1 {
                    private static int suffix1Data = 0;

                    public static int getData() {
                        return suffix1Data;
                    }

                    public static void setData(int asciiData) {
                        suffix1Data = asciiData;
                    }
                }

                public static class Suffix2 {
                    private static int suffix2Data = 0;

                    public static int getData() {
                        return suffix2Data;
                    }

                    public static void setData(int asciiData) {
                        suffix2Data = asciiData;
                    }
                }
            }
        }
    }
}
