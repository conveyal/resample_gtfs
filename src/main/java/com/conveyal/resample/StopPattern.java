package com.conveyal.resample;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.Stop;

public class StopPattern {
	List<Stop> stops = new ArrayList<Stop>();
	
	@Override
	public boolean equals(Object obj){
		if( !StopPattern.class.isInstance(obj) ){
			return false;
		}
		
		StopPattern other = (StopPattern)obj;
		if(other.stops.size()!=this.stops.size()){
			return false;
		}
		
		for(int i=0; i<this.stops.size(); i++){
			Stop aa = this.stops.get(i);
			Stop bb = other.stops.get(i);
			if(!aa.equals(bb)){
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int hashCode(){
		int out = 0;
		for(Stop stop : this.stops){
			out += stop.hashCode();
		}
		return out;
	}
}
