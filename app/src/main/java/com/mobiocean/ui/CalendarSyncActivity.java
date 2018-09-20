package com.mobiocean.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobiocean.R;
import com.mobiocean.util.CalendarSyncServer;
import com.mobiocean.util.CalendarSyncServer.ItemList;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.LKONagarNigam;
import com.mobiocean.util.LKONagarNigam.CalendarSync;
import com.mobiocean.util.RestApiCall;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;


public class CalendarSyncActivity extends Activity implements OnClickListener {

	Button bgetCalenderEvents;
	Button bdownloadCalenderEvents;
	Spinner spinnerCalendar;
	Button submitcalendar;
	static String eventDate = "";
	Handler handler;

	ArrayList<String> spinnerCalendarDateArray = new ArrayList<String>();
	ArrayAdapter<String> spinnerArrayAdapter;

	static Cursor calCursor;

	static boolean isTaskRunning = false;
	static CalendarSyncOperation mCalendarSyncOperation ;

	static ArrayList<CalendarSync> mCalendarSyncList = new ArrayList<CalendarSync>();


	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.synccalender_details);

		bgetCalenderEvents=(Button)findViewById(R.id.b_getCalenderEvents); 
		bdownloadCalenderEvents = (Button)findViewById(R.id.b_downloadCalenderEvents);
		spinnerCalendar = (Spinner)findViewById(R.id.spinnerCalendar);
		submitcalendar = (Button)findViewById(R.id.btn_submitcalendar);

		submitcalendar.setVisibility(Button.GONE);
		spinnerCalendar.setVisibility(Spinner.GONE);

		//  bVisitingcard.setOnClickListener(this);
		bgetCalenderEvents.setOnClickListener(this);
		bdownloadCalenderEvents.setOnClickListener(this);
		submitcalendar.setOnClickListener(this);
		handler = new Handler();

	}

	@Override
	public void onClick(View v) {

		switch(v.getId())
		{

		case(R.id.b_getCalenderEvents):

			mCalendarSyncOperation = new CalendarSyncOperation();
		mCalendarSyncOperation.execute("");

		break;

		case(R.id.b_downloadCalenderEvents):

			submitcalendar.setVisibility(Button.VISIBLE);
		spinnerCalendar.setVisibility(Spinner.VISIBLE);

		new ExtractCalendarEventFromServer().execute();

		break;

		case(R.id.btn_submitcalendar):

			new GetCalendarFromServer().execute();

		break;

		default:
			break;
		}
	}


	private class CalendarSyncOperation extends AsyncTask<String, String, String> 
	{
		protected  ProgressDialog ringProgressDialogLongTask;
		//	CharSequence ErrorMesg = "Please Check the Information you have Inserted OR \nCheck your Internet Connection";
		String result = "";

		@Override
		protected void onPreExecute() 
		{			
			isTaskRunning = true;
			ringProgressDialogLongTask = new ProgressDialog(CalendarSyncActivity.this);
			ringProgressDialogLongTask.setMessage("Downloading calendar. Please wait...");
			ringProgressDialogLongTask.setIndeterminate(false);
			ringProgressDialogLongTask.setMax(100);
			ringProgressDialogLongTask.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			ringProgressDialogLongTask.setCancelable(false);
			ringProgressDialogLongTask.show();
			//  ringProgressDialogLongTask  =  ProgressDialog.show(MainMenuActivity.this, "Please wait ...", "Loading ...", true);
			//	ringProgressDialogLongTask.setCancelable(false);	
		}

		@Override
		protected String doInBackground(String... params) 
		{
			//while(!intilizationComplited);

			DeBug.ShowLog("Test","IN thread  "+CallHelper.Ds.structPC.iStudId);
			int j = 0;
			int K = 0;

			RestApiCall mRestApiCall= new RestApiCall();

			String  CurrentTime = ""+System.currentTimeMillis();
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(Long.parseLong(CurrentTime));
			DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
			String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();

			if(!CallHelper.Ds.structPC.iStudId.equals(""))
			{  
				//readCalendar(getApplicationContext());

				ContentResolver contentResolver = getApplicationContext().getContentResolver();

				// Fetch a list of all calendars synced with the device, their display names and whether the

				String[] projection = new String[]{Calendars._ID, Calendars.NAME, Calendars.ACCOUNT_NAME, Calendars.ACCOUNT_TYPE};

				try {
					calCursor = contentResolver.query(Calendars.CONTENT_URI, projection, Calendars.VISIBLE + " = 1",
							null, Calendars._ID + " ASC");
				}catch (SecurityException ignore){}
				if (calCursor.moveToFirst()) {}

				HashSet<String> calendarIds = new HashSet<String>();
				try
				{
					System.out.println("Count="+calCursor.getCount());
					if(calCursor.getCount() > 0)
					{
						System.out.println("the control is just inside of the cursor.count loop");
						while (calCursor.moveToNext()) {

							String _id = calCursor.getString(0);
							String displayName = calCursor.getString(1);
							Boolean selected = !calCursor.getString(2).equals("0");

							System.out.println("Id: " + _id + " Display Name: " + displayName + " Selected: " + selected);
							calendarIds.add(_id);
						}
					}
				}
				catch(AssertionError ex)
				{
					ex.printStackTrace();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

				
				CalendarSyncServer cc = new CalendarSyncServer();
		    	cc.getCalendarListInit();
				// For each calendar, display all the events from the previous week to the end of next week.        
				//for (String id : calendarIds) 
				{

					// fetching all events from calender
					Cursor cursor = getApplicationContext().getContentResolver().
							query(Uri.parse("content://com.android.calendar/events"), 
									new String[]{"_id", "title", "description", "dtstart", "dtend", "eventLocation"}, null, null, null);         
					
					String add = null;
					cursor.moveToFirst();
					String[] CalNames = new String[cursor.getCount()];
					int[] CalIds = new int[cursor.getCount()];

					mCalendarSyncList.clear();
				
			    	
					for (int i = 0; i < CalNames.length; i++) 
					{
						//  CalIds[i] = cursor.getInt(0);
						//   CalNames[i] = "Event"+cursor.getInt(0)+": \nTitle: "+ cursor.getString(1)+"\nDescription: "+cursor.getString(2)+"\nStart Date: "+new Date(cursor.getLong(3))+"\nEnd Date : "+new Date(cursor.getLong(4))+"\nLocation : "+cursor.getString(5);
						ItemList mItemList = 	cc.getmItemListInstance();
						
						CalendarSync mCalendarSync = new LKONagarNigam().new CalendarSync();

						mCalendarSync.Repetition  = ""+cursor.getInt(0);
						mCalendarSync.EventName  = ""+cursor.getString(1);
						mCalendarSync.Description  = ""+cursor.getString(2);
						mCalendarSync.Location  = ""+cursor.getString(5);
						mCalendarSync.LogDate = SMSTimeStamp;

						mItemList.setRepetition(mCalendarSync.Repetition);
						mItemList.setEventName(mCalendarSync.EventName);
						mItemList.setDescription(mCalendarSync.Description);
						mItemList.setLocation(mCalendarSync.Location);
						mItemList.setSyncDateTime(SMSTimeStamp);
						
						Calendar calendar1 = Calendar.getInstance();
						calendar1.setTimeInMillis(cursor.getLong(3));
						DateFormat formatter11 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
						String SMSTimeStamp1 = formatter11.format(calendar1.getTime()).toString();

						Calendar calendar11 = Calendar.getInstance();
						calendar11.setTimeInMillis(cursor.getLong(4));
						DateFormat formatter111 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
						String SMSTimeStamp11 = formatter111.format(calendar11.getTime()).toString();

						mCalendarSync.StartDateTime  = SMSTimeStamp1;
						mCalendarSync.EndDateTime  = SMSTimeStamp11;

						mItemList.setStartDateTime(mCalendarSync.StartDateTime);
						mItemList.setEndDateTime(mCalendarSync.EndDateTime );
						
						mCalendarSyncList.add(mCalendarSync);
						cc.addCalendarList(mItemList);
						
						publishProgress("" + (int) ((j++ * 100) / cursor.getCount()));
						cursor.moveToNext();
					}
					cc.setAppId(CallHelper.Ds.structPC.iStudId);
					if(cursor.getCount()>0)
					publishProgress("" + (int) ((j++ * 100) / cursor.getCount()));

					cursor.close();
				
				}
				publishProgress("Upload");
				if(cc.getCalendarList().isEmpty())
					result = "No events";
				else
				result = mRestApiCall.postCalendarsEvents(getApp(), cc);
				
			/*	JSONArray jsonArrayCalendar = new JSONArray();	

				for( K=0 ;K<mCalendarSyncList.size();K++)
				{
					JSONObject jsonApp = new JSONObject();
					CalendarSync mCalendarSyncInfo = new LKONagarNigam().new CalendarSync();
					mCalendarSyncInfo = mCalendarSyncList.get(K);

					try 
					{	
						jsonApp.put("location",mCalendarSyncInfo.Location);
						jsonApp.put("startDateTime",mCalendarSyncInfo.StartDateTime);
						jsonApp.put("endDateTime",mCalendarSyncInfo.EndDateTime);
						jsonApp.put("repetition",mCalendarSyncInfo.Repetition );
						jsonApp.put("eventName",mCalendarSyncInfo.EventName );
						jsonApp.put("description",mCalendarSyncInfo.Description );
					}
					catch (JSONException e) 
					{
						e.printStackTrace();
					}

					jsonArrayCalendar.put(jsonApp);	

				}

				JSONObject jsonObjCalendar= new JSONObject();
				try {
					jsonObjCalendar.put("androidAppId", CallHelper.Ds.structPC.iStudId);
					jsonObjCalendar.put("logDate", SMSTimeStamp);
					jsonObjCalendar.put("slotNo", 1);
					jsonObjCalendar.put("totalSlot", 1);
					jsonObjCalendar.put("CalenderList", jsonArrayCalendar);

				} catch (JSONException e) {
					e.printStackTrace();
				}

				result =Integer.parseInt(mRestApiCall.uploadCalendarEventInfo(getBaseContext(), jsonObjCalendar));
				if(mCalendarSyncList.size()>0)*/
				publishProgress("" + (int) ((100 * 100) / 100));

			//	DeBug.ShowLog("calenDar", "jsonObjCalendar " + jsonObjCalendar+ "calendarInfo " +result);

			}

			return result;
		}

		@Override
		protected void onProgressUpdate(String... progress) 
		{
			if(progress[0].equalsIgnoreCase("Upload"))
				ringProgressDialogLongTask.setMessage("Uploading Calendar Events. Please wait...");
			else		
				ringProgressDialogLongTask.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String result) 
		{
			ringProgressDialogLongTask.dismiss();
			if(result==null)
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(CalendarSyncActivity.this, "Upload faild please Try again.", Toast.LENGTH_SHORT).show();
					}
				});
			else if(result.equals("No events"))
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(CalendarSyncActivity.this, "No Event found.", Toast.LENGTH_SHORT).show();
					}
				});

		
		}

	}

	public ArrayList<CalendarSync> extractContacts(MobiApplication app)
	{
		mCalendarSyncList = new ArrayList<CalendarSync>();
		mCalendarSyncList = LKONagarNigam.loadCalendarDateSync(app);

		DeBug.ShowLog("Extract List From", "Extract List From Server"+mCalendarSyncList.size());

		if(mCalendarSyncList!=null && !mCalendarSyncList.equals(""))
		{
			return mCalendarSyncList;
		}
		else
		{
			return null;
		}
	}

	public class ExtractCalendarEventFromServer extends AsyncTask<Void, Void, ArrayList<CalendarSync>> 
	{
		protected  ProgressDialog ringProgressDialogLongTask;
		int startTime, endTime;
		long milliStartTime, millisEndTime;

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */

		@Override
		protected void onPreExecute() 
		{
			ringProgressDialogLongTask  =  ProgressDialog.show(CalendarSyncActivity.this, "Please wait ...", "Loading ...", true);
			ringProgressDialogLongTask.setCancelable(false);
			super.onPreExecute();
		}

		@Override
		protected ArrayList<CalendarSync> doInBackground(Void... params) 
		{
			milliStartTime = System.currentTimeMillis();
			//startTime=	TimeUnit.MILLISECONDS.toMinutes(milliStartTime);
			startTime = (int) (((milliStartTime / 1000) / 60) % 60);
			DeBug.ShowLog("TimeTaken","StartTime  "+startTime+" minutes "+milliStartTime+" millisec");
			extractContacts(getApp());

			return extractContacts(getApp());

		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */

		@Override
		protected void onPostExecute(ArrayList<CalendarSync> result) 
		{
			// TODO Auto-generated method stub

			ringProgressDialogLongTask.dismiss();
			//super.onPostExecute(result);
			millisEndTime = System.currentTimeMillis();
			endTime = (int) (((millisEndTime / 1000) / 60) % 60);
			//endTime=	TimeUnit.MILLISECONDS.toMinutes(millisEndTime);
			DeBug.ShowLog("TimeTaken","EndTime  "+endTime+" minutes"+millisEndTime+"millisec");
			DeBug.ShowLog("TimeTaken","That took " +(endTime - startTime)+" minutes  "+ (millisEndTime-milliStartTime) + " milliseconds");


			if(result!=null)
			{	
				spinnerCalendarDateArray.clear();
				spinnerCalendarDateArray.add("__Select__");

				for(int i=0; i<mCalendarSyncList.size(); i++)
				{
					spinnerCalendarDateArray.add(mCalendarSyncList.get(i).SyncDateTime);
				}

				spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.customspinner_item, spinnerCalendarDateArray);
				spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_selector);
				spinnerCalendar.setAdapter(spinnerArrayAdapter);
				spinnerArrayAdapter.notifyDataSetChanged();
				spinnerCalendar.setOnItemSelectedListener(new OnItemSelectedListener(){

					@Override
					public void onItemSelected(AdapterView<?> parent, View view,
							int position, long id) {

						if(spinnerCalendar.getSelectedItemPosition()==0)
						{
							submitcalendar.setClickable(false);		
						}
						else{
							submitcalendar.setClickable(true);	
							eventDate = spinnerCalendar.getSelectedItem().toString();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}});
			}
		}
	}

	private  MobiApplication getApp() 
	{
		return (MobiApplication) getApplication();
	}

	public class GetCalendarFromServer extends AsyncTask<Void, String, String> 
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
				ringProgressDialogLongTask = new ProgressDialog(CalendarSyncActivity.this);
				ringProgressDialogLongTask.setMessage("Downloading Calendar. Please wait...");
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
		protected String doInBackground(Void... params) 
		{
			try {
				milliStartTime = System.currentTimeMillis();
				int j = 0;

				startTime = (int) (((milliStartTime / 1000) / 60) % 60);
				DeBug.ShowLog("TimeTaken", "StartTime  " + startTime + " minutes " + milliStartTime + " millisec");

				// extractdatafromserver(getApp());

				ArrayList<CalendarSync> mCalendarSyncList = new ArrayList<CalendarSync>();

				mCalendarSyncList = LKONagarNigam.loadCalendarSyncFromDate(getApp(), eventDate);
				DeBug.ShowLog("CalendarDownLoad", "1  " + mCalendarSyncList.size());

				if (mCalendarSyncList != null) {
					for (int i = 0; i < mCalendarSyncList.size(); i++) {
						CalendarSync mCalendarSync = mCalendarSyncList.get(i);
						//	mCalendarSync.Repetition.toString();
//					if(mCalendarSync.EventName.contains("Gourav"))
//					{
//						DeBug.ShowLog("Caledar", "Check Why Gourav not Added in Calendar");
//					}

						ContentResolver cr = CalendarSyncActivity.this.getContentResolver();
						ContentValues values = new ContentValues();
						values.put(CalendarContract.Events.DTSTART, converInJagadishFormate(mCalendarSync.StartDateTime));
						values.put(CalendarContract.Events.DTEND,  converInJagadishFormate(mCalendarSync.EndDateTime));
//						values.put(CalendarContract.Events.DTSTART, mCalendarSync.StartDateTime);
//						values.put(CalendarContract.Events.DTEND, mCalendarSync.EndDateTime);
//						values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
//						values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
//						values.put(Calendars.VISIBLE, 1);
						values.put(CalendarContract.Events.TITLE, mCalendarSync.EventName);
						values.put(CalendarContract.Events.DESCRIPTION, mCalendarSync.Description);
						values.put(CalendarContract.Events.CALENDAR_ID, mCalendarSync.Repetition);
						values.put(CalendarContract.Events.EVENT_LOCATION, mCalendarSync.Location);
						values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
						values.put(CalendarContract.Events.EVENT_COLOR, getApplicationContext().getResources().getColor(R.color.profilebackground));

						String eventId = mCalendarSync.Repetition;
						Uri uri = null;
						boolean add = isEventInCal(CalendarSyncActivity.this, eventId);

						if (!add) {
							DeBug.ShowLog("CalendarDownLoad", "3  " + i);
							try {
								uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
								pushAppointmentsToCalender(CalendarSyncActivity.this, mCalendarSync.EventName, mCalendarSync.Description, mCalendarSync.Location, 1, converInJagadishFormate(mCalendarSync.StartDateTime), false, false);
							} catch (SecurityException e) {
								e.printStackTrace();
							}
							long eventID = Long.parseLong(uri.getLastPathSegment());
							DeBug.ShowLog("CalendarDownLoad", "4 " + eventID);
						}
						publishProgress("" + (int) ((i * 100) / mCalendarSyncList.size()));
					}
					publishProgress("Remove");
				} else {
					mCalendarSyncList = null;
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... progress) 
		{
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
				ringProgressDialogLongTask.dismiss();
				//super.onPostExecute(result);
				millisEndTime = System.currentTimeMillis();
				endTime = (int) (((millisEndTime / 1000) / 60) % 60);
				//endTime = TimeUnit.MILLISECONDS.toMinutes(millisEndTime);
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(CalendarSyncActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
					}
				});
				DeBug.ShowLog("TimeTaken", "EndTime  " + endTime + " minutes" + millisEndTime + "millisec");
				DeBug.ShowLog("TimeTaken", "That took " + (endTime - startTime) + " minutes  " + (millisEndTime - milliStartTime) + " milliseconds");
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public boolean isEventInCal(Context context, String cal_meeting_id) {
		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://com.android.calendar/events"),new String[] { "_id" }, " _id = ? ", new String[] { cal_meeting_id }, null);
		if (cursor.moveToFirst()) 
		{
			return true;
		}	     
		return false;
	}


	public long converInJagadishFormate(String time)
	{	
		long milliseconds = 0;
		try {
			//String strCurrentDate = "Wed, 18 Apr 2012 07:55:29 +0000";
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
			Date newDate = null;
			newDate = format.parse(time);
			milliseconds = newDate.getTime();

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return milliseconds;
	}


	/** user defined method to delete the event based on id*/
	private int deleteEventFromCalendar(ContentResolver  cr,String id)
	{
		Uri eventUri = Uri.parse("content://com.android.calendar/events");  // or
		Uri deleteUri = null;

		deleteUri = ContentUris.withAppendedId(eventUri, Long.parseLong(id));
		int rows = cr.delete(deleteUri, null, null);
		return rows;
	}

	public static long pushAppointmentsToCalender(Activity curActivity, String title, String addInfo, String place, int status, long startDate, boolean needReminder, boolean needMailService) {
		/***************** Event: note(without alert) *******************/

		String eventUriString = "content://com.android.calendar/events";
		ContentValues eventValues = new ContentValues();

		eventValues.put("calendar_id", 1); // id, We need to choose from
		// our mobile for primary
		// its 1
		eventValues.put("title", title);
		eventValues.put("description", addInfo);
		eventValues.put("eventLocation", place);

		long endDate = startDate + 1000 * 60 * 60; // For next 1hr

		eventValues.put("dtstart", startDate);
		eventValues.put("dtend", endDate);

		// values.put("allDay", 1); //If it is bithday alarm or such
		// kind (which should remind me for whole day) 0 for false, 1
		// for true
		eventValues.put("eventStatus", status); // This information is
		// sufficient for most
		// entries tentative (0),
		// confirmed (1) or canceled
		// (2):
		eventValues.put("eventTimezone", "UTC/GMT +5:30");
   /*Comment below visibility and transparency  column to avoid java.lang.IllegalArgumentException column visibility is invalid error */

    /*eventValues.put("visibility", 3); // visibility to default (0),
                                        // confidential (1), private
                                        // (2), or public (3):
    eventValues.put("transparency", 0); // You can control whether
                                        // an event consumes time
                                        // opaque (0) or transparent
                                        // (1).
      */
		eventValues.put("hasAlarm", 1); // 0 for false, 1 for true

		Uri eventUri = curActivity.getApplicationContext().getContentResolver().insert(Uri.parse(eventUriString), eventValues);
		long eventID = Long.parseLong(eventUri.getLastPathSegment());

		if (needReminder) {
			/***************** Event: Reminder(with alert) Adding reminder to event *******************/

			String reminderUriString = "content://com.android.calendar/reminders";

			ContentValues reminderValues = new ContentValues();

			reminderValues.put("event_id", eventID);
			reminderValues.put("minutes", 5); // Default value of the
			// system. Minutes is a
			// integer
			reminderValues.put("method", 1); // Alert Methods: Default(0),
			// Alert(1), Email(2),
			// SMS(3)

			Uri reminderUri = curActivity.getApplicationContext().getContentResolver().insert(Uri.parse(reminderUriString), reminderValues);
		}

		/***************** Event: Meeting(without alert) Adding Attendies to the meeting *******************/

		if (needMailService) {
			String attendeuesesUriString = "content://com.android.calendar/attendees";

			/********
			 * To add multiple attendees need to insert ContentValues multiple
			 * times
			 ***********/
			ContentValues attendeesValues = new ContentValues();

			attendeesValues.put("event_id", eventID);
			attendeesValues.put("attendeeName", "xxxxx"); // Attendees name
			attendeesValues.put("attendeeEmail", "gingerboxmobility@gmail.com");// Attendee
			// E
			// mail
			// id
			attendeesValues.put("attendeeRelationship", 0); // Relationship_Attendee(1),
			// Relationship_None(0),
			// Organizer(2),
			// Performer(3),
			// Speaker(4)
			attendeesValues.put("attendeeType", 0); // None(0), Optional(1),
			// Required(2), Resource(3)
			attendeesValues.put("attendeeStatus", 0); // NOne(0), Accepted(1),
			// Decline(2),
			// Invited(3),
			// Tentative(4)

			Uri attendeuesesUri = curActivity.getApplicationContext().getContentResolver().insert(Uri.parse(attendeuesesUriString), attendeesValues);
		}

		return eventID;

	}
}