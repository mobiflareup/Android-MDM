package com.mobiocean.util;

public class Constant {
    public static final String APP_CODE = "MOC0001";
    public static String COUNTRY_CODE;
    public static int COUNTRY_INDEX;
    public static String GCM_SERVER_URL;
    public static final long MIN_LUT[] = {1L << 0, 1L << 1, 1L << 2, 1L << 3, 1L << 4, 1L << 5, 1L << 6, 1L << 7, 1L << 8, 1L << 9,
            1L << 10, 1L << 11, 1L << 12, 1L << 13, 1L << 14, 1L << 15, 1L << 16, 1L << 17, 1L << 18, 1L << 19,
            1L << 20, 1L << 21, 1L << 22, 1L << 23, 1L << 24, 1L << 25, 1L << 26, 1L << 27, 1L << 28, 1L << 29,
            1L << 30, 1L << 31, 1L << 32, 1L << 33, 1L << 34, 1L << 35, 1L << 36, 1L << 37, 1L << 38, 1L << 39,
            1L << 40, 1L << 41, 1L << 42, 1L << 43, 1L << 44, 1L << 45, 1L << 46, 1L << 47, 1L << 48, 1L << 49,
            1L << 50, 1L << 51, 1L << 52, 1L << 53, 1L << 54, 1L << 55, 1L << 56, 1L << 57, 1L << 58, 1L << 59};
    public static final String APPLICATION_NAME = "MobiMDM";

    public static void Init(String Country) {
        COUNTRY_CODE = "+91";
        CallHelper.Ds.structDNCC.stDefunctNumber[0] = "GINGER";
        CallHelper.Ds.structDNCC.stPullMsgNo = "+919212148888";    //918376013432
        GCM_SERVER_URL = "http://mobiocean.com/";
        COUNTRY_INDEX = 1;
    }
}