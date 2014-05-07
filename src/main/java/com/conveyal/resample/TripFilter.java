package com.conveyal.resample;

import org.json.JSONArray;
import org.onebusaway.gtfs.model.Trip;

public class TripFilter {

	private String propName;
	private String propVal;

	public static TripFilter fromJSON(JSONArray jsonArray) {
		TripFilter ret = new TripFilter();
		
		ret.propName = jsonArray.getString(0);
		ret.propVal = jsonArray.getString(1);
		
		if(!ret.propName.equals("trip_short_name")){
			throw new UnsupportedOperationException("filter on property '"+ret.propName+"' not supported");
		}
		
		return ret;
	}

	public boolean accepts(Trip trip) {
		if(propName.equals("trip_short_name")){
			return trip.getTripShortName().equals(propVal);
		} else {
			throw new UnsupportedOperationException("filter on property '"+propName+"' not supported");
		}
	}

}
