package com.conveyal.resample;

import org.json.JSONException;
import org.json.JSONObject;

public class Route {

	private boolean suppress;
	private String route;
	private JSONObject headwayData;
	private JSONObject tripsData;

	public static Route fromJSON(JSONObject jRoute) {
		Route ret = new Route();
		
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
		Object ret = headwayData.get(window_name);
		if(!JSONObject.NULL.equals(ret)){
			return (Double)ret;
		} else {
			return null;
		}
	}

	public Integer getTrips(String window_name) {
		Object ret = tripsData.get(window_name);
		if(!JSONObject.NULL.equals(ret)){
			return (Integer)ret;
		} else {
			return null;
		}	
	}

}
