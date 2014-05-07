package com.conveyal.resample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ServiceLevelFile {
	
	private JSONObject data;

	public ServiceLevelFile(String servicelevel_filename) throws IOException {
		File ff = new File(servicelevel_filename);
		String jsonStr = new String( Files.readAllBytes( ff.toPath() ) );
		data = new JSONObject( jsonStr );
	}

	public List<Window> getWindows() {
		List<Window> ret = new ArrayList<Window>();
		
		JSONArray jWindows = data.getJSONArray("windows");
		for(int i=0; i<jWindows.length(); i++){
			JSONObject jWindow = jWindows.getJSONObject(i);
			Window ww = Window.fromJSON( jWindow );
			ret.add( ww );
		}
		
		return ret;
	}

	public List<ServiceLevel> getServiceLevels() {
		List<ServiceLevel> ret = new ArrayList<ServiceLevel>();
		
		JSONArray jRoutes = data.getJSONArray("routes");
		for(int i=0; i<jRoutes.length(); i++){
			JSONObject jRoute = jRoutes.getJSONObject(i);
			ServiceLevel rt = ServiceLevel.fromJSON( jRoute );
			ret.add( rt );
		}

		return ret;
	}

}
