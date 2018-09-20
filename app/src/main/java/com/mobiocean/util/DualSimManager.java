package com.mobiocean.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

public class DualSimManager {
	 
    private String SIM_VARINT = "";
    private String telephonyClassName = "";
    private SharedPreferences pref;
    private int slotNumber_1 = 0;
    private int slotNumber_2 = 1;
    private String slotName_1 = "null";
    private String slotName_2 = "null";
    private String[] listofClass;
 
    private String IMEI_1,IMSI_1,NETWORK_TYPE_1,NETWORK_OPERATOR_NAME_1,SIM_NETWORK_SIGNAL_STRENGTH_1;
    private String IMEI_2,IMSI_2,NETWORK_TYPE_2,NETWORK_OPERATOR_NAME_2,SIM_NETWORK_SIGNAL_STRENGTH_2;
    private String NETWORK_OPERATOR_CODE_1,isGPRS_1;
    private String NETWORK_OPERATOR_CODE_2,isGPRS_2;
    boolean isRoaming_1,isRoaming_2;
    private int[] CELL_LOC_1;
    private int[] CELL_LOC_2;
 
    final static String m_IMEI = "getDeviceId";
    final static String m_IMSI = "getSubscriberId";
 
    final static String m_NETWORK_OPERATOR = "getNetworkOperatorName";
    final static String m_NETWORK_OPERATOR_CODE = "getNetworkOperator";
 
    final static String m_NETWORK_TYPE_NAME = "getNetworkTypeName";
 
    final static String m_CELL_LOC = "getNeighboringCellInfo";
    final static String m_IS_ROAMING = "isNetworkRoaming";
 
    final static String m_SIM_SERIAL = "getSimSerialNumber";
    final static String m_SIM_OPERATOR = "getSimOperatorName";
    final static String m_DATA_STATE = "getDataNetworkType";
 
    protected static customTelephony customTelephony;
 
    public DualSimManager(Context mContext) {
        if(IMEI_1==null){
            customTelephony = new customTelephony(mContext);
        }else{
            customTelephony.getCurrentData();
        }
    }
 
    public boolean isSIMSupported(){
        boolean isSIMSupported = false;
        if((IMEI_1==null || IMEI_1.equalsIgnoreCase("") || IMEI_1.equalsIgnoreCase("null")) && (IMEI_2==null || IMEI_2.equalsIgnoreCase("") || IMEI_2.equalsIgnoreCase("null"))){
            isSIMSupported = false;
        }else{
            isSIMSupported = true;
        }
        return isSIMSupported;
    }
 
    public boolean isDualSIMSupported(){
        if(IMEI_2!=null || !IMEI_2.equalsIgnoreCase("null") || !IMEI_2.equalsIgnoreCase("")){
            return true;
        }else
            return false;
    }
 
    public boolean isSecondSimActive() {
        if(IMSI_2!=null || !IMSI_2.equalsIgnoreCase("null") || !IMSI_2.equalsIgnoreCase("")){
            return true;
        }else
            return false;
 
    }
 
    public int getSupportedSimCount() {
        if((IMEI_1==null || IMEI_1.equalsIgnoreCase("") || IMEI_1.equalsIgnoreCase("null")) && (IMEI_2==null || IMEI_2.equalsIgnoreCase("") || IMEI_2.equalsIgnoreCase("null"))){
            return 0;
        }else if(IMSI_2!=null || !IMSI_2.equalsIgnoreCase("null") || !IMSI_2.equalsIgnoreCase("")){
            return 2;
        }else{
            return 1;
        }
    }
 
    public String getIMEI(int slotnumber) {
        if(slotnumber==0)
            return IMEI_1;
        else
            return IMEI_2;
    }
 
 
    public String getNETWORK_OPERATOR_NAME(int slotnumber) {
        //return NETWORK_OPERATOR_NAME_1;
        if(slotnumber==0)
        	
            return NETWORK_OPERATOR_NAME_1;
        else
            return NETWORK_OPERATOR_NAME_2;
		
    }
 
