package com.mobiocean.util;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CalendarSyncServer {

	@SerializedName("AppId")
	private String AppId;

	@SerializedName("calendarList")
	private ArrayList<ItemList> calendarList;

	@SerializedName("mItemList")
	private ItemList mItemList;
	
	public class ItemList
	{
		@SerializedName("EventName")
		String EventName;

		@SerializedName("Location")
		String Location;

		@SerializedName("StartDateTime")
		String StartDateTime;

		@SerializedName("EndDateTime")
		String EndDateTime;

		@SerializedName("Repetition")
		String Repetition;

		@SerializedName("Description")
		String Description;

		@SerializedName("SyncDateTime")
		String SyncDateTime;
		
		public String getEventName() {
			return EventName;
		}
		public void setEventName(String eventName) {
			EventName = eventName;
		}
		public String getLocation() {
			return Location;
		}
		public void setLocation(String location) {
			Location = location;
		}
		public String getStartDateTime() {
			return StartDateTime;
		}
		public void setStartDateTime(String startDateTime) {
			StartDateTime = startDateTime;
		}
		public String getEndDateTime() {
			return EndDateTime;
		}
		public void setEndDateTime(String endDateTime) {
			EndDateTime = endDateTime;
		}
		public String getRepetition() {
			return Repetition;
		}
		public void setRepetition(String repetition) {
			Repetition = repetition;
		}
		public String getDescription() {
			return Description;
		}
		public void setDescription(String description) {
			Description = description;
		}
		public String getSyncDateTime() {
			return SyncDateTime;
		}
		public void setSyncDateTime(String syncDateTime) {
			SyncDateTime = syncDateTime;
		}
		
		
	}


	public String getAppId() {
		return AppId;
	}
	public void setAppId(String appId) {
		AppId = appId;
	}
	public ArrayList<ItemList> getCalendarList() {
		return calendarList;
	}
	public ArrayList<ItemList> getCalendarListInit() {
		return calendarList = new ArrayList<ItemList>();
	}

	public void setCalendarList(ArrayList<ItemList> calendarList) {
		this.calendarList = calendarList;
	}
	
	public void addCalendarList(ItemList calendarList) {
		this.calendarList.add(calendarList);
	}

	public ItemList getmItemList() {
		return mItemList;
	}
	public ItemList getmItemListInstance() {
		return mItemList = new ItemList();
	}

	public void setmItemList(ItemList mItemList) {
		this.mItemList = mItemList;
	}
	
	
}
