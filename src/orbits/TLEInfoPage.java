package orbits;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatPassTime;
import uk.me.g4dpz.satellite.TLE;

public class TLEInfoPage {
	private URL url;
	
	public TLEInfoPage(String url) throws MalformedURLException{
		this.url = new URL(url);
	}
	
	public String getURL(){
		return url.toString();
	}

	public TLE getTLEForSatellite(String satName){
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String line = null;
			while((line = br.readLine().trim()) != null){
				if(line.equals(satName.trim())){
					String tleLine1 = br.readLine();
					String tleLine2 = br.readLine();
					
					return new TLE(new String[]{line, tleLine1, tleLine2});
				}
			}
		} catch(Exception e){
			return null;
		} 
		
		return null;
	}
	
	public List<SatInfo> getAllNextPasses(GroundStationPosition gsp){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		ArrayList<SatInfo> sptList = new ArrayList<SatInfo>();
		
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim();
				String tleLine1 = br.readLine();
				String tleLine2 = br.readLine();
				
				TLE tle = new TLE(new String[]{line, tleLine1, tleLine2});
				PassPredictor pp = new PassPredictor(tle, gsp);
				SatPassTime spt = pp.nextSatPass(new Date());
		
				sptList.add(new SatInfo(line, spt));
			}
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
			
		return sptList;
	}
}
