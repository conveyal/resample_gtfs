package com.conveyal.resample;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.Trip;

public class Main {

	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.err.println("usage: resample_gtfs gtfs_filename servicelevel_filename");
			System.exit(-1);
		}

		String gtfs_filename = args[0];
		String servicelevel_filename = args[1];
		
		ServiceLevelFile slf = new ServiceLevelFile( servicelevel_filename );
		
		GTFSService gss = new GTFSService( gtfs_filename );
		gss.read();
		
		List<Trip> resampled = new ArrayList<Trip>(); // every trip that will be deleted
		List<Trip> newSamples = new ArrayList<Trip>(); // every new trip
		
		for(ServiceLevel serviceLevel : slf.getServiceLevels()){
			
			
			for(Window window : slf.getWindows()){
				Double headway = serviceLevel.getHeadway(window.name);
				Integer nTrips = serviceLevel.getTrips(window.name);
				
				List<Trip> trips = gss.getTrips( serviceLevel.route, serviceLevel.tripFilters, window );
				
				if(trips==null){
					System.out.println( "route "+serviceLevel.route+" window "+window.name+": could not find route" );
				} else {
					if( headway!=null && nTrips!=null ){
						throw new Exception("has to be one or the other, headway or trips");
					}
					
					System.out.println( "route "+serviceLevel.route+" window "+window.name+": "+trips.size()+" trips" );
					System.out.println( "resampling to headway "+headway+" or "+nTrips+" trips" );
				}
				
			}
		}
		
	} 

}


