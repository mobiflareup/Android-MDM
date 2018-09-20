package org.conveyance.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobiocean.util.DeBug;

import org.conveyance.model.RCustomerModel;

import java.util.ArrayList;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          12-11-16         Initial creation
 ****************************************************************************/

public class RCustomerListTableDB extends RTableData.CustomerListTable {
    private SQLiteDatabase database;
    private RDBHelper dbHelper;
    private Context context;

    public RCustomerListTableDB(Context context) {
        this.context = context;
        dbHelper = new RDBHelper(context);
    }

    public void insertCustomers(ArrayList<RCustomerModel> customerListModelsArr, String appId) {
        long insertId = 0;
        Open();
        for(RCustomerModel customerListModels:customerListModelsArr) {
            ContentValues values = new ContentValues();
            values.put(APPID,appId);
            values.put(CustomerId, customerListModels.getCustomerId());
            values.put(CustomerName, customerListModels.getCustomerName());
            values.put(EmailId, customerListModels.getEmailId());
            values.put(MobileNo, customerListModels.getMobileNo());
            values.put(Country, customerListModels.getCountry());
            values.put(State, customerListModels.getState());
            values.put(City, customerListModels.getCity());
            values.put(Latitude, customerListModels.getLatitude());
            values.put(Longitude, customerListModels.getLongitude());
            values.put(Address, customerListModels.getAddress());
            values.put(AltEmailId, customerListModels.getAltEmailId());
            values.put(District, customerListModels.getDistrict());
            values.put(PinCode, customerListModels.getPinCode());
            values.put(TinNumber, customerListModels.getTinNumber());
            values.put(AltAddress, customerListModels.getAltAddress());
            insertId = database.insert(TABLE_NAME, null, values);
            DeBug.ShowLog("Templates Inserted : ", customerListModels.getCustomerName() + " : " + insertId);
        }
        Close();
    }


    public ArrayList<RCustomerModel> getAllCustomer() {
        Open();
        ArrayList<RCustomerModel> customerListModelArrayList=new ArrayList<>();

        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = database.rawQuery(countQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RCustomerModel customerListModel=new RCustomerModel();
                customerListModel.setCustomerId(cursor.getInt(cursor.getColumnIndex(CustomerId)));
                customerListModel.setCustomerName(cursor.getString(cursor.getColumnIndex(CustomerName)));
                customerListModel.setEmailId(cursor.getString(cursor.getColumnIndex(EmailId)));
                customerListModelArrayList.add(customerListModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Close();
        return customerListModelArrayList;
    }

    public void Delete_Table() {
        int deletedRows = 0;
        Open();
        deletedRows = database.delete(TABLE_NAME, null, null);
        Close();
    }

    /***
     * Open Database
     */
    private void Open() {
        database = dbHelper.getWritableDatabase();
    }

    /***
     * Close Database
     */
    private void Close() {
        dbHelper.close();
    }

}