    public String getSIM_NETWORK_SIGNAL_STRENGTH(int slotnumber) {
        if(slotnumber==0)
            return SIM_NETWORK_SIGNAL_STRENGTH_1;
        else
            return SIM_NETWORK_SIGNAL_STRENGTH_2;
    }
 
    public boolean isGPRS(int slotnumber){
        boolean isGPRS = false;
        try {
            String GPRS_FLAG = isGPRS_1;
            if(slotnumber == 1){
                GPRS_FLAG = isGPRS_2;
            }
            int gprsInt = Integer.parseInt(GPRS_FLAG);
            if(gprsInt==TelephonyManager.DATA_CONNECTING || gprsInt == TelephonyManager.DATA_CONNECTED){
                isGPRS = true;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return isGPRS;
    }
 
    public void setSIM_NETWORK_SIGNAL_STRENGTH_1(
            String sIM_NETWORK_SIGNAL_STRENGTH_1) {
        SIM_NETWORK_SIGNAL_STRENGTH_1 = sIM_NETWORK_SIGNAL_STRENGTH_1;
    }
 
 
    public int getSIM_CELLID(int slotnumber) {
        if(slotnumber==0)
            return CELL_LOC_1[0];
        else
            return CELL_LOC_2[0];
    }
 
    public int getSIM_LOCID(int slotnumber) {
        if(slotnumber==0)
            return CELL_LOC_1[1];
        else
            return CELL_LOC_2[1];
    }
 
    public String getNETWORK_TYPE(int slotnumber) {
 
        if(slotnumber==0)
            return NETWORK_TYPE_1;
        else
            return NETWORK_TYPE_2;
    }
 
    public int[] getNETWORK_OPERATOR_CODE(int slotnumber) {
        int OperatorCode[] = new int[2];
        String code = NETWORK_OPERATOR_CODE_1;
        if(slotnumber==1){
            code = NETWORK_OPERATOR_CODE_2;
        }
        OperatorCode[0]=-1;
        OperatorCode[1]=-1;
        try{
            if (code != null) {
                OperatorCode[0] = Integer.parseInt(code.substring(0, 3));
                OperatorCode[1] = Integer.parseInt(code.substring(3));
            }
        }catch(Exception e){}
        return OperatorCode;
    }
 
    public boolean isRoaming(int slotnumber) {
        if(slotnumber==0){
            return isRoaming_1;
        }else{
            return isRoaming_2;
        }
    }
 
    class customTelephony{
        Context mContext;
        TelephonyManager telephony;
        public customTelephony(Context mContext) {
            try {
                this.mContext = mContext;
                telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                telephonyClassName = pref.getString("dualsim_telephonycls", "");
                SIM_VARINT = pref.getString("SIM_VARINT", "");
                slotName_1 = pref.getString("SIM_SLOT_NAME_1", "");
                slotName_2 = pref.getString("SIM_SLOT_NAME_2", "");
                slotNumber_1 = pref.getInt("SIM_SLOT_NUMBER_1", 0);
                slotNumber_2 = pref.getInt("SIM_SLOT_NUMBER_2", 1);
                telephonyClassName = pref.getString("dualsim_telephonycls", "");
                /*getCellLocation(1);*/
                if (telephonyClassName.equalsIgnoreCase("")) {
                    fetchClassInfo();
                } else if (!isValidMethod(telephonyClassName)) {
                    fetchClassInfo();
                }
                gettingAllMethodValues();
 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
 
        /**
         * This method returns the class name in which we fetch dual sim details
         * */
        public void fetchClassInfo() {
            try {
                telephonyClassName = "android.telephony.TelephonyManager";
                listofClass = new String[] {
                        "com.mediatek.telephony.TelephonyManagerEx",
                        "android.telephony.TelephonyManager",
                        "android.telephony.MSimTelephonyManager",
                        "android.telephony.TelephonyManager" };
                for (int index = 0; index < listofClass.length; index++) {
                    if (isTelephonyClassExists(listofClass[index])) {
                        if (isMethodExists(listofClass[index], "getDeviceId")) {
                            System.out.println("getDeviceId method found");
                            if (!SIM_VARINT.equalsIgnoreCase("")) {
                                break;
                            }
                        }
                        if (isMethodExists(listofClass[index],
                                "getNetworkOperatorName")) {
                            System.out
                                    .println("getNetworkOperatorName method found");
                            break;
                        } else if (isMethodExists(listofClass[index],
                                "getSimOperatorName")) {
                            System.out.println("getSimOperatorName method found");
                            break;
                        }
                    }
                }
                for (int index = 0; index < listofClass.length; index++) {
                    if (slotName_1 == null || slotName_1.equalsIgnoreCase("")) {
                        getValidSlotFields(listofClass[index]);
                        // if(slotName_1!=null || !slotName_1.equalsIgnoreCase("")){
                        getSlotNumber(listofClass[index]);
                    } else {
                        break;
                    }
                }
 
                Editor edit = pref.edit();
                edit.putString("dualsim_telephonycls", telephonyClassName);
                edit.putString("SIM_VARINT", SIM_VARINT);
                edit.putString("SIM_SLOT_NAME_1", slotName_1);
                edit.putString("SIM_SLOT_NAME_2", slotName_2);
                edit.putInt("SIM_SLOT_NUMBER_1", slotNumber_1);
                edit.putInt("SIM_SLOT_NUMBER_2", slotNumber_2);
                edit.commit();
                System.out.println("Done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
 
        /**
         * Check Method is found in class
         * */
        public boolean isValidMethod(String className) {
            boolean isValidMail = false;
            try {
                if (isMethodExists(className, "getDeviceId")) {
                    isValidMail = true;
                } else if (isMethodExists(className, "getNetworkOperatorName")) {
                    isValidMail = true;
                } else if (isMethodExists(className, "getSimOperatorName")) {
                    isValidMail = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isValidMail;
        }
 
        /**
         * Check method with sim variant
         * */
        public boolean isMethodExists(String className, String compairMethod) {
            boolean isExists = false;
            try {
                Class<?> telephonyClass = Class.forName(className);
                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;
                StringBuffer sbf = new StringBuffer();
                Method[] methodList = telephonyClass.getDeclaredMethods();
                for (int index = methodList.length - 1; index >= 0; index--) {
                    sbf.append("\n\n" + methodList[index].getName());
                    if (methodList[index].getReturnType().equals(String.class)) {
                        String methodName = methodList[index].getName();
                        if (methodName.contains(compairMethod)) {
                            Class<?>[] param = methodList[index]
                                    .getParameterTypes();
                            if (param.length > 0) {
                                if (param[0].equals(int.class)) {
                                    try {
                                        SIM_VARINT = methodName.substring(
                                                compairMethod.length(),
                                                methodName.length());
                                        telephonyClassName = className;
                                        isExists = true;
                                        break;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    telephonyClassName = className;
                                    isExists = true;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isExists;
        }
 
 
        public void gettingAllMethodValues() {
            try {
                IMEI_1 = invokeMethod(telephonyClassName, slotNumber_1,m_IMEI, SIM_VARINT);
                if(IMEI_1 ==null || IMEI_1.equalsIgnoreCase("")){
                    IMEI_1  = telephony.getDeviceId();
                }
                IMEI_2 = invokeMethod(telephonyClassName, slotNumber_2,m_IMEI, SIM_VARINT);
 
                IMSI_1 = invokeMethod(telephonyClassName, slotNumber_1,m_IMSI, SIM_VARINT);
                if(IMSI_1 ==null || IMSI_1.equalsIgnoreCase("")){
                    IMSI_1  = telephony.getSimSerialNumber();
                }
                IMSI_2 = invokeMethod(telephonyClassName, slotNumber_2,m_IMSI, SIM_VARINT);
 
                NETWORK_OPERATOR_NAME_1 = invokeMethod(telephonyClassName,slotNumber_1, m_SIM_OPERATOR, SIM_VARINT);
                if(NETWORK_OPERATOR_NAME_1 ==null || NETWORK_OPERATOR_NAME_1.equalsIgnoreCase("") || NETWORK_OPERATOR_NAME_1.equalsIgnoreCase("UNKNOWN")){
                    NETWORK_OPERATOR_NAME_1  = telephony.getSimOperatorName();
                }
                NETWORK_OPERATOR_NAME_2 = invokeMethod(telephonyClassName,slotNumber_2, m_SIM_OPERATOR, SIM_VARINT);
                if (NETWORK_OPERATOR_NAME_1.equalsIgnoreCase(""))
                    NETWORK_OPERATOR_NAME_1 = invokeMethod(telephonyClassName,
                            slotNumber_1, m_NETWORK_OPERATOR, SIM_VARINT);
                if (NETWORK_OPERATOR_NAME_2.equalsIgnoreCase(""))
                    NETWORK_OPERATOR_NAME_2= invokeMethod(telephonyClassName,
                            slotNumber_2, m_NETWORK_OPERATOR, SIM_VARINT);
 
                NETWORK_OPERATOR_CODE_1= invokeMethod(telephonyClassName,slotNumber_1, m_NETWORK_OPERATOR_CODE, SIM_VARINT);
                if(NETWORK_OPERATOR_CODE_1 ==null || NETWORK_OPERATOR_CODE_1.equalsIgnoreCase("") || NETWORK_OPERATOR_CODE_1.equalsIgnoreCase("UNKNOWN")){
                    NETWORK_OPERATOR_CODE_1  = telephony.getSimOperator();
                }
                NETWORK_OPERATOR_CODE_2 = invokeMethod(telephonyClassName,
                        slotNumber_2, m_NETWORK_OPERATOR_CODE, SIM_VARINT);
                getCurrentData();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
 
        private void getCurrentData(){
            try {
 
                NETWORK_TYPE_1 = getNetworkType(0);
                NETWORK_TYPE_2 = getNetworkType(1);
                if(NETWORK_TYPE_1 ==null || NETWORK_TYPE_1.equalsIgnoreCase("") || NETWORK_TYPE_1.equalsIgnoreCase("UNKNOWN")){
                  //  NETWORK_TYPE_1  = Utility.getNetworkTypeName(telephony.getNetworkType());
                }
                isRoaming_1 = invokeMethodboolean(telephonyClassName, slotNumber_1,m_IS_ROAMING, SIM_VARINT);
                isRoaming_2 = invokeMethodboolean(telephonyClassName, slotNumber_2,m_IS_ROAMING, SIM_VARINT);
 
                isGPRS_1 = invokeMethod(telephonyClassName,slotNumber_1, m_DATA_STATE, SIM_VARINT);
                isGPRS_2 = invokeMethod(telephonyClassName,slotNumber_2, m_DATA_STATE, SIM_VARINT);
 
                CELL_LOC_1 = getCellLocation(slotNumber_1);
                CELL_LOC_2 = getCellLocation(slotNumber_2);
 
 
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
 
        public String getNetworkType(int slotnumber) {
            String networkType = "UNKNOWN";
            try {
                if(slotnumber==0){
                    networkType = invokeMethod(telephonyClassName,slotNumber_1, m_NETWORK_TYPE_NAME, SIM_VARINT);
                }else{
                    networkType = invokeMethod(telephonyClassName,slotNumber_2, m_NETWORK_TYPE_NAME, SIM_VARINT);
                }
                if(networkType.equalsIgnoreCase("")){
                    try {
                        networkType = getDeviceIdBySlot("getNetworkType", slotnumber);
                    } catch (Exception e) {}
                    if(networkType.equalsIgnoreCase("")){
                        try {
                            networkType = getDeviceIdBySlotOld("getNetworkTypeGemini", slotnumber);
                        } catch (Exception e) {}
                    }
                }
               /* ConnectivityInfo connInfo = new ConnectivityInfo(mContext);
                networkType = connInfo.getNetworkTypeName(Integer.parseInt(networkType));
                if (slotnumber == 0 && !Utility.isEmpty(networkType)
                		) {
                    networkType = connInfo.getNetworkTypeName(telephony.getNetworkType());
                }*/
            } catch (Exception e) {
                //e.printStackTrace();
            }
            return networkType;
        }
 
        public int[] getCellLocation(int slot) {
            int[] cellLoc = new int[] { -1, -1 };
            try {
                if (slot == 0) {
                    if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                        GsmCellLocation location = (GsmCellLocation) telephony
                                .getCellLocation();
                        if (location == null) {
                            location = (GsmCellLocation) telephony
                                    .getCellLocation();
                        }
                        if (location != null) {
                            cellLoc[0] = location.getCid();
                            cellLoc[1] = location.getLac();
                        }
                    }
                } else {
                    Object cellInfo = (Object) getObjectBySlot("getNeighboringCellInfo"+SIM_VARINT, slot);
                    if (cellInfo != null) {
                        List<NeighboringCellInfo> info = (List<NeighboringCellInfo>) cellInfo;
                        cellLoc[0] = info.get(0).getCid();
                        cellLoc[1] = info.get(0).getLac();
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
            return cellLoc;
        }
 
        private Object getObjectBySlot(String predictedMethodName, int slotID) {
            Object ob_phone = null;
            try {
                Class<?> telephonyClass = Class.forName(telephonyClassName);
                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;
                Method getSimID = telephonyClass.getMethod(predictedMethodName,parameter);
                Object[] obParameter = new Object[1];
                obParameter[0] = slotID;
                ob_phone = getSimID.invoke(telephony, obParameter);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            return ob_phone;
        }
 
        private boolean invokeMethodboolean(String className,
                int slotNumber, String methodName, String SIM_variant) {
            boolean val = false;
            try {
                Class<?> telephonyClass = Class.forName(telephonyClassName);
                Constructor[] cons = telephonyClass.getDeclaredConstructors();
                cons[0].getName();
                cons[0].setAccessible(true);
                Object obj = cons[0].newInstance();
                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;
                Object ob_phone = null;
                try {
                    Method getSimID = telephonyClass.getMethod(methodName
                            + SIM_variant, parameter);
                    Object[] obParameter = new Object[1];
                    obParameter[0] = slotNumber;
                    ob_phone = getSimID.invoke(obj, obParameter);
                } catch (Exception e) {
                    if (slotNumber == 0) {
                        Method getSimID = telephonyClass.getMethod(methodName
                                + SIM_variant, parameter);
                        Object[] obParameter = new Object[1];
                        obParameter[0] = slotNumber;
                        ob_phone = getSimID.invoke(obj);
                    }
                    e.printStackTrace();
                }
 
                if (ob_phone != null) {
                    val = (boolean)Boolean.parseBoolean(ob_phone.toString());
                }
            } catch (Exception e) {
                invokeOldMethod(className, slotNumber, methodName, SIM_variant);
            }
 
            return val;
        }
 
        public boolean isTelephonyClassExists(String className) {
 
            boolean isClassExists = false;
            try {
                Class<?> telephonyClass = Class.forName(className);
                isClassExists = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isClassExists;
        }
        /**
         * Here we are identify sim slot number
         * */
        public void getValidSlotFields(String className) {
 
            String value = null;
            try {
                Class<?> telephonyClass = Class.forName(className);
                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;
                StringBuffer sbf = new StringBuffer();
                Field[] fieldList = telephonyClass.getDeclaredFields();
                for (int index = 0; index < fieldList.length; index++) {
                    sbf.append("\n\n" + fieldList[index].getName());
                    Class<?> type = fieldList[index].getType();
                    Class<?> type1 = int.class;
                    if (type.equals(type1)) {
                        String variableName = fieldList[index].getName();
                        if (variableName.contains("SLOT")
                                || variableName.contains("slot")) {
                            if (variableName.contains("1")) {
                                slotName_1 = variableName;
                            } else if (variableName.contains("2")) {
                                slotName_2 = variableName;
                            } else if (variableName.contains("" + slotNumber_1)) {
                                slotName_1 = variableName;
                            } else if (variableName.contains("" + slotNumber_2)) {
                                slotName_2 = variableName;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
 
        /**
         * Some device assign different slot number so here code execute
         * to get slot number
         * */
        public void getSlotNumber(String className) {
            try {
                Class<?> c = Class.forName(className);
                Field fields1 = c.getField(slotName_1);
                fields1.setAccessible(true);
                slotNumber_1 = (Integer) fields1.get(null);
                Field fields2 = c.getField(slotName_2);
                fields2.setAccessible(true);
                slotNumber_2 = (Integer) fields2.get(null);
            } catch (Exception e) {
                slotNumber_1 = 0;
                slotNumber_2 = 1;
                // e.printStackTrace();
            }
        }
 
        private String invokeMethod(String className, int slotNumber,
                String methodName, String SIM_variant) {
            String value = "";
 
            try {
                Class<?> telephonyClass = Class.forName(className);
                Constructor[] cons = telephonyClass.getDeclaredConstructors();
                cons[0].getName();
                cons[0].setAccessible(true);
                Object obj = cons[0].newInstance();
                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;
                Object ob_phone = null;
                try {
                    Method getSimID = telephonyClass.getMethod(methodName
                            + SIM_variant, parameter);
                    Object[] obParameter = new Object[1];
                    obParameter[0] = slotNumber;
                    ob_phone = getSimID.invoke(obj, obParameter);
                } catch (Exception e) {
                    if (slotNumber == 0) {
                        Method getSimID = telephonyClass.getMethod(methodName
                                + SIM_variant, parameter);
                        Object[] obParameter = new Object[1];
                        obParameter[0] = slotNumber;
                        ob_phone = getSimID.invoke(obj);
                    }
                }
 
                if (ob_phone != null) {
                    value = ob_phone.toString();
                }
            } catch (Exception e) {
                invokeOldMethod(className, slotNumber, methodName, SIM_variant);
            }
 
            return value;
        }
 
        public String invokeOldMethod(String className, int slotNumber,
                String methodName, String SIM_variant) {
            String val = "";
            try {
                Class<?> telephonyClass = Class
                        .forName("android.telephony.TelephonyManager");
                Constructor[] cons = telephonyClass.getDeclaredConstructors();
                cons[0].getName();
                cons[0].setAccessible(true);
                Object obj = cons[0].newInstance();
                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;
                Object ob_phone = null;
                try {
                    Method getSimID = telephonyClass.getMethod(methodName
                            + SIM_variant, parameter);
                    Object[] obParameter = new Object[1];
                    obParameter[0] = slotNumber;
                    ob_phone = getSimID.invoke(obj, obParameter);
                } catch (Exception e) {
                    if (slotNumber == 0) {
                        Method getSimID = telephonyClass.getMethod(methodName
                                + SIM_variant, parameter);
                        Object[] obParameter = new Object[1];
                        obParameter[0] = slotNumber;
                        ob_phone = getSimID.invoke(obj);
                    }
                }
 
                if (ob_phone != null) {
                    val = ob_phone.toString();
                }
            } catch (Exception e) {
 
            }
            return val;
        }
 
        public String getDeviceIdBySlot(String predictedMethodName, int slotID) {
 
            String imei = null;
            try {
                Class<?> telephonyClass = Class.forName(telephonyClassName);
                Constructor[] cons = telephonyClass.getDeclaredConstructors();
                cons[0].getName();
                cons[0].setAccessible(true);
                Object obj = cons[0].newInstance();
                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;
                Method getSimID = telephonyClass.getMethod(predictedMethodName,
                        parameter);
                Object[] obParameter = new Object[1];
                obParameter[0] = slotID;
                Object ob_phone = getSimID.invoke(obj, obParameter);
 
                if (ob_phone != null) {
                    imei = ob_phone.toString();
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
 
            return imei;
        }
        public String getDeviceIdBySlotOld(String predictedMethodName, int slotID) {
 
            String value = null;
            try {
                Class<?> telephonyClass = Class.forName(telephony.getClass()
                        .getName());
 
                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;
                Method getSimID = telephonyClass.getMethod(predictedMethodName,
                        parameter);
 
                Object[] obParameter = new Object[1];
                obParameter[0] = slotID;
                Object ob_phone = getSimID.invoke(telephony, obParameter);
 
                if (ob_phone != null) {
                    value = ob_phone.toString();
 
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
 
            return value;
        }
    }
 
 
}