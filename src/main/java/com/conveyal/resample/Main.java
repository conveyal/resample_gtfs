package com.conveyal.resample;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.serialization.GtfsWriter;

public class Main {

	static Set<Trip> staleTrips = new HashSet<Trip>(); // every trip that will be deleted
	static Set<Trip> newTrips = new HashSet<Trip>(); // every new trip
	static List<StopTime> newStopTimes = new ArrayList<StopTime>();
	static GTFSService gss = null;
	
	public static void main(String[] args) throws Exception {

		if (args.length < 3) {
			System.err.println("usage: resample_gtfs gtfs_filename servicelevel_filename output_filename");
			System.exit(-1);
		}

		String gtfs_filename = args[0];
		String servicelevel_filename = args[1];
		String output_filename = args[2];
		
		ServiceLevelFile slf = new ServiceLevelFile( servicelevel_filename );
		
		gss = new GTFSService( gtfs_filename );
		gss.read();
		
		for(ServiceLevel serviceLevel : slf.getServiceLevels()){
			
			if(serviceLevel.suppress){
				System.out.println( "suppress route "+serviceLevel.route );
				
				List<Trip> trips = gss.getTrips( serviceLevel.route );
				deleteTrips( trips );
				
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
						if(dir0.size()>0)
							resampleByHeadway( dir0, window, headway );
						if(dir1.size()>0)
							resampleByHeadway( dir1, window, headway );
					} else if(nTrips!=null && headway==null){
						if(dir0.size()>0)
							resampleByTrips( dir0, window, nTrips );
						if(dir1.size()>0)
							resampleByTrips( dir1, window, nTrips );
					} else if(headway==null && nTrips==null){
						deleteTrips( trips );
					} else {
						throw new Exception("has to be one or the other, headway or trips");
					}
				}
				
			}
			
			
			
		}
				
		// write a new gtfs
	    GtfsWriter gtfsWriter = new GtfsWriter();
	    gtfsWriter.setOutputLocation(new File(output_filename));
	    
	    // copy over every type that doens't change;
	    @SuppressWarnings("rawtypes")
		Class[] unChangedEntityTypes = {Stop.class,Agency.class,ServiceCalendarDate.class,
	    		FareAttribute.class,FareRule.class,FeedInfo.class,Frequency.class,Pathway.class,
	    		Route.class,ServiceCalendar.class,ShapePoint.class,Transfer.class};
	    
	    for(int i=0; i<unChangedEntityTypes.length; i++){
	    	@SuppressWarnings("rawtypes")
			Class et = unChangedEntityTypes[i];
		    @SuppressWarnings({ "unchecked", "rawtypes" })
			Collection items = gss.store.getAllEntitiesForType(et);
		    for( Object item : items ){
		    	gtfsWriter.handleEntity(item);
		    }
	    }
	    
	    // add stoptimes
	    //   identify stale stoptimes to avoid copying
	    HashSet<StopTime> oldStopTimes = new HashSet<StopTime>();
	    for(Trip trip : staleTrips){
	    	oldStopTimes.addAll( gss.getStopTimesForTrip( trip ) );
	    }
	    //   copy stoptimes from old feed, except stale ones
	    int nDeletedStopTimes = 0;
	    for(StopTime stopTime : gss.store.getAllStopTimes()){
	    	if(!oldStopTimes.contains(stopTime)){
	    		gtfsWriter.handleEntity(stopTime);
	    	} else {
	    		nDeletedStopTimes++;
	    	}
	    }
	    //   add new stoptimes to feed
	    for(StopTime stopTime : newStopTimes){
	    	gtfsWriter.handleEntity(stopTime);
	    }
	    System.out.println( nDeletedStopTimes+" stale StopTimes deleted, "+newStopTimes.size()+" added" );
	    
	    // add trips
	    //   copy over trips from old feed, except stale ones
	    int nDeletedTrips = 0;
	    for(Trip trip : gss.store.getAllTrips()){
	    	
	    	if(!staleTrips.contains(trip)){
	    		gtfsWriter.handleEntity(trip);
	    	} else {
	    		nDeletedTrips++;
	    	}
	    }
	    //   add new trips to feed
	    for( Trip trip : newTrips ){
	    	gtfsWriter.handleEntity( trip );
	    }
	    System.out.println( nDeletedTrips+" stale trips deleted, "+newTrips.size()+" added" );
	    	    
	    gtfsWriter.close();

	}

	private static void deleteTrips(List<Trip> trips) {
		System.out.println( "deleting "+trips.size()+" trips" );
		
		staleTrips.addAll( trips );
	}

	private static void resampleByTrips(List<Trip> oldTrips, Window window, Integer nTrips) {
		for(Trip trip : oldTrips){
			if(trip.getId().getId().equals("18114250")){
				System.out.println("breakpoint");
			}
			//TODO debug, undo
		}
		
		System.out.println( "resampling "+oldTrips.size()+" trips by "+nTrips+" trips" );
		
		if( oldTrips.size() == nTrips ){
			// no actual change in the number of trips
			System.out.println( "no change to service level" );
			return;
		}
		
		staleTrips.addAll(oldTrips);
		
		//determine headway
		ArrayList<StopTime> departs = new ArrayList<StopTime>();
		for(Trip trip : oldTrips ){
			StopTime depart = gss.getStopTimesForTrip(trip).get(0);
			departs.add(depart);
		}
		Collections.sort(departs, new Comparator<StopTime>(){
			@Override
			public int compare(StopTime o1, StopTime o2) {
				return o1.getDepartureTime() - o2.getDepartureTime();
			}
		});
		ArrayList<Integer> waits = new ArrayList<Integer>();
		for(int i=0; i<departs.size()-1; i++){
			int wait = departs.get(i+1).getDepartureTime() - departs.get(i).getDepartureTime();
			waits.add(wait);
		}
		Collections.sort(waits);
		Integer headway = waits.get(waits.size()/2);
		
		int serviceStart;
		int subWindowSize = headway*(nTrips-1);
		// if the headway * nTrips is larger than the window, the headway 
		// will be window/nTrips and the start time will be the start of the window
		if( subWindowSize > window.sizeSeconds() ){
			if(nTrips <= 1){
				headway = window.sizeSeconds();
			} else {
				headway = window.sizeSeconds() / (nTrips-1);
			}
			serviceStart = window.sizeSeconds();
		
		// else we'll stick with the same headway and add trips to both ends of the service window
		} else {
			int subWindowMiddle = (window.startSeconds()+window.endSeconds())/2;
			serviceStart = subWindowMiddle - subWindowSize/2;
		}
		
		System.out.println( "mean headway is "+headway );
		
		Trip exemplar = getExemplar( oldTrips );
		
		for(int i=0; i<nTrips; i++){
			int t = serviceStart + i*headway;
			 
			Trip sample = new Trip(exemplar);
			sample.setBlockId(null); //no interlining for resampled trips
			newTrips.add(sample);
			
			// replace the id
			AgencyAndId id = new AgencyAndId();
			id.setAgencyId( exemplar.getId().getAgencyId() );
			id.setId( exemplar.getId().getId()+"-tr-resample-"+t/60 );
			sample.setId(id);
			
			// make a bunch of new stoptimes
			List<StopTime> movedStopTimes = moveStopTimes(exemplar, t);
			for( StopTime st : movedStopTimes ){
				st.setTrip(sample);
			}
			newStopTimes.addAll(movedStopTimes);
			
			System.out.println( "make trip "+sample+" starting at "+t/60+"min" );
		}
		
	}

	private static void resampleByHeadway(List<Trip> oldTrips, Window window, Double headwayMins) {
		for(Trip trip : oldTrips){
			if(trip.getId().getId().equals("18164218")){
				System.out.println("breakpoint");
			}
			//TODO debug, undo
		}
		
		System.out.println( "resampling "+oldTrips.size()+" trips by headway "+headwayMins+" mins" );
		
		int nTrips = (int) (window.sizeMins() / headwayMins);
		if( nTrips == oldTrips.size() ){
			// no actual change in the number of trips
			System.out.println( "no change to service level" );
			return;
		}
		
		staleTrips.addAll(oldTrips);
		
		System.out.println( "resampled to "+nTrips+" new trips" );
		
		Trip exemplar = getExemplar( oldTrips );
		
		for(int t=window.startMins(); t<window.endMins(); t+=headwayMins){
			Trip sample = new Trip(exemplar);
			sample.setBlockId(null); // no interlining for resampled trips
			newTrips.add(sample);
			
			// replace the id
			AgencyAndId id = new AgencyAndId();
			id.setAgencyId( exemplar.getId().getAgencyId() );
			id.setId( exemplar.getId().getId()+"-hw-resample-"+t );
			sample.setId(id);
			
			if(id.getId().equals( "18164218-resample-1380" )){
				System.out.println("BREAK");
				//TODO DEBUG
			}
			
			// make a bunch of new stoptimes
			List<StopTime> movedStopTimes = moveStopTimes(exemplar, t*60);
			for( StopTime st : movedStopTimes ){
				st.setTrip(sample);
			}
			newStopTimes.addAll(movedStopTimes);
			
			System.out.println( "make trip "+sample+" starting at "+t+"min" );
		}

	}

