package com.goboomtown.chat;

//import com.goboomtown.api.BoomtownChat;
//import com.goboomtown.api.BoomtownChatMessage;
//import com.goboomtown.services.BoomtownServices;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SettingsStore {

	private static final String PREFS = "com.goboomtown.Settings.Prefs";
	private static final String SETTINGS_LOCATION = "com.goboomtown.LOCATION";
	private static final String SETTINGS_MONITORING = "com.goboomtown.MONITORING";
	private static final String SETTINGS_MONITORING_DELAY = "com.goboomtown.MONITORING_DELAY";
	private static final String SETTINGS_APP_STORE_URL = "com.goboomtown.APP_STORE_URL";
	private static final String SETTINGS_IS_DEBUGGING = "com.goboomtown.SETTINGS_IS_DEBUGGING";
	private static final String SETTINGS_CHATHISTORY = "com.goboomtown.CHATHISTORY";

	private static SettingsStore settingsStore;
	private SharedPreferences prefs;
	private Context context;

	private SettingsStore(Context context) {
		this.context = context.getApplicationContext();
		prefs = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
	}

	public static SettingsStore getInstance(Context context) {
		if (settingsStore == null) {
			settingsStore = new SettingsStore(context);
		}
		return settingsStore;
	}

	public long monitoringDelay(){
		// default is 24 hours - in seconds
		return prefs.getLong(SETTINGS_MONITORING_DELAY, 86400);
	}
	
	public void setMonitoringDelay(long delay) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(SETTINGS_MONITORING_DELAY, delay);
		editor.commit();
	}

	public boolean monitoringEnabled() {
//		boolean monitoring_enabled = BoomtownServices.monitoringRequested(context);
//		return prefs.getBoolean(SETTINGS_MONITORING, monitoring_enabled);
		return false;
	}

	public void setMonitoringEnabled(boolean enabled) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(SETTINGS_MONITORING, enabled);
		editor.commit();
	}

	public boolean currentLocationEnabled() {
		return prefs.getBoolean(SETTINGS_LOCATION, true);
	}

	public void setCurrentLocationEnabled(boolean enabled) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(SETTINGS_LOCATION, enabled);
		editor.commit();
	}

	public void setIsDebugging(boolean isDebugging) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(SETTINGS_IS_DEBUGGING, isDebugging);
		editor.commit();
	}

	public boolean isDebugging() {
		return prefs.getBoolean(SETTINGS_IS_DEBUGGING, false);
	}

	public ArrayList<BoomtownChatMessage> roomHistory(String roomJid)
	{
		BoomtownChatMessage message;
		ArrayList<BoomtownChatMessage> history = new ArrayList<BoomtownChatMessage>();
		String historyString = prefs.getString(SETTINGS_CHATHISTORY+roomJid, "{}");
		try {
			JSONArray jsonArray = new JSONArray(historyString);
			ArrayList<String> historyEntries = (ArrayList<String>) JSONHelper.toList(jsonArray);
			for ( String historyEntry : historyEntries ) {
				message = new BoomtownChatMessage();
				message.populateFromString(historyEntry);
				history.add(message);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return history;
	}


	public void setRoomHistory(String roomJid, ArrayList<BoomtownChatMessage> history)
	{
		SharedPreferences.Editor ed = prefs.edit();
		Object jsonHistory = null;
		try {
			jsonHistory = JSONHelper.toJSON(history);
			ed.putString(SETTINGS_CHATHISTORY+roomJid, jsonHistory.toString());

			ed.commit();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
