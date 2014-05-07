package com.conveyal.resample;

public class Main {

	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.err.println("usage: resample_gtfs gtfs_filename servicelevel_filename");
			System.exit(-1);
		}

		String gtfs_filename = args[0];
		String servicelevel_filename = args[1];
		
		ServiceLevelFile slf = new ServiceLevelFile( servicelevel_filename );
		
		for( Window window : slf.getWindows() ){
			System.out.println( window );
		}
		
		for( Route route : slf.getRoutes() ){
			System.out.println( route );
			System.out.println( route.getHeadway("peak_am") );
			System.out.println( route.getTrips("peak_am") );
		}
		
	} 

}


