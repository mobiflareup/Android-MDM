package com.mobiocean.ui;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.mobiocean.R;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.ContactStruct;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.InsertIntoContactSync;
import com.mobiocean.util.RestApiCall;
import com.mobiocean.util.Sms;
import com.mobiocean.util.Sms.SMS;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class
DataBackupFragment extends Fragment {
	private final String TAG = DataBackupFragment.class.getSimpleName(); 
	protected static final String PREFS_NAME = "MyPrefsFile";
	public SharedPreferences settings;
	public SharedPreferences.Editor editor;
	
	public DataBackupFragment() {

	}
	AlertDialog.Builder alartBuilder ;
	AlertDialog alertDialog;
	int whichBackup = -1 ;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_databackup, container,false);

		whichBackup = -1;
		final CheckBox cbSms   = (CheckBox) rootView.findViewById(R.id.cbSMS);
		final CheckBox cbA     = (CheckBox) rootView.findViewById(R.id.cbAddressBook);
		final Switch tbSyncData= (Switch) rootView.findViewById(R.id.swSyncData);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
		else
			settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		editor = settings.edit();
		
		if(settings.getBoolean("canTakeBackup", false))
		{
			tbSyncData.setChecked(true);
			cbSms.setEnabled(true);
			cbA.setEnabled(true);
		}
		else
		{
			tbSyncData.setChecked(false);
			cbSms.setEnabled(false);
			cbA.setEnabled(false);
		}
		alartBuilder = new AlertDialog.Builder(getActivity());
		alartBuilder.setMessage("Want to take SMS backup?");
		alartBuilder.setPositiveButton("Yes", new OnClickListener() {
		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(whichBackup == 0)
					new dataSync().execute(0);
				else
					new dataSync().execute(1);

			}
		});
		alartBuilder.setNegativeButton("No", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		alertDialog  = alartBuilder.create();


		cbSms.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cbSms.setChecked(false);
				whichBackup = 0;
				alertDialog.setMessage("Want to take SMS backup?");
				if(isChecked && !alertDialog.isShowing())
					alertDialog.show();				
			}
		});

		cbA.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cbA.setChecked(false);
				whichBackup = 1;
				alertDialog.setMessage("Want to take Address Book backup?");
				if(isChecked && !alertDialog.isShowing())
					alertDialog.show();		

			}
		});
		tbSyncData.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				editor.putBoolean("canTakeBackup", isChecked);
				editor.apply();
				if(isChecked)
				{
					cbSms.setEnabled(true);
					cbA.setEnabled(true);
				}
				else
				{
					cbSms.setEnabled(false);
					cbA.setEnabled(false);
				}
			}
		});
		
		return rootView;
	}

	class dataSync extends AsyncTask<Integer, String, String>
	{


		protected  ProgressDialog ringProgressDialogLongTask;

		@Override
		protected void onPreExecute() {
			ringProgressDialogLongTask = new ProgressDialog(getActivity());
			ringProgressDialogLongTask.setMessage("Uploading. Please wait...");
			ringProgressDialogLongTask.setIndeterminate(false);
			ringProgressDialogLongTask.setMax(100);
			ringProgressDialogLongTask.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			ringProgressDialogLongTask.setCancelable(false);
			ringProgressDialogLongTask.show();
			super.onPreExecute();
		}


		@Override
		protected String doInBackground(Integer... params) {
			try {
				if (params[0] == 0) {
					List<SMS> smsList = getAllSms(getActivity());
					if (smsList.size() > 0) {
						publishProgress("sms");
						RestApiCall mRestApiCall = new RestApiCall();

						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(System.currentTimeMillis());
						DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
						String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();

						Sms sms = new Sms();
						sms.setAppId(CallHelper.Ds.structPC.iStudId);
						sms.setLogDateTime(SMSTimeStamp);
						sms.setSmsList(smsList);

						mRestApiCall.postSmsBackUp((MobiApplication) getActivity().getApplication(), sms);
					}
//					Calendar calendar = Calendar.getInstance();
//					calendar.setTimeInMillis(System.currentTimeMillis());
//					DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
//					String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();
//
//					RestApiCall mRestApiCall= new RestApiCall();
//
//					List<List<SMS>> partitions = new ArrayList<List<SMS>>();
//					int splitSize = 1;
//					for(int i=0;i<smsList.size();i=i + splitSize)
//					{
//						int size = Math.min(i + splitSize, smsList.size());
//						if(size == smsList.size())
//							size = (smsList.size() -splitSize )+1;
//
//						List<SMS> ll = new ArrayList<SMS>(size);
//						for(int k=i;k<Math.min(i + splitSize, smsList.size());k++)
//						{
//							ll.add(smsList.get(k));
//						}
//						partitions.add(ll);//add(listInsertIntoContactSync.subList(i, Math.min(i + 100, listInsertIntoContactSync.size())));
//
//						//  mRestApiCall.postContacts(getApp(), mContactStruct);
//					}
//
//					for(int i=0;i<partitions.size();i++)
//					{
//
//						Sms  mSms = new Sms();
//						mSms.setAppId(CallHelper.Ds.structPC.iStudId);
//						mSms.setLogDateTime(SMSTimeStamp);
//						mSms.setSmsList(partitions.get(i));
//						DeBug.ShowLog("UploadingContact",partitions.size()+" "+i);
//						mRestApiCall.postSmsBackUp((MobiApplication)getActivity().getApplication(), mSms);
//						publishProgress("" + (int) ((i*partitions.size()* 100) / smsList.size()));
//					}
				} else {
					ArrayList<InsertIntoContactSync> listInsertIntoContactSync = CallHelper.getContactList(getActivity());
					publishProgress("con");
					if (listInsertIntoContactSync.size() > 0) {
						RestApiCall mRestApiCall = new RestApiCall();

						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(System.currentTimeMillis());
						DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
						String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();

						ContactStruct contactStruct = new ContactStruct();
						contactStruct.setAppId(CallHelper.Ds.structPC.iStudId);
						contactStruct.setLogDateTime(SMSTimeStamp);
						contactStruct.setList(listInsertIntoContactSync);

						mRestApiCall.postContacts((MobiApplication) getActivity().getApplication(), CallHelper.Ds.structPC.iStudId, contactStruct);

					}

//					List<List<InsertIntoContactSync>> partitions = new ArrayList<List<InsertIntoContactSync>>();
//					int splitSize = 10;
//					for(int i=0;i<listInsertIntoContactSync.size();i=i + splitSize)
//					{
//						int size = Math.min(i + splitSize, listInsertIntoContactSync.size());
//						if(size == listInsertIntoContactSync.size())
//							size = (listInsertIntoContactSync.size() -splitSize )+1;
//						if(size > 0) {
//							List<InsertIntoContactSync> ll = new ArrayList<InsertIntoContactSync>(size);
//							for (int k = i; k < Math.min(i + splitSize, listInsertIntoContactSync.size()); k++) {
//								ll.add(listInsertIntoContactSync.get(k));
//							}
//							partitions.add(ll);//add(listInsertIntoContactSync.subList(i, Math.min(i + 100, listInsertIntoContactSync.size())));
//
//							//  mRestApiCall.postContacts(getApp(), mContactStruct);
//						}
//					}
//					Calendar calendar = Calendar.getInstance();
//					calendar.setTimeInMillis(System.currentTimeMillis());
//					DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
//					String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();

//					for(int i=0;i<partitions.size();i++)
//					{
//
//						ContactStruct mContactStruct = new ContactStruct();
//						mContactStruct.setAppId(CallHelper.Ds.structPC.iStudId);
//						mContactStruct.setLogDateTime(SMSTimeStamp);
//						mContactStruct.setList(partitions.get(i));
//						DeBug.ShowLog("UploadingContact",partitions.size()+" "+i);
//						mRestApiCall.postContacts((MobiApplication)getActivity().getApplication(), "1",mContactStruct);
//						publishProgress("" + (int) ((i*partitions.size()* 100) / listInsertIntoContactSync.size()));
//					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			try {
				if (values[0].equalsIgnoreCase("con"))
					ringProgressDialogLongTask.setMessage("uploading Contacts. Please wait...");
				else if (values[0].equalsIgnoreCase("sms"))
					ringProgressDialogLongTask.setMessage("uploading SMS. Please wait...");
				else {
					ringProgressDialogLongTask.setProgress(Integer.parseInt(values[0]));
				}
				super.onProgressUpdate(values);
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				ringProgressDialogLongTask.dismiss();
				Toast.makeText(getActivity(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
				super.onPostExecute(result);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	public List<SMS> getAllSms(Context context) {
		Sms sms = new Sms();
		Sms.SMS objSms = sms.newSMS();
		Uri message = Uri.parse("content://sms/");
		ContentResolver cr = context.getContentResolver();

		Cursor c = cr.query(message, null, null, null, null);
		int totalSMS = c.getCount();

		if (c.moveToFirst()) {
			for (int i = 0; i < totalSMS; i++) {

				objSms = sms.newSMS();
				objSms.setAddress(c.getString(c
						.getColumnIndexOrThrow("address")));
				objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(Long.parseLong(c.getString(c.getColumnIndexOrThrow("date"))));
				DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
				String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();	
				objSms.setStartDateTime(SMSTimeStamp);
				if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
					objSms.setIsIncoming(1);
				} else {
					objSms.setIsIncoming(0);
				}
				DeBug.ShowLogD(TAG, objSms.getIsIncoming()+" "+objSms.getMsg());
				sms.addToList(objSms);
				c.moveToNext();
			}
		}
		// else {
		// throw new RuntimeException("You have no SMS");
		// }
		c.close();

		return sms.getSmsList();
	}
}