//	private static Trip copyTrip(Trip exemplar) {
//		Trip ret = new Trip();
//		ret.set
//	}

	private static List<StopTime> moveStopTimes(Trip exemplar, int time) {
		List<StopTime> ret = new ArrayList<StopTime>();
		
		List<StopTime> origStopTimes = gss.getStopTimesForTrip( exemplar );
		StopTime first = origStopTimes.get(0);
		int offset = time - first.getDepartureTime();
		for( StopTime stopTime : origStopTimes ){
			StopTime newSt = new StopTime(stopTime);
			newSt.setDepartureTime( stopTime.getDepartureTime()+offset );
			newSt.setArrivalTime( stopTime.getArrivalTime()+offset );
			ret.add( newSt );
		}
		
		return ret;
	}

	private static Trip getExemplar(List<Trip> oldTrips) {
		ArrayList<Trip> mostCommonPatternTrips = getMostPopularPatternTrips(oldTrips);
		
		// return first trip from it
		return mostCommonPatternTrips.get(0);
	}

	private static ArrayList<Trip> getMostPopularPatternTrips(List<Trip> oldTrips) {
		// group trips by patterns
		HashMap<StopPattern, ArrayList<Trip>> patternToTrips = groupTripsByPattern(oldTrips);
		
		// find most common pattern
		ArrayList<Trip> mostCommonPatternTrips = getLargestList(patternToTrips);
		return mostCommonPatternTrips;
	}

	private static ArrayList<Trip> getLargestList(HashMap<StopPattern, ArrayList<Trip>> patternToTrips) {
		List<ArrayList<Trip>> patternGroups = new ArrayList<ArrayList<Trip>>( patternToTrips.values() );
		Collections.sort(patternGroups, new Comparator<ArrayList<Trip>>(){
			@Override
			public int compare(ArrayList<Trip> o1, ArrayList<Trip> o2) {
				return o2.size()-o1.size(); //sort descending
			}
		});
		ArrayList<Trip> mostCommonPatternTrips = patternGroups.get(0);
		return mostCommonPatternTrips;
	}

	private static HashMap<StopPattern, ArrayList<Trip>> groupTripsByPattern(List<Trip> oldTrips) {
		HashMap<StopPattern,ArrayList<Trip>> patternToTrips = new HashMap<StopPattern,ArrayList<Trip>>();
		
		for( Trip trip : oldTrips ){
			StopPattern pattern = gss.getPattern( trip );
			
			ArrayList<Trip> tripsWithPattern = patternToTrips.get(pattern);
			if( tripsWithPattern==null ){
				tripsWithPattern = new ArrayList<Trip>();
				patternToTrips.put(pattern, tripsWithPattern);
			}
			
			tripsWithPattern.add( trip );
		}
		return patternToTrips;
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


