package org.conveyance.configuration;

import org.conveyance.model.RAddCustomerModel;
import org.conveyance.model.RControlModel;
import org.conveyance.model.RCustomerModel;
import org.conveyance.model.RExtraAllowanceModel;
import org.conveyance.model.RModeModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RConveyanceAPI {

    @GET("api/Visit/CustomerList")
    Call<ArrayList<RCustomerModel>> getCustomer(@Query("appId") String p_appId);

    @GET("api/Travel/Mode")
    Call<ArrayList<RModeModel>> getMode(@Query("appId") String p_appId);

    @POST("api/visit/Detail")
    Call<Integer> startStopConveyance(@Body RControlModel controlModel);

    @POST("api/Travel/VisitInfo")
    Call<Integer> visitInfo(@Body RControlModel controlModel);

    @POST("api/Travel/ExtraAllowance")
    Call<Integer> extraAllowance(@Body RExtraAllowanceModel extraAllowanceModel);

    @POST("api/Conveyance/InsertCustomerDetails")
    Call<Integer> createCustomer(@Body RAddCustomerModel addCustomerModel);

}