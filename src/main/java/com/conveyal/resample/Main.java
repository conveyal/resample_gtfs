package com.conveyal.resample;

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
		
		for(ServiceLevel serviceLevel : slf.getServiceLevels()){
			for(Window window : slf.getWindows()){
				Double headway = serviceLevel.getHeadway(window.name);
				Integer nTrips = serviceLevel.getTrips(window.name);
				
				List<Trip> trips = gss.getTrips( serviceLevel.route, window );
				
				if(trips==null){
					System.out.println( "route "+serviceLevel.route+" window "+window.name+": could not find route" );
				} else {
					System.out.println( "route "+serviceLevel.route+" window "+window.name+": "+trips.size()+" trips" );
				}
				
			}
		}
		
	} 

}


