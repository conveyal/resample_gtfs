package com.conveyal.resample;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Window {

	private String name;
	private int start;
	private int end;
	private ArrayList<String> windows;

	public static Window fromJSON(JSONObject jWindow) {
		Window ret = new Window();
		
		ret.name = jWindow.getString("name");
		ret.start = jWindow.getInt("start");
		ret.end = jWindow.getInt("end");
		ret.windows = new ArrayList<String>();
		JSONArray jServiceIds = jWindow.getJSONArray("service_ids");
		for(int i=0; i<jServiceIds.length(); i++){
			String service_id = jServiceIds.getString(i);
			ret.windows.add( service_id );
		}
		
		return ret;
	}
	
	public String toString(){
		return "<Window '"+this.name+"' "+this.start+"-"+this.end+" "+this.windows+">";
	}

}
