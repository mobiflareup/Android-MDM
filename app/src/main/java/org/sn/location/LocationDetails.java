package org.sn.location;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mobiocean.beans.SendAttendanceBean;
import com.mobiocean.database.DatabaseHabdler_SMSSent;
import com.mobiocean.service.UploadService;
import com.mobiocean.ui.AttendanceActivity;
import com.mobiocean.ui.Conveyance;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.Struct_Send_SMSInfo;

import org.conveyance.configuration.RSharedData;
import org.conveyance.database.RStartStopLocation;
import org.conveyance.model.RControlModel;
import org.conveyance.services.RUploadDetailsService;
import org.sn.beans.ConveyanceBean;
import org.sn.database.ConveyanceLocationTable;
import org.sn.services.UpdateConveyanceService;
import org.sn.util.Constants;
import org.sn.util.ServiceCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * @author Narayanan S
 */
public class LocationDetails implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    //region VARIABLES
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 1;
    private Context context;
    private static Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private final static int TIME_INTERVAL = 1;
    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    public SharedPreferences.Editor editor;
    private RSharedData rSharedData;
    private RStartStopLocation locationTableAccess;
    private boolean isSOS = false;
    private boolean isOneMinuteTimer = false;
    private boolean isTravelAllowance = false;
    private boolean isConveyance = false;
    private boolean isAttendanceActivity = false;
    private boolean isConveyanceActivity = false;
    private int cAllowanceType = 0;
    private int attendanceType = 0;
    private int conveyanceType = 0;
    private DatabaseHabdler_SMSSent SMS_db;
    private ConveyanceLocationTable conveyanceLocationTable;
    //endregion

    //region Constructor
    public LocationDetails(Context context) {
        this.context = context;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        rSharedData = new RSharedData(context);
        locationTableAccess = new RStartStopLocation(context);
        SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
        conveyanceLocationTable = new ConveyanceLocationTable(context);
    }
    //endregion

    //region Function to check the presence of GPS and status of GPS
    public boolean hasGPSDevice() {
        try {
            final LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (mgr == null) return false;
            final List<String> providers = mgr.getAllProviders();
            if (providers == null) return false;
            return (providers.contains(LocationManager.GPS_PROVIDER) || providers.contains(LocationManager.NETWORK_PROVIDER)) && mgr.isProviderEnabled(LocationManager.GPS_PROVIDER) && mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    //endregion

    //region Function to get TowerLocation
    private TelephonyLocationBean getCellInfo() {
        TelephonyLocationBean tlb = new TelephonyLocationBean();
        try {
            TelephonyManager mtelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            GsmCellLocation GCL = (GsmCellLocation) mtelephonyManager.getCellLocation();
            if (GCL != null) {
                String networkOperator = mtelephonyManager.getNetworkOperator();
                if (!(networkOperator == null || networkOperator.isEmpty() || networkOperator.length() < 4)) {
                    String mobileCountryCode = "0";
                    String mobileNetworkCode = "0";
                    try {
                        mobileCountryCode = networkOperator.substring(0, 3);
                        mobileNetworkCode = networkOperator.substring(3);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }

                    tlb.CellId = "" + GCL.getCid();
                    tlb.LAC = "" + GCL.getLac();
                    tlb.MCC = mobileCountryCode;
                    tlb.MNC = mobileNetworkCode;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tlb;
    }
    //endregion

    //region Function to be used by travel allowance to getLocation
    public void travelAllowance() {
        if (hasGPSDevice()) {
            isTravelAllowance = true;
            setLocationUpdate();
        } else {
            RControlModel controlModel = new RControlModel();
            TelephonyLocationBean tlb = getCellInfo();
            controlModel.setAppId(rSharedData.getAppId());
            controlModel.setLatitude("0");
            controlModel.setLongitude("0");
            controlModel.setAccuracy("0");
            controlModel.setAltitude("0");
            controlModel.setBearing("0");
            controlModel.setElapsedRealtimeNanos("0");
            controlModel.setProvider("Tower");
            controlModel.setSpeed("0");
            controlModel.setTime("0");
            controlModel.setLogDateTime(CallHelper.GetTimeWithDate());
            controlModel.setIsLogin("2");
            controlModel.setMCC(tlb.MCC);
            controlModel.setLAC(tlb.LAC);
            controlModel.setMNC(tlb.MNC);
            controlModel.setCellId(tlb.CellId);
            controlModel.setCustomerId("0");
            controlModel.setModeOfTravel("0");
            controlModel.setVisitId("0");
            if (rSharedData.getStatus()) {
                locationTableAccess.insertLocation(controlModel);
                if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                    context.startService(new Intent(context, RUploadDetailsService.class));
                }
            }
        }
    }
    //endregion

    //region Function to be used by SOS to getLocation
    public void sos() {
        if (hasGPSDevice()) {
            isSOS = true;
            setLocationUpdate();
        } else {
            TelephonyLocationBean tlb = getCellInfo();
            String TimeStamp = CallHelper.GetTimeWithDate();
            if (SMS_db.recordExist(TimeStamp, 1) == 0) {
                SMS_db.addSMS(new Struct_Send_SMSInfo(2, CallHelper.GetTimeWithDate(), "0", "Google", 0.0, 0.0, 10, System.currentTimeMillis(), tlb.CellId, tlb.LAC, tlb.MCC, tlb.MNC));
                if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                    Intent msgIntent = new Intent(context, UploadService.class);
                    Bundle b = new Bundle();
                    msgIntent.putExtra("UploadStatus", 0);
                    msgIntent.putExtras(b);
                    context.startService(msgIntent);
                }
            }
        }
    }
    //endregion

    //region Function to be used by OneMinuteTimer to sendLocation
    public void timerLocation() {
        if (hasGPSDevice()) {
            isOneMinuteTimer = true;
            setLocationUpdate();
        } else {
            TelephonyLocationBean tlb = getCellInfo();
            String type = "GPS";
            int loc_count = settings.getInt("SyncIntentService.loc_count", 0);
            type += "-" + loc_count;
            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                SMS_db.addSMS(new Struct_Send_SMSInfo(2, CallHelper.GetTimeWithDate(), "0", type, 0.0, 0.0, 0, System.currentTimeMillis(), tlb.CellId, tlb.LAC, tlb.MCC, tlb.MNC));
                Intent msgIntent = new Intent(context, UploadService.class);
                Bundle b = new Bundle();
                msgIntent.putExtra("UploadStatus", 0);
                msgIntent.putExtras(b);
                context.startService(msgIntent);
            }
            editor.putInt("SyncIntentService.loc_count", ++loc_count);
            editor.commit();
        }
    }
    //endregion

    //region Function to be used by Conveyance to getLocation
    public void conveyance(int type) {
        if (hasGPSDevice()) {
            isConveyance = true;
            this.cAllowanceType = type;
            setLocationUpdate();
        } else {
            TelephonyLocationBean tlb = getCellInfo();
            ConveyanceBean conveyanceBean = new ConveyanceBean();
            conveyanceBean.IsLogin = String.valueOf(type);
            conveyanceBean.LogDateTime = CallHelper.GetTimeWithDate();
            conveyanceBean.Latitude = "0.0";
            conveyanceBean.Longitude = "0.0";
            conveyanceBean.CellId = tlb.CellId;
            conveyanceBean.LAC = tlb.LAC;
            conveyanceBean.MNC = tlb.MNC;
            conveyanceBean.MCC = tlb.MCC;
            conveyanceLocationTable.insertLocation(conveyanceBean);
            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                context.startService(new Intent(context, UpdateConveyanceService.class));
            }
        }
    }
    //endregion

    //region Function to be used by attendance to getLocation
    public void attendanceActivity(int type) {
        if (hasGPSDevice()) {
            isAttendanceActivity = true;
            this.attendanceType = type;
            setLocationUpdate();
        } else {
            TelephonyLocationBean tlb = getCellInfo();
            SendAttendanceBean attendanceBean = new SendAttendanceBean(context, type);
            attendanceBean.LogDateTime = CallHelper.GetTimeWithDate();
            attendanceBean.Latitude = "0.0";
            attendanceBean.Longitude = "0.0";
            attendanceBean.CellId = tlb.CellId;
            attendanceBean.LAC = tlb.LAC;
            attendanceBean.MNC = tlb.MNC;
            attendanceBean.MCC = tlb.MCC;
            try {
                if (Constants.serviceResponse != null) {
                    Object obj = Constants.serviceResponse.get(ServiceCallback.ATTENDANCE_ACTIVITY);
                    ((AttendanceActivity) obj).callService(attendanceBean);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //endregion

    //region Function to be used by conveyance to start and stop
    public void conveyanceActivity(int type) {
        if (hasGPSDevice()) {
            isConveyanceActivity = true;
            this.conveyanceType = type;
            setLocationUpdate();
        } else {
            TelephonyLocationBean tlb = getCellInfo();
            ConveyanceBean conveyanceBean = new ConveyanceBean(type);
            conveyanceBean.LogDateTime = CallHelper.GetTimeWithDate();
            conveyanceBean.Latitude = "0.0";
            conveyanceBean.Longitude = "0.0";
            conveyanceBean.CellId = tlb.CellId;
            conveyanceBean.LAC = tlb.LAC;
            conveyanceBean.MNC = tlb.MNC;
            conveyanceBean.MCC = tlb.MCC;
            try {
                if (Constants.serviceResponse != null) {
                    Object obj = Constants.serviceResponse.get(ServiceCallback.CONVEYANCE_ACTIVITY);
                    ((Conveyance) obj).startStopConveyance(conveyanceBean);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //endregion

    //region Function to enable Location update if GPS is turned on
    private void setLocationUpdate() {
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            lm.removeUpdates(this);
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled || isNetworkEnabled) {
                if (isNetworkEnabled) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                }
                if (isGPSEnabled) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                }
            }
        } catch (SecurityException e) {
            DeBug.ShowLogD("LSNS", "SetLocationUpdateError SecurityException-> " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            DeBug.ShowLogD("LSNS", "SetLocationUpdateError Exception-> " + e.getMessage());
            e.printStackTrace();
        }
    }
    //endregion

    //region Function to get Tower or GPS Location accordingly, the location produced may be a old one and tower is new
    public LocationBean getLocation() {
        LocationBean lb = new LocationBean();
        try {
            if (hasGPSDevice()) {
                Location loc = getGpsDetails();
                if (loc != null) {
                    lb.IsGPS = true;
                    lb.Lat = Double.toString(loc.getLatitude());
                    lb.Longt = Double.toString(loc.getLongitude());
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
                        lb.ElapsedRealTimeNanos = String.valueOf(loc.getElapsedRealtimeNanos() / 1000000);
                    lb.Time = String.valueOf(loc.getTime());
                    lb.Provider = loc.getProvider();
                    if (loc.hasAltitude())
                        lb.Altitude = String.valueOf(loc.getAltitude());
                    if (loc.hasAccuracy())
                        lb.Accuracy = String.valueOf(loc.getAccuracy());
                    if (loc.hasBearing())
                        lb.Bearing = String.valueOf(loc.getBearing());
                    if (loc.hasSpeed())
                        lb.Speed = String.valueOf(loc.getSpeed());
                } else {
                    lb.IsGPS = false;
                }
            }
            if (!lb.IsGPS) {
                TelephonyLocationBean tlb = getCellInfo();
                lb.CellId = tlb.CellId;
                lb.LAC = tlb.LAC;
                lb.MCC = tlb.MCC;
                lb.MNC = tlb.MNC;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lb;
    }
    //endregion

    //region Function to get the GPS Location, the location produced may be a old one
    private Location getGpsDetails() {
        try {
            Location location = null, gpsLocation = null, netLocation = null;
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            lm.removeUpdates(this);
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled || isNetworkEnabled) {
                if (isNetworkEnabled) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    netLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (isGPSEnabled) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
            if (gpsLocation != null && System.currentTimeMillis() - gpsLocation.getTime() < 60000) {
                location = gpsLocation;
            } else if (netLocation != null && System.currentTimeMillis() - netLocation.getTime() < 60000) {
                location = netLocation;
            } else if (mLastLocation != null && System.currentTimeMillis() - mLastLocation.getTime() < 60000) {
                location = mLastLocation;
            } else {
                location = null;
            }
            return location;
        } catch (SecurityException e) {
            DeBug.ShowLogD("LSNS", "GetLocation SecurityException-> " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            DeBug.ShowLogD("LSNS", "GetLocation Exception-> " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    //endregion

    //region Listener invoked after fetching new location
    @Override
    public void onLocationChanged(Location location) {
        DeBug.ShowLog("LSNS", "onLocationChanged : " + location.getLatitude() + "," + location.getLongitude() + ", " + context.getPackageName());
        try {
            if (context != null) {
                try {

                    String TimeStamp = dateTime(location.getTime());
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    String provider = location.getProvider();

                    //region Travel Allowance Location Update
                    if (isTravelAllowance && rSharedData.getStatus()) {
                        isTravelAllowance = false;
                        RControlModel controlModel = new RControlModel();
                        controlModel.setAppId(rSharedData.getAppId());
                        controlModel.setLatitude("" + lat);
                        controlModel.setLongitude("" + lon);
                        controlModel.setTime("" + location.getTime());
                        if (location.hasAccuracy())
                            controlModel.setAccuracy("" + location.getAccuracy());
                        else
                            controlModel.setAccuracy("0");
                        if (location.hasAltitude())
                            controlModel.setAltitude("" + location.getAltitude());
                        else
                            controlModel.setAltitude("0");
                        if (location.hasBearing())
                            controlModel.setBearing("" + location.getBearing());
                        else
                            controlModel.setBearing("0");
                        if (location.hasSpeed())
                            controlModel.setSpeed("" + location.getSpeed());
                        else
                            controlModel.setSpeed("0");
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
                            controlModel.setElapsedRealtimeNanos("" + location.getElapsedRealtimeNanos());
                        else
                            controlModel.setElapsedRealtimeNanos("API16");
                        controlModel.setProvider(provider);
                        controlModel.setLogDateTime(TimeStamp);
                        controlModel.setIsLogin("2");
                        controlModel.setMCC("0");
                        controlModel.setLAC("0");
                        controlModel.setMNC("0");
                        controlModel.setCellId("0");
                        controlModel.setCustomerId("0");
                        controlModel.setModeOfTravel("0");
                        controlModel.setVisitId("0");
                        locationTableAccess.insertLocation(controlModel);
                        if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                            context.startService(new Intent(context, RUploadDetailsService.class));
                        }
                    }
                    //endregion

                    //region SOS Location Update
                    if (isSOS) {
                        isSOS = false;
                        SMS_db.addSMS(new Struct_Send_SMSInfo(2, TimeStamp, "0", provider, lat, lon, 10, System.currentTimeMillis(), "0", "0", "0", "0"));
                        if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                            Intent msgIntent = new Intent(context, UploadService.class);
                            Bundle b = new Bundle();
                            msgIntent.putExtra("UploadStatus", 0);
                            msgIntent.putExtras(b);
                            context.startService(msgIntent);
                        }
                    }
                    //endregion

                    //region Location Management Location Update
                    if (isOneMinuteTimer) {
                        isOneMinuteTimer = false;
                        int loc_count = settings.getInt("SyncIntentService.loc_count", 0);
                        String type = provider + "-" + loc_count;
                        SMS_db.addSMS(new Struct_Send_SMSInfo(2, TimeStamp, "0", type, lat, lon, 0, System.currentTimeMillis(), "0", "0", "0", "0"));
                        if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                            Intent msgIntent = new Intent(context, UploadService.class);
                            Bundle b = new Bundle();
                            msgIntent.putExtra("UploadStatus", 0);
                            msgIntent.putExtras(b);
                            context.startService(msgIntent);
                        }
                        editor.putInt("SyncIntentService.loc_count", ++loc_count);
                        editor.commit();
                    }
                    //endregion

                    //region Conveyance Location Update
                    if (isConveyance) {
                        isConveyance = false;
                        ConveyanceBean conveyanceBean = new ConveyanceBean();
                        conveyanceBean.IsLogin = String.valueOf(cAllowanceType);
                        conveyanceBean.LogDateTime = TimeStamp;
                        conveyanceBean.Latitude = String.valueOf(lat);
                        conveyanceBean.Longitude = String.valueOf(lon);
                        conveyanceBean.CellId = "0";
                        conveyanceBean.LAC = "0";
                        conveyanceBean.MNC = "0";
                        conveyanceBean.MCC = "0";
                        conveyanceLocationTable.insertLocation(conveyanceBean);
                        if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                            context.startService(new Intent(context, UpdateConveyanceService.class));
                        }
                    }
                    //endregion

                    //region Attendance Activity Location Update
                    if (isAttendanceActivity) {
                        isAttendanceActivity = false;
                        SendAttendanceBean attendanceBean = new SendAttendanceBean(context, attendanceType);
                        attendanceBean.LogDateTime = TimeStamp;
                        attendanceBean.Latitude = String.valueOf(lat);
                        attendanceBean.Longitude = String.valueOf(lon);
                        attendanceBean.CellId = "0";
                        attendanceBean.LAC = "0";
                        attendanceBean.MNC = "0";
                        attendanceBean.MCC = "0";
                        try {
                            if (Constants.serviceResponse != null) {
                                Object obj = Constants.serviceResponse.get(ServiceCallback.ATTENDANCE_ACTIVITY);
                                ((AttendanceActivity) obj).callService(attendanceBean);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //endregion

                    //region Conveyance Activity Location Update for start and stop
                    if (isConveyanceActivity) {
                        isConveyanceActivity = false;
                        ConveyanceBean conveyanceBean = new ConveyanceBean(conveyanceType);
                        conveyanceBean.LogDateTime = TimeStamp;
                        conveyanceBean.Latitude = String.valueOf(lat);
                        conveyanceBean.Longitude = String.valueOf(lon);
                        conveyanceBean.CellId = "0";
                        conveyanceBean.LAC = "0";
                        conveyanceBean.MNC = "0";
                        conveyanceBean.MCC = "0";
                        try {
                            if (Constants.serviceResponse != null) {
                                Object obj = Constants.serviceResponse.get(ServiceCallback.CONVEYANCE_ACTIVITY);
                                ((Conveyance) obj).startStopConveyance(conveyanceBean);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //endregion

                } catch (Exception e) {
                    e.printStackTrace();
                }
                LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                lm.removeUpdates(this);
            }
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        } catch (SecurityException ignore) {
        }
    }
    //endregion

    //region Listener Invoked On Fused Connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(TIME_INTERVAL);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            DeBug.ShowLogD("LSNS", "onConnected SecurityException-> " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            DeBug.ShowLogD("LSNS", "onConnected Exception-> " + e.getMessage());
            e.printStackTrace();
        }
    }
    //endregion

    //region UnHandled Listeners
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        DeBug.ShowLog("LSNS", "onStatusChanged : " + s + " : " + i);
    }

    @Override
    public void onProviderEnabled(String s) {
        DeBug.ShowLog("LSNS", "onProviderEnabled : " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        DeBug.ShowLog("LSNS", "onProviderDisabled : " + s);
    }

    @Override
    public void onConnectionSuspended(int i) {
        DeBug.ShowLog("LSNS", "onConnectionSuspended : " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        DeBug.ShowLog("LSNS", "onConnectionFailed : " + connectionResult.getErrorMessage());
    }
    //endregion

    //region Convert long to dateTime
    private String dateTime(long currTime) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(currTime);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        String strDate = sdf.format(c.getTime());
        return strDate;
    }
    //endregion
}