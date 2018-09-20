package org.conveyance.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mobiocean.R;

import org.conveyance.configuration.RHelper;
import org.conveyance.configuration.RSharedData;
import org.conveyance.model.RAddCustomerModel;
import org.sn.location.LocationBean;
import org.sn.location.LocationDetails;
import org.sn.location.NetworkUtil;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

public class RAddCustomerActivity extends AppCompatActivity {
    //Mandatory
    private EditText customer_name, mobile_no, email_id, contact_person, address, latitude, longitude, pin_code;
    //Optional
    private EditText alt_mobile_no, alt_email_id, alt_contact_person, alt_address, city, district, state, country, tin_code;
    private Button addbtn;
    private Context context;
    private Handler handler;
    private Toolbar toolbar;
    private RHelper helper = new RHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r_activity_add_customer);

        context = this;
        handler = new Handler();

        //Mandatory
        customer_name = (EditText) findViewById(R.id.customer_name);
        mobile_no = (EditText) findViewById(R.id.mobile_no);
        email_id = (EditText) findViewById(R.id.email_id);
        contact_person = (EditText) findViewById(R.id.contact_person);
        address = (EditText) findViewById(R.id.address);
        latitude = (EditText) findViewById(R.id.latitude);
        longitude = (EditText) findViewById(R.id.longitude);
        pin_code = (EditText) findViewById(R.id.pin_code);
        tin_code = (EditText) findViewById(R.id.tin_code);

        //Optional
        alt_mobile_no = (EditText) findViewById(R.id.alt_mobile_no);
        alt_email_id = (EditText) findViewById(R.id.alt_email_id);
        alt_contact_person = (EditText) findViewById(R.id.alt_contact_person);
        alt_address = (EditText) findViewById(R.id.alt_address);
        city = (EditText) findViewById(R.id.city);
        district = (EditText) findViewById(R.id.district);
        state = (EditText) findViewById(R.id.state);
        country = (EditText) findViewById(R.id.country);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        addbtn = (Button) findViewById(R.id.addbtn);

        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCustomer();
            }
        });

        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationDetails locationDetails = new LocationDetails(context);
            if (locationDetails.hasGPSDevice()) {
                final LocationBean locBean = locationDetails.getLocation();
                LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (manager!=null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locBean.Lat!=null && !isEmpty(locBean.Lat)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Geocoder geocoder;
                                List<Address> addresses;
                                geocoder = new Geocoder(context, Locale.getDefault());
                                addresses = geocoder.getFromLocation(Double.parseDouble(locBean.Lat), Double.parseDouble(locBean.Longt), 1);
                                final String addressTxt = addresses.get(0).getAddressLine(0);
                                final String cityTxt = addresses.get(0).getLocality();
                                final String stateTxt = addresses.get(0).getAdminArea();
                                final String countryTxt = addresses.get(0).getCountryName();
                                final String postalCodeTxt = addresses.get(0).getPostalCode();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        address.setText(addressTxt);
                                        city.setText(cityTxt);
                                        state.setText(stateTxt);
                                        country.setText(countryTxt);
                                        pin_code.setText(postalCodeTxt);
                                        latitude.setText(locBean.Lat);
                                        longitude.setText(locBean.Longt);
                                    }
                                });
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else {
                    AlertDialog alertDialog;
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(context);
                    builder.setTitle("Cannot get Location")
                            .setMessage("Please Enable GPS to find your Location")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    context.startActivity(intent);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            } else {
                Toast.makeText(context, "Device has no gps hardware in it", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void createCustomer(){

        String cust_name = customer_name.getText().toString().trim();
        String mob_no = mobile_no.getText().toString().trim();
        String alt_mob_no = alt_mobile_no.getText().toString().trim();
        String email = email_id.getText().toString().trim();
        String alt_email = alt_email_id.getText().toString().trim();
        String cnt_pers = contact_person.getText().toString().trim();
        String alt_cnt_pers = alt_contact_person.getText().toString().trim();
        String addr = address.getText().toString().trim();
        String alt_addr = alt_address.getText().toString().trim();
        String lat = latitude.getText().toString().trim();
        String longt = longitude.getText().toString().trim();
        String pin_cde = pin_code.getText().toString().trim();
        String tin_cde = tin_code.getText().toString().trim();
        String cty = city.getText().toString().trim();
        String dist = district.getText().toString().trim();
        String stat = state.getText().toString().trim();
        String cont = country.getText().toString().trim();
        RSharedData settings = new RSharedData(context);
        if(notEmpty(cust_name) && isValidMobileNo(mob_no) && isValidEmail(email) && notEmpty(cnt_pers) && notEmpty(addr) && notEmpty(lat) && notEmpty(longt) && notEmpty(pin_cde)){

            RAddCustomerModel custDet = new RAddCustomerModel(settings.getAppId(),cust_name,mob_no,email,alt_mob_no,cnt_pers,alt_cnt_pers,alt_email,addr,alt_addr,lat,longt,cty,dist,stat,cont,pin_cde,tin_cde);

            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                Call<Integer> call = helper.getInterface().createCustomer(custDet);
                call.enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        if(response!=null){
                            Integer result = response.body();
                            if(result!=null && result>0){
                                Toast.makeText(context, "Customer added successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                Toast.makeText(context, "Please try again later", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(context, "Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Toast.makeText(context, "Please try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Check Your Internet !", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(context, "Fill All Fields Properly", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean notEmpty(CharSequence s){
        return s!=null && !s.toString().trim().isEmpty();
    }

    /**
     * Function to Check the given string is a valid email or not.
     */
    public boolean isValidEmail(CharSequence emailId) {
        return notEmpty(emailId) && android.util.Patterns.EMAIL_ADDRESS.matcher(emailId).matches();
    }

    /**
     * Function to mobile number is 10digit
     */
    public boolean isValidMobileNo(CharSequence mobileNo) {
        return notEmpty(mobileNo) && mobileNo.length() == 10;
    }

}