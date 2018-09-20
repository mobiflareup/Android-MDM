package com.mobiocean.ui;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.mobiocean.service.CallDetectService;
import com.mobiocean.util.CAT;

/**
 *Test HandSet Model     = s01
 *ADT Pakage Version     = 21.0.1.201212060302
 *Eclipse Platform       = 4.2.1.v20120814
 *Date					= October 17,2013
 *Functionality			= To categorize the Application of Android Accordingly(Game-App, Chat-App, Allowed-App)			
 *Android version		= 2.3.6 [Gingerbread (API level 10)]
 */

public class PackageList extends ArrayList<ArrayList<String>> 
{
	private static final long serialVersionUID = 1L;
	private static final String SHARED_PREF_TAG = "structFCC.packageList";

	protected static final String PREFS_NAME = "MyPrefsFile";
	public static SharedPreferences settings;
	public static SharedPreferences.Editor editor;

	int size=0;
	public PackageList()
	{
		initialize();
	}
	public PackageList(Context context, int size) 
	{

		this.size= size;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
		else
			settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		editor = settings.edit();

		initialize();
	}
	//will use
	public boolean addPkg(int group, String pkgName) 
	{
		removePkgIgnoreCase(pkgName);
		boolean result = get(group).add(pkgName);
		//	saveCategoryToSharedPreferences(CallDetectService.callDetectService.editor,cat);
		return result;
	}

	//will use
	public void addApplicationListToGroup(int group, ArrayList<String> listPkgName) 
	{

		get(group).addAll(listPkgName);
		//	saveCategoryToSharedPreferences(CallDetectService.callDetectService.editor,cat);

	}

	//will use
	public boolean addPkgWithoutSave(int group, String pkgName) 
	{
		removePkgIgnoreCase(pkgName);
		boolean result = get(group).add(pkgName);
		return result;
	}
	//use
	private void initialize() {
		clear();
		for(int i=0;i<size;i++)
			add(new ArrayList<String>());
	}

	//use
	public int containedInCategoryIgnoreCase(int group, String pkgName) 
	{			
		return this.get(group).indexOf(pkgName);	
	}

	//use
	public int getCategoryIgnoreCase(String pkgName) {
		for(int i=0;i<this.size;i++)			
			if(this.get(i).indexOf(pkgName)!=-1)
				return i;
		return -1;
	}
	//use
	public boolean containsIgnoreCase(String pkgName) {

			for(int j=0; j<size;j++)
				if(this.get(j).contains(pkgName))
					return true;
		
		return false;
	}



	public ArrayList<String> getCategory(CAT cat) {
		return get(cat.intValue());
	}

	public boolean isControlledIgnoreCase(String pkgName) {
		int size;
		for(CAT cat : CAT.values()){
			if(cat==CAT.HIDDEN||cat==CAT.ALWAYS)
				continue;
			size = sizeOfCat(cat);
			for(int j=0; j<size;j++)
				if(getPkg(cat,j).equalsIgnoreCase(pkgName))
					return true;
		}
		return false;
	}

	/*	public int isGameIgnoreCase(String pkgName) {
		return containedInCategoryIgnoreCase(CAT.GAME,pkgName);
	}*/

	public boolean isVisibleIgnoreCase(String pkgName) {
		int size;
		for(CAT cat : CAT.values()){
			if(cat==CAT.HIDDEN)
				continue;
			size = sizeOfCat(cat);
			for(int j=0; j<size;j++)
				if(getPkg(cat,j).equalsIgnoreCase(pkgName))
					return true;
		}
		return false;
	}

	public String getPkg(CAT cat,int index) {
		return get(cat.intValue()).get(index);
	}

	public boolean removePkg(String pkgName) {
		int size;
		for(CAT cat : CAT.values()){
			size = sizeOfCat(cat);
			for(int j=0; j<size;j++)
				if(getPkg(cat,j).equals(pkgName)) {
					get(cat.intValue()).remove(j);
					saveCategoryToSharedPreferences(CallDetectService.callDetectService.editor,cat);
					return true;
				}
		}
		return false;
	}

	public boolean removePkg(String pkgName, CAT cat) {
		if(get(cat.intValue()).remove(pkgName)==false)
			return false;
		saveCategoryToSharedPreferences(CallDetectService.callDetectService.editor,cat);
		return true;
	}

	public boolean removePkgIgnoreCase(String pkgName) 
	{

		for(int j=0; j<this.size;j++)
			if(this.get(j).contains(pkgName)) 
			{
				get(j).remove(pkgName);
				return true;
			}

		return false;
	}

	public boolean removePkgIgnoreCase(String pkgName, CAT cat) {
		int size = sizeOfCat(cat);
		for(int j=0; j<size;j++)
			if(getPkg(cat,j).equalsIgnoreCase(pkgName)) {
				get(cat.intValue()).remove(j);
				saveCategoryToSharedPreferences(CallDetectService.callDetectService.editor,cat);
				return true;
			}
		return false;
	}

	public boolean removePkgIgnoreCaseIgnoreHidden(String pkgName) {
		for(CAT cat : CAT.values()){
			if(cat==CAT.HIDDEN)
				continue;
			int size=sizeOfCat(cat);
			for(int j=0; j<size;j++)
				if(getPkg(cat,j).equalsIgnoreCase(pkgName)){
					get(cat.intValue()).remove(j);
					saveCategoryToSharedPreferences(CallDetectService.callDetectService.editor,cat);
					return true;
				}
		}
		return false;
	}

	public int sizeOfCat(CAT cat){
		return get(cat.intValue()).size();
	}

	public void saveCategoryToSharedPreferences(SharedPreferences.Editor editor,CAT cat) {
		int size = get(cat.intValue()).size();
		for(int j=0;j<size;j++)
			editor.putString(SHARED_PREF_TAG+cat.name()+j,getPkg(cat,j));
		editor.putInt(SHARED_PREF_TAG+cat.name()+"size", get(cat.intValue()).size());
		editor.commit();
	}

	public void saveToSharedPreferences(SharedPreferences.Editor editor) {
		for(CAT cat : CAT.values()){
			int size = sizeOfCat(cat);
			for(int j=0;j<size;j++)
				editor.putString(SHARED_PREF_TAG+cat.name()+j,getPkg(cat,j));
			editor.putInt(SHARED_PREF_TAG+cat.name()+"size", size);
		}
		editor.commit();
	}

	/*	public void readFromSharedPreferences(SharedPreferences settings) {
		initialize();
		for(CAT cat : CAT.values()){
			int size=settings.getInt(SHARED_PREF_TAG+cat.name()+"size", 0);
			for(int j=0;j<size;j++)
				addPkgWithoutSave(cat,settings.getString(SHARED_PREF_TAG+cat.name()+j, null));
		}
	}*/
}