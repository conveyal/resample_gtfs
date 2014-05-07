package com.conveyal.resample;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.Trip;

public class Main {

	static List<Trip> resampled = new ArrayList<Trip>(); // every trip that will be deleted
	static List<Trip> newSamples = new ArrayList<Trip>(); // every new trip
	
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
		
		for(ServiceLevel serviceLevel : slf.getServiceLevels()){
			
			if(serviceLevel.suppress){
				System.out.println( "suppress route "+serviceLevel.route );
				//TODO implement
				continue;
			}
			
			
			for(Window window : slf.getWindows()){
				System.out.println( "resampling route "+serviceLevel.route+" "+window.name );
				
				Double headway = serviceLevel.getHeadway(window.name);
				Integer nTrips = serviceLevel.getTrips(window.name);
				
				List<Trip> trips = gss.getTrips( serviceLevel.route, serviceLevel.tripFilters, window );
				
				List<Trip> dir0 = filterByDirection( trips, "0" );
				List<Trip> dir1 = filterByDirection( trips, "1" );
						
				if(trips==null){
					System.out.println( "could not find route" );
				} else {										
					if(headway!=null && nTrips==null){
						resampleByHeadway( dir0, window, headway );
						resampleByHeadway( dir1, window, headway );
					} else if(nTrips!=null && headway==null){
						resampleByTrips( dir0, window, nTrips );
						resampleByTrips( dir1, window, nTrips );
					} else if(headway==null && nTrips==null){
						deleteTrips( trips );
					} else {
						throw new Exception("has to be one or the other, headway or trips");
					}
				}
				
			}
		}
		
	}

	private static void deleteTrips(List<Trip> trips) {
		System.out.println( "deleting "+trips.size()+" trips" );
		
		resampled.addAll( trips );
	}

	private static void resampleByTrips(List<Trip> oldTrips, Window window, Integer nTrips) {
		System.out.println( "resampling "+oldTrips.size()+" trips by "+nTrips+" trips" );
		
		resampled.addAll(oldTrips);
		
		
	}

	private static void resampleByHeadway(List<Trip> oldTrips, Window window, Double headway) {
		System.out.println( "resampling "+oldTrips.size()+" trips by headway "+headway+" mins" );
		
		int nTrips = (int) ((window.end - window.start)*60 / headway);
		if( nTrips == oldTrips.size() ){
			// no actual change in the number of trips
			System.out.println( "no change to service level" );
			return;
		}
		
		resampled.addAll(oldTrips);
		
		System.out.println( "resampled to "+nTrips+" new trips" );
		
		//Trip examplar = getExemplar( oldTrips );

		
	}

	private static List<Trip> filterByDirection(List<Trip> trips, String dirValue) {
		List<Trip> ret = new ArrayList<Trip>();
		
		for(Trip trip : trips){
			if( trip.getDirectionId().equals(dirValue) ){
				ret.add(trip);
			}
		}
		
		return ret;
	} 

}


