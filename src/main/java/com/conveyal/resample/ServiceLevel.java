package com.conveyal.resample;

import org.json.JSONException;
import org.json.JSONObject;

public class ServiceLevel {

	private boolean suppress;
	String route;
	private JSONObject headwayData;
	private JSONObject tripsData;

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
