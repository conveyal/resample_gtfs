package com.conveyal.resample;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceLevel {

	boolean suppress;
	String route;
	private JSONObject headwayData;
	private JSONObject tripsData;
	public List<TripFilter> tripFilters=new ArrayList<TripFilter>();

	public static ServiceLevel fromJSON(JSONObject jRoute) {
		ServiceLevel ret = new ServiceLevel();
		
		try{
			ret.suppress = jRoute.getBoolean("suppress");
		} catch (JSONException ex){
			ret.suppress = false;
		}
		ret.route = jRoute.getString("route");
		ret.headwayData = jRoute.getJSONObject("headways");
		ret.tripsData = jRoute.getJSONObject("trips");
		
		try{
			JSONArray jTripFilters = jRoute.getJSONArray("trip_filters");
			for(int i=0; i<jTripFilters.length(); i++){
				TripFilter tf = TripFilter.fromJSON( jTripFilters.getJSONArray(i) );
				ret.tripFilters.add(tf);
			}
		} catch (JSONException ex){
			// no trip filters. that's fine.
		}
		
		return ret;
	}
	
	public String toString(){
		return "<Route "+this.route+">";
	}

	public Double getHeadway(String window_name) {
		try{
			Object ret = headwayData.get(window_name);
			if(!JSONObject.NULL.equals(ret)){
				return (Double)ret;
			} else {
				return null;
			}
		} catch(JSONException ex){
			return null;
		}

	}

	public Integer getTrips(String window_name) {
		try{
			Object ret = tripsData.get(window_name);
			if(!JSONObject.NULL.equals(ret)){
				return (Integer)ret;
			} else {
				return null;
			}	
		} catch (JSONException ex) {
			return null;
		}

	}

}
