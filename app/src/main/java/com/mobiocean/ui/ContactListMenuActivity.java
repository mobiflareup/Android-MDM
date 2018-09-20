package com.mobiocean.ui;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobiocean.R;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.ContactStruct;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.InsertIntoContactSync;
import com.mobiocean.util.LKONagarNigam;
import com.mobiocean.util.RestApiCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ContactListMenuActivity extends Activity {

	ArrayList<String> spinnerDateArray = new ArrayList<String>();
	ArrayAdapter<String> spinnerArrayAdapter;
	Spinner spinner1;
	static String date = "";
	//	ArrayList<InsertIntoContactSync> mInsertContact = null;
	Button btnsubmit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list_menu);

		// Creating a button click listener for the "Add Contact" button
		OnClickListener addClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) 
			{
				spinner1.setVisibility(Spinner.VISIBLE);
				btnsubmit.setVisibility(Button.VISIBLE);
				new ExtractContactDatesFromServer().execute();

			};
		};


		// Creating a button click listener for the "Add Contact" button
		OnClickListener contactsClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) 
			{

				new ContactSyncWithServer().execute();

			}
		};

		OnClickListener getdataFromserver = new OnClickListener() {

			@Override
			public void onClick(View v) 
			{
				if(spinner1.getSelectedItemPosition()!=0)
					new GetContactFromServer().execute(""+spinner1.getSelectedItem());

			}
		};


		// Getting reference to "Add Contact" button
		Button btnAdd = (Button) findViewById(R.id.btn_add);

		// Getting reference to "Contacts List" button
		Button btnContacts = (Button) findViewById(R.id.btn_contacts);

		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner1.setVisibility(Spinner.GONE);

		// Setting click listener for the "Add Contact" button
		btnAdd.setOnClickListener(addClickListener);

		// Setting click listener for the "List Contacts" button
		btnContacts.setOnClickListener(contactsClickListener);

		btnsubmit  = (Button) findViewById(R.id.btn_submit);
		btnsubmit.setVisibility(Button.GONE);
		btnsubmit.setOnClickListener(getdataFromserver);

	}

	public ArrayList<InsertIntoContactSync> extractContacts(MobiApplication app)
	{

		ArrayList<InsertIntoContactSync> mInsertContact = LKONagarNigam.loadContactSync(app);



		if(mInsertContact!=null && !mInsertContact.equals(""))
		{
			DeBug.ShowLog("From_server", "Extract List From Server"+mInsertContact.size());
			return mInsertContact;
		}
		else
		{
			return null;
		}
	}


	public void fetchContacts() {

		String phoneNumber = null;
		//String email = null;
		ArrayList<InsertIntoContactSync> listInsertIntoContactSync = new ArrayList<InsertIntoContactSync>();
		ArrayList<String> listPhoneNumber = new ArrayList<String>();

		String  CurrentTime = ""+System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(CurrentTime));
		DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();


		Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
		String _ID = ContactsContract.Contacts._ID;
		String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
		String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

		Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
		String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

		Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
		String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
		String DATA = ContactsContract.CommonDataKinds.Email.DATA;

		StringBuffer output = new StringBuffer();

		ContentResolver contentResolver = getContentResolver();

		Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);	

		// Loop for every contact in the phone
		if (cursor.getCount() > 0) 
		{
			while (cursor.moveToNext()) {

				String email = null;

				String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
				String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));

				int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

				if (hasPhoneNumber > 0)
				{

					if(name.equalsIgnoreCase("mahendra"))
						output.append("\n First Name:" + name);

					// Query and loop for every phone number of the contact
					Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
					if (phoneCursor.moveToNext())
					{
						phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
						String lookUp = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
						output.append("\n Phone number:" + phoneNumber);

						if(listPhoneNumber.contains(phoneNumber))
						{
							Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookUp);
							DeBug.ShowLog("ContactSync", phoneNumber+" "+name+ " "+contentResolver.delete(uri,  null, null));
							continue;
						}
						else{
							listPhoneNumber.add(phoneNumber);
						}
					}

					phoneCursor.close();

					// Query and loop for every email of the contact
					Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,	null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);

					while (emailCursor.moveToNext()) {

						email = emailCursor.getString(emailCursor.getColumnIndex(DATA));

						output.append("\nEmail Id:" + email);

					}

					emailCursor.close();
				}

				output.append("\n");


				InsertIntoContactSync mInsertIntoContactSync = new InsertIntoContactSync(name, phoneNumber, email, SMSTimeStamp);	

				//		WebserviceCall WSC = new WebserviceCall();


				listInsertIntoContactSync.add(mInsertIntoContactSync);
			}
		}
		RestApiCall mRestApiCall= new RestApiCall();
		ContactStruct mContactStruct = new ContactStruct();
		mContactStruct.setAppId(CallHelper.Ds.structPC.iStudId);
		mContactStruct.setLogDateTime(SMSTimeStamp);
		mContactStruct.setList(listInsertIntoContactSync);

		mRestApiCall.postContacts(getApp(),"0", mContactStruct);

		for(int i=0;i<listInsertIntoContactSync.size();i++)
		{
			InsertIntoContactSync mInsertIntoContactSync =listInsertIntoContactSync.get(i) ;
			RestApiCall mRestApiCall1= new RestApiCall();
			JSONObject json= new JSONObject();
			try {
				json.put("AnroidApp_Id",CallHelper.Ds.structPC.iStudId);
				json.put("Messanger_Id",""+0);
				json.put("ContactName",mInsertIntoContactSync.getContact_name());
				json.put("ContactMobileNo1",mInsertIntoContactSync.getCont_mob1());
				json.put("EmailId",mInsertIntoContactSync.getEmail_id());
				json.put("LogDate",mInsertIntoContactSync.getLogdate());

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	

			int resultForWebService=Integer.parseInt(mRestApiCall1.InsertIntoContactSync(json));

		}
	}
	public void removeRepetedContacts() {

		String phoneNumber = null;
		//String email = null;
		ArrayList<InsertIntoContactSync> listInsertIntoContactSync = new ArrayList<InsertIntoContactSync>();
		ArrayList<String> listPhoneNumber = new ArrayList<String>();

		String  CurrentTime = ""+System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(CurrentTime));
		DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();


		Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
		String _ID = ContactsContract.Contacts._ID;
		String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
		String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

		Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
		String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

		Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
		String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
		String DATA = ContactsContract.CommonDataKinds.Email.DATA;

		StringBuffer output = new StringBuffer();

		ContentResolver contentResolver = getContentResolver();

		Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);	

		// Loop for every contact in the phone
		if (cursor.getCount() > 0) 
		{
			while (cursor.moveToNext()) {

				String email = null;

				String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
				String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));

				int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

				if (hasPhoneNumber > 0)
				{
					if(name.equalsIgnoreCase("mahendra"))
						output.append("\n First Name:" + name);

					// Query and loop for every phone number of the contact
					Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
					if (phoneCursor.moveToNext())
					{
						phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
						String lookUp = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
						output.append("\n Phone number:" + phoneNumber);

						if(listPhoneNumber.contains(phoneNumber))
						{
							Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookUp);
							DeBug.ShowLog("ContactSync", phoneNumber+" "+name+ " "+contentResolver.delete(uri,  null, null));
							continue;
						}
						else{
							listPhoneNumber.add(phoneNumber);
						}
					}

					phoneCursor.close();

				}
			}
		}
	}

	public class ExtractContactDatesFromServer extends AsyncTask<Void, Void, ArrayList<InsertIntoContactSync>> 
	{
		protected  ProgressDialog ringProgressDialogLongTask;
		int startTime,endTime;
		long milliStartTime,millisEndTime;
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */

		@Override
		protected void onPreExecute() 
		{
			ringProgressDialogLongTask  =  ProgressDialog.show(ContactListMenuActivity.this, "Please wait ...", "Loading ...", true);
			ringProgressDialogLongTask.setCancelable(false);
			super.onPreExecute();
		}

		@Override
		protected ArrayList<InsertIntoContactSync> doInBackground(Void... params) 
		{
			// TODO Auto-generated method stub
			milliStartTime = System.currentTimeMillis();
			//startTime=	TimeUnit.MILLISECONDS.toMinutes(milliStartTime);
			startTime = (int) (((milliStartTime / 1000) / 60) % 60);
			DeBug.ShowLog("TimeTaken","StartTime  "+startTime+" minutes "+milliStartTime+" millisec");


			return extractContacts(getApp());

		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */

		@Override
		protected void onPostExecute(ArrayList<InsertIntoContactSync> result) 
		{
			// TODO Auto-generated method stub

			ringProgressDialogLongTask.dismiss();
			//super.onPostExecute(result);
			millisEndTime = System.currentTimeMillis();
			endTime = (int) (((millisEndTime / 1000) / 60) % 60);
			//endTime=	TimeUnit.MILLISECONDS.toMinutes(millisEndTime);
			DeBug.ShowLog("TimeTaken","EndTime  "+endTime+" minutes"+millisEndTime+"millisec");
			DeBug.ShowLog("TimeTaken","That took " +(endTime - startTime)+" minutes  "+ (millisEndTime-milliStartTime) + " milliseconds");


			if(result!=null && !result.isEmpty())
			{	
				spinnerDateArray.clear();
				spinnerDateArray.add("__Select__");

				for(int i=0; i<result.size(); i++)
				{
					spinnerDateArray.add(result.get(i).getLogDateTime());
				}

				spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.customspinner_item, spinnerDateArray);
				spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_selector);
				spinner1.setAdapter(spinnerArrayAdapter);
				spinnerArrayAdapter.notifyDataSetChanged();
				spinner1.setOnItemSelectedListener(new OnItemSelectedListener(){

					@Override
					public void onItemSelected(AdapterView<?> parent, View view,
							int position, long id) {

						if(spinner1.getSelectedItemPosition()==0)
						{
							btnsubmit.setClickable(false);		
						}
						else{
							btnsubmit.setClickable(true);	
							date = spinner1.getSelectedItem().toString();
						}

					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}});
			}

			Toast.makeText(ContactListMenuActivity.this,"Updated Successfully",Toast.LENGTH_SHORT).show();
		}
	}

	private  MobiApplication getApp() 
	{
		return (MobiApplication) getApplication();
	}

	public class ContactSyncWithServer extends AsyncTask<Void, String, String> 
	{
		protected  ProgressDialog ringProgressDialogLongTask;
		int startTime,endTime;
		long milliStartTime,millisEndTime;
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() 
		{
			try {
				ringProgressDialogLongTask = new ProgressDialog(ContactListMenuActivity.this);
				ringProgressDialogLongTask.setMessage("Uploading Contacts. Please wait...");
				ringProgressDialogLongTask.setIndeterminate(false);
				ringProgressDialogLongTask.setMax(100);
				ringProgressDialogLongTask.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				ringProgressDialogLongTask.setCancelable(false);
				ringProgressDialogLongTask.show();
				super.onPreExecute();
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(Void... params) 
		{
			try {
				{
					ArrayList<InsertIntoContactSync> listInsertIntoContactSync = CallHelper.getContactList(getBaseContext());
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

						mRestApiCall.postContacts((MobiApplication) getApplication(), CallHelper.Ds.structPC.iStudId, contactStruct);

					}
				}
				// TODO Auto-generated method stub
//				milliStartTime = System.currentTimeMillis();
//				//startTime=	TimeUnit.MILLISECONDS.toMinutes(milliStartTime);
//				startTime = (int) (((milliStartTime / 1000) / 60) % 60);
//				DeBug.ShowLog("TimeTaken", "StartTime  " + startTime + " minutes " + milliStartTime + " millisec");
//
//				publishProgress("contact");
//				/*ArrayList<InsertIntoContactSync> listInsertIntoContactSync = CallHelper.getContactList(ContactListMenuActivity.this);*/
//				publishProgress("Upload");
//				/*if (listInsertIntoContactSync.size() > 0) {
//					RestApiCall mRestApiCall = new RestApiCall();
//
//					Calendar calendar = Calendar.getInstance();
//					calendar.setTimeInMillis(System.currentTimeMillis());
//					DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
//					String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();
//
//					ContactStruct contactStruct = new ContactStruct();
//					contactStruct.setAppId(CallHelper.Ds.structPC.iStudId);
//					contactStruct.setLogDateTime(SMSTimeStamp);
//					contactStruct.setList(listInsertIntoContactSync);
//
//					mRestApiCall.postContacts((MobiApplication) getApplication(), CallHelper.Ds.structPC.iStudId, contactStruct);
//
////					fetchContacts();
//
//				}*/
//
//			ArrayList<String> listPhoneNumberForServer = new ArrayList<String>();
//
//			String phoneNumber = null;
//			//String email = null;
//			ArrayList<InsertIntoContactSync> listInsertIntoContactSync = new ArrayList<InsertIntoContactSync>();
//			ArrayList<String> listPhoneNumber = new ArrayList<String>();
//
//			String  CurrentTime = ""+System.currentTimeMillis();
//			Calendar calendar = Calendar.getInstance();
//			calendar.setTimeInMillis(Long.parseLong(CurrentTime));
//			DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
//			String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();
//
//			Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
//			String _ID = ContactsContract.Contacts._ID;
//			String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
//			String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
//
//			Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
//			String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
//			String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
//
//			Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
//			String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
//			String DATA = ContactsContract.CommonDataKinds.Email.DATA;
//
//			StringBuffer output = new StringBuffer();
//
//			ContentResolver contentResolver = getContentResolver();
//
//			Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);
//
//			// Loop for every contact in the phone
//			int j = 0;
//			if (cursor.getCount() > 0)
//			{
//				while (cursor.moveToNext()) {
//
//					String email = null;
//
//					String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
//					String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
//
//					int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
//
//					if (hasPhoneNumber > 0)
//					{
//
//						if(name.equalsIgnoreCase("mahendra"))
//							output.append("\n First Name:" + name);
//
//						// Query and loop for every phone number of the contact
//						Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
//						if (phoneCursor.moveToNext())
//						{
//							phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER)).trim();
//							String lookUp = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
//							output.append("\n Phone number:" + phoneNumber);
//
//							if(listPhoneNumber.contains(phoneNumber))
//							{
//								Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookUp);
//								DeBug.ShowLog("ContactSync", phoneNumber+" "+name+ " "+contentResolver.delete(uri,  null, null));
//								continue;
//							}
//							else{
//								listPhoneNumber.add(phoneNumber);
//							}
//						}
//
//						phoneCursor.close();
//
//						// Query and loop for every email of the contact
//						Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,	null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);
//
//						while (emailCursor.moveToNext()) {
//
//							email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
//
//							output.append("\nEmail Id:" + email);
//
//						}
//
//						emailCursor.close();
//					}
//
//					output.append("\n");
//
////					phoneNumber = phoneNumber.replace(" ", "");
//
////					if(phoneNumber.contains("9579543434"))
////						DeBug.ShowLogD("AginAndAgain", ""+phoneNumber);
////
////					StringBuffer bb = new StringBuffer(phoneNumber);
////
////					if(phoneNumber.startsWith("091"))
////						bb.delete(0, 3);
////					else
////						if(phoneNumber.startsWith("+91"))
////							bb.delete(0, 3);
////					else
////					if(phoneNumber.startsWith("0"))
////						bb.delete(0, 1);
////					else
////						if(phoneNumber.startsWith("+"))
////							bb.delete(0, 1);
////
////
////					if(phoneNumber.contains("9579543434"))
////						DeBug.ShowLogD("AginAndAgain", ""+bb.toString());
//
//					//		WebserviceCall WSC = new WebserviceCall();
//
//					if(phoneNumber!=null) {
//						StringBuffer bb = new StringBuffer(phoneNumber);
//						publishProgress("" + (int) ((j++ * 100) / cursor.getCount()));
//
//						if (!listPhoneNumberForServer.contains(bb.toString())) {
//							listPhoneNumberForServer.add(bb.toString());
//							InsertIntoContactSync mInsertIntoContactSync = new InsertIntoContactSync(name, bb.toString(), email, SMSTimeStamp);
//							mInsertIntoContactSync.setMessangerId("" + 0);
//							listInsertIntoContactSync.add(mInsertIntoContactSync);
//						}
//					}
//				}
//			}
//			publishProgress("Upload");
//
//			DeBug.ShowLog("UploadingContact","1 "+listInsertIntoContactSync.size());
//			RestApiCall mRestApiCall= new RestApiCall();
//
//			List<List<InsertIntoContactSync>> partitions = new ArrayList<List<InsertIntoContactSync>>();
//			int splitSize = 10;
//			for(int i=0;i<listInsertIntoContactSync.size();i=i + splitSize)
//			{
//				int size = Math.min(i + splitSize, listInsertIntoContactSync.size());
//				if(size == listInsertIntoContactSync.size())
//					size = (listInsertIntoContactSync.size() -splitSize )+1;
//				List<InsertIntoContactSync> ll = new ArrayList<InsertIntoContactSync>(size);
//				for(int k=i;k<Math.min(i + splitSize, listInsertIntoContactSync.size());k++)
//				{
//					ll.add(listInsertIntoContactSync.get(k));
//				}
//				partitions.add(ll);//add(listInsertIntoContactSync.subList(i, Math.min(i + 100, listInsertIntoContactSync.size())));
//
//				//  mRestApiCall.postContacts(getApp(), mContactStruct);
//			}
//
//			for(int i=0;i<partitions.size();i++)
//			{
//
//				ContactStruct mContactStruct = new ContactStruct();
//				mContactStruct.setAppId(CallHelper.Ds.structPC.iStudId);
//				mContactStruct.setLogDateTime(SMSTimeStamp);
//				mContactStruct.setList(partitions.get(i));
//				DeBug.ShowLog("UploadingContact",partitions.size()+" "+i);
//				mRestApiCall.postContacts(getApp(),"0", mContactStruct);
//				publishProgress("" + (int) ((i*partitions.size()* 100) / listInsertIntoContactSync.size()));
//			}
//
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;

		}

		@Override
		protected void onProgressUpdate(String... progress) {
			// Set progress percentage
//			if(progress[0].equalsIgnoreCase("Upload"))
//				ringProgressDialogLongTask.setMessage("Uploading Contacts. Please wait...");
//			else
//				ringProgressDialogLongTask.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String result) 
		{
			// TODO Auto-generated method stub
			try {
				ringProgressDialogLongTask.dismiss();
				//super.onPostExecute(result);
				millisEndTime = System.currentTimeMillis();
				endTime = (int) (((millisEndTime / 1000) / 60) % 60);
				//endTime=	TimeUnit.MILLISECONDS.toMinutes(millisEndTime);
				Toast.makeText(ContactListMenuActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
				DeBug.ShowLog("TimeTaken", "EndTime  " + endTime + " minutes" + millisEndTime + "millisec");
				DeBug.ShowLog("TimeTaken", "That took " + (endTime - startTime) + " minutes  " + (millisEndTime - milliStartTime) + " milliseconds");
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public class GetContactFromServer extends AsyncTask<String, String, String> 
	{
		protected  ProgressDialog ringProgressDialogLongTask;
		int startTime,endTime;
		long milliStartTime,millisEndTime;
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() 
		{
			try {
				ringProgressDialogLongTask = new ProgressDialog(ContactListMenuActivity.this);
				ringProgressDialogLongTask.setMessage("Downloading Contacts. Please wait...");
				ringProgressDialogLongTask.setIndeterminate(false);
				ringProgressDialogLongTask.setMax(100);
				ringProgressDialogLongTask.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				ringProgressDialogLongTask.setCancelable(false);
				ringProgressDialogLongTask.show();
				super.onPreExecute();
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(String... params) 
		{
			try {
				milliStartTime = System.currentTimeMillis();
				int j = 0;

				startTime = (int) (((milliStartTime / 1000) / 60) % 60);
				DeBug.ShowLog("TimeTaken", "StartTime  " + startTime + " minutes " + milliStartTime + " millisec");

				//		extractdatafromserver(getApp());

				ArrayList<InsertIntoContactSync> mInsertContact = new ArrayList<InsertIntoContactSync>();

				mInsertContact = LKONagarNigam.loadContactSyncFromDate(getApp(), params[0]);


				DeBug.ShowLog("DownloadingContacts", "1  " + mInsertContact.size());

				if (mInsertContact != null && !mInsertContact.isEmpty()) {

					for (int i = 0; i < mInsertContact.size(); i++) {
						InsertIntoContactSync mInsertIntoContactSync = mInsertContact.get(i);

						mInsertIntoContactSync.getContact_name();

						ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

						int rawContactID = ops.size();

						// Adding insert operation to operations list
						// to insert a new raw contact in the table ContactsContract.RawContacts
						ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
								.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
								.withValue(RawContacts.ACCOUNT_NAME, null)
								.build());

						// Adding insert operation to operations list
						// to insert display name in the table ContactsContract.Data
						ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
								.withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
								.withValue(StructuredName.DISPLAY_NAME, mInsertIntoContactSync.getContact_name())
								.build());

						// Adding insert operation to operations list
						// to insert Mobile Number in the table ContactsContract.Data
						ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
								.withValue(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
								.withValue(Phone.NUMBER, mInsertIntoContactSync.getCont_mob1())
								.withValue(Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
								.build());

						// Adding insert operation to operations list
						// to insert Work Email in the table ContactsContract.Data
						ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
								.withValue(ContactsContract.Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
								.withValue(Email.ADDRESS, mInsertIntoContactSync.getEmail_id())
								.withValue(Email.TYPE, Email.TYPE_WORK)
								.build());
						DeBug.ShowLog("DownloadingContacts", "2 " + i);
						try {
							// Executing all the insert operations as a single database transaction
							getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
							//Toast.makeText(ContactListMenuActivity.this, "Contact is successfully added", Toast.LENGTH_SHORT).show();
							DeBug.ShowLog("DownloadingContacts", "3 " + i);

						} catch (RemoteException e) {
							e.printStackTrace();
							DeBug.ShowLog("DownloadingContacts", "4 " + i);
						} catch (OperationApplicationException e) {
							DeBug.ShowLog("DownloadingContacts", "5 " + i);
							e.printStackTrace();
						}
						publishProgress("" + (int) ((i * 100) / mInsertContact.size()));
					}
					publishProgress("Remove");
					removeRepetedContacts();
				} else {
					mInsertContact = null;
				}


			}catch (Exception e){
				e.printStackTrace();
			}
			return null;

		}

		@Override
		protected void onProgressUpdate(String... progress) {
			try {
				// Set progress percentage
				if (progress[0].equalsIgnoreCase("Remove"))
					ringProgressDialogLongTask.setMessage("Finalizing. Please wait...");
				else
					ringProgressDialogLongTask.setProgress(Integer.parseInt(progress[0]));
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		@Override
		protected void onPostExecute(String result) 
		{
			try {
				// TODO Auto-generated method stub

				ringProgressDialogLongTask.dismiss();
				//super.onPostExecute(result);
				millisEndTime = System.currentTimeMillis();
				endTime = (int) (((millisEndTime / 1000) / 60) % 60);
				//endTime=	TimeUnit.MILLISECONDS.toMinutes(millisEndTime);
				DeBug.ShowLog("TimeTaken", "EndTime  " + endTime + " minutes" + millisEndTime + "millisec");
				DeBug.ShowLog("TimeTaken", "That took " + (endTime - startTime) + " minutes  " + (millisEndTime - milliStartTime) + " milliseconds");
			}catch (Exception e){
				e.printStackTrace();
			}

		}
	}

	public void extractdatafromserver(MobiApplication app) {

		ArrayList<InsertIntoContactSync> mInsertContact = new ArrayList<InsertIntoContactSync>();
		mInsertContact = LKONagarNigam.loadContactSyncFromDate(app,date);

		DeBug.ShowLog("From_server", "Extract List From Server"+mInsertContact.size());

		if(mInsertContact!=null)
		{
			for(int i = 0 ; i < mInsertContact.size() ; i++) 
			{
				InsertIntoContactSync mInsertIntoContactSync=mInsertContact.get(i);
				mInsertIntoContactSync.getContact_name();

				ArrayList<ContentProviderOperation> ops =   new ArrayList<ContentProviderOperation>();

				int rawContactID = ops.size();

				// Adding insert operation to operations list 
				// to insert a new raw contact in the table ContactsContract.RawContacts
				ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
						.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
						.withValue(RawContacts.ACCOUNT_NAME, null)
						.build());

				// Adding insert operation to operations list
				// to insert display name in the table ContactsContract.Data
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
						.withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
						.withValue(StructuredName.DISPLAY_NAME,mInsertIntoContactSync.getContact_name() )
						.build());

				// Adding insert operation to operations list
				// to insert Mobile Number in the table ContactsContract.Data
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
						.withValue(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
						.withValue(Phone.NUMBER, mInsertIntoContactSync.getCont_mob1())
						.withValue(Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
						.build());

				// Adding insert operation to operations list
				// to insert Work Email in the table ContactsContract.Data
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
						.withValue(ContactsContract.Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
						.withValue(Email.ADDRESS, mInsertIntoContactSync.getEmail_id())
						.withValue(Email.TYPE, Email.TYPE_WORK)
						.build());				

				try{
					// Executing all the insert operations as a single database transaction
					getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
					//Toast.makeText(ContactListMenuActivity.this, "Contact is successfully added", Toast.LENGTH_SHORT).show();


				}catch (RemoteException e) {					
					e.printStackTrace();
				}catch (OperationApplicationException e) 
				{
					e.printStackTrace();
				}
			}
			removeRepetedContacts();
		}

		else
		{
			mInsertContact=null;
		}

	}

}