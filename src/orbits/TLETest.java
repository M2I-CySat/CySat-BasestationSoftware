package orbits;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.InvalidTleException;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatNotFoundException;
import uk.me.g4dpz.satellite.SatPassTime;
import uk.me.g4dpz.satellite.TLE;

public class TLETest {
	private static final String newline = System.getProperty("line.separator");
	
	public static void main(String[] args){
		try{
//			TLE tle = makeTLE("src/orbits/tle3.txt");
			String tleURL = "http://www.celestrak.com/NORAD/elements/noaa.txt";
//			String tleURL = "http://www.celestrak.com/NORAD/elements/stations.txt";
			String satName = "NOAA 6 [P]";
//			String tleURL = "http://www.celestrak.com/NORAD/elements/cubesat.txt";
//			String satName = "M-CUBED & EXP-1 PRIME";
			
			printNextPass(tleURL, satName);
//			printAllNextPasses(tleURL);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void printAllNextPasses(String tleURL) throws MalformedURLException{
		TLEInfoPage tleInfo = new TLEInfoPage(tleURL);
		GroundStationPosition ames = new GroundStationPosition(42.03472, -93.62, 287);
		List<SatInfo> sptList = tleInfo.getAllNextPasses(ames);
		if(sptList != null){
			for(SatInfo si : sptList){
				System.out.println("Next pass of satellite: " + si.getName() + newline + newline + si.getNextPassTime() + newline + newline);
			}
		}
	}
	
	public static void printNextPass(String tleURL, String satName) throws IllegalArgumentException, InvalidTleException, SatNotFoundException, MalformedURLException{
		TLEInfoPage tleInfo = new TLEInfoPage(tleURL);
		TLE tle = tleInfo.getTLEForSatellite(satName);
		if(tle == null){
			System.err.println("Unable to obtain TLE for satellite: " + satName);
			System.exit(0);
		}
		GroundStationPosition ames = new GroundStationPosition(42.03472, -93.62, 287);
		PassPredictor pp = new PassPredictor(tle, ames);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
//		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		SatPassTime spt = pp.nextSatPass(new Date());
		System.out.println("Next pass of satellite: " + tle.getName() + newline + newline + spt);
		System.out.println();
		System.out.println("Starts at: " + sdf.format(spt.getStartTime()));
		long dur = spt.getEndTime().getTime() - spt.getStartTime().getTime();
		System.out.println(spt.getAosAzimuth() + " az -> " + spt.getLosAzimuth() + " az via " + String.format("%.2f", spt.getMaxEl())
					+ " el in " + dur/1000 + " seconds (" + String.format("%.2f", (dur/1000.0/60.0)) + " minutes)");	
	}
	
	public static void printNextPasses(SimpleDateFormat sdf, PassPredictor pp, int nHours) throws InvalidTleException, SatNotFoundException, ParseException{
		for(SatPassTime sptt : pp.getPasses(new Date(), nHours, false)){
//			System.out.println(sdf.format(sptt.getStartTime()));
			System.out.println(sptt.getStartTime());
			System.out.println();
		}
	}
	
	public static TLE makeTLE(String path) throws FileNotFoundException, InvalidTleException{
		Scanner s = new Scanner(new File(path));
		String[] data = new String[3];
		int i=0;
		while(s.hasNextLine()){
			if(i == 3){
				s.close();
				throw new InvalidTleException();
			}
			data[i++] = s.nextLine();
		}
		s.close();
		
		return new TLE(data);
	}
}
