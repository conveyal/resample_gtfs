package com.conveyal.resample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class GTFSService {

	private String filename;
	private GtfsRelationalDaoImpl store;

	public GTFSService(String gtfs_filename) {
		this.filename = gtfs_filename;
		this.store = null;
	}

	public void read() throws IOException {
	    // read in the GTFS
	    GtfsReader reader = new GtfsReader();
	    reader.setInputLocation(new File(filename));
	    
	    store = new GtfsRelationalDaoImpl();
	    reader.setEntityStore(store);

	    System.out.print("reading gtfs file...");
	    reader.run();
	    System.out.println("done.");
	}

	public List<Trip> getTrips(String routeName, Window window) {
		
		
		Route route = this.getRouteForName(routeName);
		if(route==null){
			return null;
		}
		
		List<Trip> ret = new ArrayList<Trip>();
		
		List<Trip> trips = store.getTripsForRoute(route);
		for(Trip trip : trips){
			if( !window.includes(trip,store.getStopTimesForTrip(trip)) ){
				continue;
			}
			ret.add(trip);
		}
		
		return ret;
	}

	private Route getRouteForName(String routeName) {
		String baseRouteName;
		if(routeName.length()>2 && routeName.substring(routeName.length()-2).equals("EX")){
			baseRouteName = routeName.substring(0,routeName.length()-2);
		} else if (routeName.length()>4 && routeName.substring(routeName.length()-4).equals("DART")){
			baseRouteName = routeName.substring(0,routeName.length()-4);
		} else {
			baseRouteName = routeName;
		}
				
		for( Route route : store.getAllRoutes() ){
			if(route.getShortName().equals(baseRouteName)){
				return route;
			}
		}
		return null;
	}

}
