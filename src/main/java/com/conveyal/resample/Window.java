package com.conveyal.resample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

public class Window {

	String name;
	private int start;
	private int end;
	private ArrayList<String> serviceIds;

	public static Window fromJSON(JSONObject jWindow) {
		Window ret = new Window();
		
		ret.name = jWindow.getString("name");
		ret.start = jWindow.getInt("start");
		ret.end = jWindow.getInt("end");
		ret.serviceIds = new ArrayList<String>();
		JSONArray jServiceIds = jWindow.getJSONArray("service_ids");
		for(int i=0; i<jServiceIds.length(); i++){
			String service_id = jServiceIds.getString(i);
			ret.serviceIds.add( service_id );
		}
		
		return ret;
	}
	
	public String toString(){
		return "<Window '"+this.name+"' "+this.start+"-"+this.end+" "+this.serviceIds+">";
	}

	public boolean includes(Trip trip, List<StopTime> list) {
		// check that this trip runs one of the window's service ids
		if(!this.runsOn(trip.getServiceId())){
			return false;
		}
		
		// check that the trip runs during the service window's hour span
		ArrayList<StopTime> al = new ArrayList<StopTime>(list);
		Collections.sort(al,new Comparator<StopTime>(){
			@Override
			public int compare(StopTime o1, StopTime o2) {
				return o1.getDepartureTime() - o2.getDepartureTime();
			}
		});
		
		double depart = al.get(0).getDepartureTime() / 3600.0;
		return depart >= this.start && depart < this.end;
	}

	private boolean runsOn(AgencyAndId serviceId) {
		return this.serviceIds.contains(serviceId.getId());
	}

}
