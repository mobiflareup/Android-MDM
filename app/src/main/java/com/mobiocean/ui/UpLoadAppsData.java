package com.mobiocean.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.mobiocean.mobidb.ApplicationInfoDB;
import com.mobiocean.mobidb.ApplicationInfoStruct;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.RestApiCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpLoadAppsData
{
	static ArrayList <String> ALL_APPS = new ArrayList<String>();
	static ArrayList <String> ALL_APPS_Name = new ArrayList<String>();



	public static int run(Context ctx) 
	{
		ApplicationInfoDB mApplicationInfoDatabaseHandler = ApplicationInfoDB.getInstance(ctx);
		//store ALL apps name and package names in sharedprefarance
		ArrayList<ApplicationInfoStruct> listApplicationInfoStructs = new ArrayList<ApplicationInfoStruct>();

	

		int allAppsUploaded = 0;
		String Pkg=null;
		int count=0;

		//	String stUploadUrlHeader=URL_HEADER+"/Parents/Instl_App.aspx";
		DeBug.ShowLog("EXP","UploadAppData uploading app info "+CallHelper.Ds.structLGC.bTimesCount);

		ALL_APPS.clear();
		ALL_APPS_Name.clear();

		if(ALL_APPS.isEmpty())
		{
			PackageManager manager = ctx.getPackageManager();

			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			//get list of all apps
			final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
			Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

			if (apps != null) 
			{
				count = apps.size();
				//CallHelper.Ds.structPC.TotalApps=count;
				//CallDetectService.callDetectService.editor.putInt("structPC.TotalApps", CallHelper.Ds.structPC.TotalApps);
				if(ALL_APPS.isEmpty())
				{
					List<String> DuplicateName=new ArrayList<String>();
					for (int i = 0; i < (count); i++) 
					{
						Pkg=null;
						ResolveInfo info = apps.get(i);
						Pkg=info.activityInfo.applicationInfo.packageName.toLowerCase();

						if(!ALL_APPS.contains(Pkg))
						{

							String CurrentAppName=info.loadLabel(manager).toString();


							//CallHelper.Ds.structFCC.All_APPSS.contains(Pkg);
							for(int k=0;k<ALL_APPS_Name.size();k++)
							{

								if(ALL_APPS_Name.get(k).equals(CurrentAppName))
								{
									//find out apps with similler names
									if(!DuplicateName.contains(CurrentAppName))
										DuplicateName.add(CurrentAppName);
									//CurrentAppName= CurrentAppName +" "+Pkg.substring(Pkg.lastIndexOf(".")+1);
								}
							}

							DeBug.ShowLog("APPS","index ss "+i+"  "+Pkg+" "+CurrentAppName );
							ALL_APPS.add(Pkg);
							ALL_APPS_Name.add(CurrentAppName);

						}

					}

					//assign different name to apps with similer name
					try {
						for (int j = 0; j < DuplicateName.size(); j++) {
							String DuplicateAppName = DuplicateName.get(j);
							for (int k = 0; k < ALL_APPS_Name.size(); k++) {

								if (ALL_APPS_Name.get(k).equalsIgnoreCase((DuplicateAppName))) {
									String APPNAME;
									String PKGNAME = ALL_APPS.get(k);
									APPNAME = ALL_APPS_Name.get(k);
									int firstIndex = PKGNAME.indexOf(".") + 1;
									int lstIndex = PKGNAME.indexOf(".", firstIndex);
									APPNAME = APPNAME + " " + PKGNAME.substring(firstIndex, lstIndex);
									ALL_APPS_Name.remove(k);
									ALL_APPS_Name.add(k, APPNAME);
								}
							}
						}
					}catch (Exception e){
						e.printStackTrace();
					}

					//get the list of apps which are needed to be uploaded
					/*for(int i=0;i<ALL_APPS.size();i++)
					{
						DeBug.ShowLog("APPS","index rr "+i+"  "+ALL_APPS.get(i)+" "+ALL_APPS_Name.get(i) );

						Pkg=ALL_APPS.get(i);
						if(CAT.ALWAYS.intValue()==CallHelper.Ds.structFCC.packageList.getCategoryIgnoreCase(Pkg) || CAT.HIDDEN.intValue()==CallHelper.Ds.structFCC.packageList.getCategoryIgnoreCase(Pkg))
						{
							DeBug.ShowLog("APPS"," A "+Pkg );					
						}
						else
						{
							CallHelper.Ds.structFCC.lbApps_Uploaded.add(false);
							CallHelper.Ds.structFCC.lstAppsTobeUploaded.add(ALL_APPS_Name.get(i));
							CallHelper.Ds.structFCC.bTotalApps_To_Be_Uploaded++;
						}
					}*/
				}

			}
			for(int index=0; index<ALL_APPS.size();index++)
			{
				ApplicationInfoStruct mApplicationInfoStruct = new ApplicationInfoStruct();

				mApplicationInfoStruct.setAppGroup(0);
				mApplicationInfoStruct.setAppIndex(index);
				mApplicationInfoStruct.setAppName(ALL_APPS_Name.get(index));
				mApplicationInfoStruct.setAppPackege(ALL_APPS.get(index));
				mApplicationInfoStruct.setIsAllowed(1);
				mApplicationInfoStruct.setIsAllowedNow(1);
				mApplicationInfoStruct.setIsInstalled(1);
				mApplicationInfoStruct.setIsSyncWithServer(0);
				mApplicationInfoStruct.setTimestamp(System.currentTimeMillis());
				listApplicationInfoStructs.add(mApplicationInfoStruct);

				DeBug.ShowLog("APPS","index "+index+"  "+ALL_APPS.get(index)+" "+ALL_APPS_Name.get(index) );
	
			}

			for(int i=0;i<listApplicationInfoStructs.size();i++)
			{
				mApplicationInfoDatabaseHandler.addData(listApplicationInfoStructs.get(i));
			}


		}


		//upload app info to server
		int i=0;						
	

		RestApiCall mRestApiCall= new RestApiCall();

		JSONObject json= new JSONObject();
		try {
		
			json.put("AppId",CallHelper.Ds.structPC.iStudId);
			

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		JSONArray appArray =new JSONArray();

		for(i=0 ;i<listApplicationInfoStructs.size();i++)
		{
			JSONObject jsonApp= new JSONObject();

			String  stAppName     = listApplicationInfoStructs.get(i).getAppName().trim();
			if(stAppName.equals("***"))
			{
				continue;
			}
			try {
				stAppName=URLEncoder.encode(stAppName, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


			stAppName = stAppName.replace("%C2%A0","");
			try 
			{
				stAppName = URLDecoder.decode(stAppName,"UTF-8");
			}
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int AppIndex=listApplicationInfoStructs.get(i).getAppIndex();
			try 
			{
				jsonApp.put("ChatApp",""+stAppName.trim());
				jsonApp.put("AppIndx",""+AppIndex);
				jsonApp.put("IsInstalled", listApplicationInfoStructs.get(i).getIsInstalled());
				jsonApp.put("LogDateTime", listApplicationInfoStructs.get(i).getTimestampForServer());
			
			}
			catch (JSONException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			appArray.put(jsonApp);	
		}

		try {
			json.put("chatAppLst",appArray);

			DeBug.ShowLog("AppList", ""+appArray);

			allAppsUploaded = Integer.parseInt(mRestApiCall.SetAppList(json));

			if(allAppsUploaded!=-1)
				mApplicationInfoDatabaseHandler.updateAllInstallationStatus(1);

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		DeBug.ShowLog("APPS","bTotalApps_To_Be_Uploaded  "+CallHelper.Ds.structFCC.bTotalApps_To_Be_Uploaded +"  "+count+" NAU "+CallHelper.Ds.structPC.bNo_of_App_Uploaded);
		return allAppsUploaded;
	}

	public static int update(Context ctx) 
	{

		ApplicationInfoDB mApplicationInfoDatabaseHandler = ApplicationInfoDB.getInstance(ctx);
		//store ALL apps name and package names in sharedprefarance
		ArrayList<ApplicationInfoStruct> listApplicationInfoStructs = new ArrayList<ApplicationInfoStruct>();


		
		int allAppsUploaded = 0;
			int count=0;

		//	String stUploadUrlHeader=URL_HEADER+"/Parents/Instl_App.aspx";
		DeBug.ShowLog("EXP","UploadAppData uploading app info "+CallHelper.Ds.structLGC.bTimesCount);


		ALL_APPS.clear();
		ALL_APPS_Name.clear();

		listApplicationInfoStructs = mApplicationInfoDatabaseHandler.getData();
		CallHelper.Ds.structFCC.lbApps_Uploaded.clear();
		CallHelper.Ds.structFCC.lstAppsTobeUploaded.clear();

		//	int ALL_APPS_Size=settings.getInt("structFCC.ALL_APPS_Size",CallHelper.Ds.structFCC.ALL_APPS.size());

		for(int i=0 ; i<listApplicationInfoStructs.size();i++)
		{
			ALL_APPS.add(listApplicationInfoStructs.get(i).getAppPackege());
			ALL_APPS_Name.add(listApplicationInfoStructs.get(i).getAppName());
		}


		if(!ALL_APPS.isEmpty())
		{
			
			RestApiCall mRestApiCall= new RestApiCall();

			JSONObject json= new JSONObject();

			int i=0;						
			int TotalApps=0;	
			try {
				json.put("Sim_No",CallHelper.Ds.structPC.stSIMSerialno);
				json.put("Stdnt_Id",CallHelper.Ds.structPC.iStudId);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


			JSONArray appArray =new JSONArray();

			for(i=0 ;i<TotalApps;i++)
			{
				JSONObject jsonApp= new JSONObject();

				String  stAppName     = ALL_APPS_Name.get(i).trim();
				if(stAppName.equals("***"))
				{
					continue;
				}
				String ss = stAppName;
				try {
					stAppName=URLEncoder.encode(stAppName, "UTF-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}


				stAppName = stAppName.replace("%C2%A0","");
				try 
				{
					stAppName= URLDecoder.decode(stAppName,"UTF-8");
				}
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}



				int AppIndex=ALL_APPS_Name.lastIndexOf(ss);
				try 
				{
					jsonApp.put("ChatApp",""+stAppName.trim());
					jsonApp.put("AppIndx",""+AppIndex);
				}
				catch (JSONException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				appArray.put(jsonApp);	
			}

			try {
				json.put("chatAppLst",appArray);
				allAppsUploaded = Integer.parseInt(mRestApiCall.SetAppList(json));
				if(allAppsUploaded!=-1)
					mApplicationInfoDatabaseHandler.updateAllInstallationStatus(1);

			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			DeBug.ShowLog("APPS","bTotalApps_To_Be_Uploaded  "+CallHelper.Ds.structFCC.bTotalApps_To_Be_Uploaded +"  "+count+" NAU "+CallHelper.Ds.structPC.bNo_of_App_Uploaded);
			return allAppsUploaded;
		}
		return allAppsUploaded;	
	}
}