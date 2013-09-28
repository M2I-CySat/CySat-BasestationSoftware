package util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;

import orbits.AzElPair;
import orbits.CelestrakSatellite;
import orbits.CommandSet;
import orbits.PassPath;
import orbits.SatellitePass;
import orbits.TLEInfoPage;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.InvalidTleException;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatNotFoundException;
import uk.me.g4dpz.satellite.SatPassTime;
import uk.me.g4dpz.satellite.TLE;

public class SatelliteUtils {
	public static final double AMES_LATITUDE = 42.03472;
	public static final double AMES_LONGITUDE = -93.62;
	public static final double AMES_ELEVATION_METERS = 287;
	
	private static ArrayList<CelestrakSatellite> satellites;
	private static String satInfoFile = "/orbits/satellites.txt";
	
	private static boolean initialized = false;
	
	public static void main(String[] args) throws FileNotFoundException, MalformedURLException, URISyntaxException, IllegalArgumentException, InvalidTleException, SatNotFoundException{
		init();

		List<Pair<CelestrakSatellite, SatPassTime>> passList = new ArrayList<Pair<CelestrakSatellite, SatPassTime>>();
		
		for(CelestrakSatellite sat : satellites){
			TLE tle = sat.getTLE();

			GroundStationPosition ames = new GroundStationPosition(AMES_LATITUDE, AMES_LONGITUDE, AMES_ELEVATION_METERS);
			PassPredictor pp = new PassPredictor(tle, ames);
			SatPassTime spt = pp.nextSatPass(new Date());
			passList.add(new ImmutablePair<CelestrakSatellite, SatPassTime>(sat, spt));
		}
		
		System.out.println("Got Satellites! Sorting...");
	
////	Sort by next pass time
//		Collections.sort(passList, new Comparator<Pair<CelestrakSatellite, SatPassTime>>(){
//			@Override
//			public int compare(Pair<CelestrakSatellite, SatPassTime> o1, Pair<CelestrakSatellite, SatPassTime> o2){
//				long t1 = o1.getRight().getStartTime().getTime();
//				long t2 = o2.getRight().getStartTime().getTime();
//				return t1 < t2 ? -1 : t1 == t2 ? 0 : 1;
//			}
//		});
		
//		Sort by next pass time
		Collections.sort(passList, new Comparator<Pair<CelestrakSatellite, SatPassTime>>(){
			@Override
			public int compare(Pair<CelestrakSatellite, SatPassTime> o1, Pair<CelestrakSatellite, SatPassTime> o2){
				double e1 = o1.getRight().getMaxEl();
				double e2 = o2.getRight().getMaxEl();
				return e1 < e2 ? -1 : e1 == e2 ? 0 : 1;
			}
		});
		
		for(Pair<CelestrakSatellite, SatPassTime> pass : passList){
			System.out.println();
			System.out.println("For satellite: " + pass.getLeft().getSatName());
			System.out.println();
			System.out.println(pass.getRight());
		}
	}
	
	public static void init() throws FileNotFoundException, MalformedURLException, URISyntaxException{
		if(!initialized)
			getCelestrakSatellites();
		initialized = true;
	}
	
	public static ArrayList<CelestrakSatellite> getSatellites(){
		return satellites;
	}
	
	public static CelestrakSatellite getSatellite(String satName){
		for(CelestrakSatellite sat : satellites){
			if(sat.getSatName().trim().equalsIgnoreCase(satName)){
				return sat;
			}
		}
		
		return null;
	}
	
	private static void getCelestrakSatellites() throws FileNotFoundException, MalformedURLException, URISyntaxException{
		satellites = new ArrayList<CelestrakSatellite>();
		InputStream is = Class.class.getResourceAsStream(satInfoFile);
		if(is == null){
			System.err.println("Can't find file: " + satInfoFile);
			throw new FileNotFoundException();
		}
		
		Scanner s = new Scanner(is);
		while(s.hasNextLine()){
			String infoPageURL = s.nextLine().trim();
			TLEInfoPage infoPage = new TLEInfoPage(infoPageURL);
			while(s.hasNextLine()){
				String satName = s.nextLine().trim();
				if(satName.isEmpty())
					break;

				if(!satName.startsWith("#"))
					satellites.add(new CelestrakSatellite(satName, infoPage));
			}
		}
		
		s.close();
	}
	
	public static List<SatellitePass> getNextSatellitePasses(String satName, int timeStep, int nTimes) {
		List<SatellitePass> passes = new ArrayList<SatellitePass>();
		Date startDate = new Date();
		for(int i=0; i < nTimes; i++){
			SatellitePass pass = getNextSatellitePass(satName, timeStep, startDate);
			if(pass != null){
				passes.add(pass);
				startDate = new Date(pass.getSatPassTime().getEndTime().getTime() + 1000L);
			}
		}
		
		return passes;
	}
	
	public static SatellitePass getNextSatellitePass(String satName, int timeStep){
		return getNextSatellitePass(satName, timeStep, new Date());
	}
	
	public static SatellitePass getNextSatellitePass(String satName, int timeStep, Date date){
		CelestrakSatellite satellite = SatelliteUtils.getSatellite(satName);
		if(satellite == null){
			JOptionPane.showMessageDialog(null, "Invalid satellite! Unable to show pass data.");
			return null;
		}
		
		TLE tle = satellite.getTLE();
		if(tle == null){
			JOptionPane.showMessageDialog(null, "Invalid satellite! Unable to show pass data.");
			return null;
		}

		try{
			SatellitePass satPass = new SatellitePass();
			satPass.setSatellite(satellite);
	
			GroundStationPosition ames = new GroundStationPosition(SatelliteUtils.AMES_LATITUDE, 
						SatelliteUtils.AMES_LONGITUDE, SatelliteUtils.AMES_ELEVATION_METERS);
		
			PassPredictor pp = new PassPredictor(tle, ames);
			SatPassTime spt = pp.nextSatPass(date);
			
			satPass.setGroundStation(ames);
			satPass.setSatPassTime(spt);
			
			satPass.setTimeStep(timeStep);
			satPass.setPassPoints(SatelliteUtils.getPassPathPoints(spt, timeStep));
			
			return satPass;
		} catch(Exception e){
			return null;
		}
	}
	
	public static List<AzElPair> getPassPathPoints(SatPassTime spt, int timeStep){
		PassPath pp = new PassPath(spt.getAosAzimuth(), spt.getLosAzimuth(), spt.getMaxEl(), getDuration(spt));

		return pp.getLatLongs(timeStep);
	}
	public static CommandSet getRotatorCommandSet(List<AzElPair> list, long baseTime, int timeStep){
		CommandSet cmdSet = new CommandSet(new Date(baseTime), timeStep);

		for(AzElPair pair : list){
			cmdSet.add(String.format("%03d %03.0f", (int) ((pair.getAz() + 180) % 360), pair.getEl()));
		}

		return cmdSet;
	}
	
	public static int getDuration(SatPassTime spt){
		return (int) ((spt.getEndTime().getTime() - spt.getStartTime().getTime()) / 1000.0);
	}
}