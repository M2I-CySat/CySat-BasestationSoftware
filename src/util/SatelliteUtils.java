package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;

import orbits.CelestrakSatellite;
import orbits.CommandSet;
import orbits.SatellitePass;
import orbits.TLEInfoPage;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.InvalidTleException;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatNotFoundException;
import uk.me.g4dpz.satellite.SatPassTime;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.TLE;

public class SatelliteUtils {
	public static final double AMES_LATITUDE = 42.027087;
	public static final double AMES_LONGITUDE = -93.653373;
	public static final double AMES_ELEVATION_METERS = 287;
	public static final double MIN_ELEV = 10.0; //Minimum elevation in degrees to consider a satellite pass
	
	private static ArrayList<CelestrakSatellite> satellites;
	private static String satInfoFile = "res/satellites.txt";
	
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
			loadCelestrakSatellites();
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
	
	private static void loadCelestrakSatellites() throws FileNotFoundException, MalformedURLException, URISyntaxException{
		satellites = new ArrayList<CelestrakSatellite>();
		File infoFile = new File(satInfoFile);
		if(!infoFile.exists()){
			System.err.println("Can't find file: " + satInfoFile);
			throw new FileNotFoundException();
		}
		
		Scanner in = new Scanner(infoFile);
		while(in.hasNextLine()){
			String line = in.nextLine();
			if(line.startsWith("#") || line.trim().isEmpty()) {
				continue;
			}
			
			String infoPageURL = line.trim();
			TLEInfoPage infoPage = new TLEInfoPage(infoPageURL);
			while(in.hasNextLine()){
				String satName = in.nextLine().trim();
				if(satName.isEmpty())
					break;

				if(!satName.startsWith("#"))
					satellites.add(new CelestrakSatellite(satName, infoPage));
			}
		}
		
		in.close();
	}
	
	public static List<SatellitePass> getNextSatellitePasses(String satName, int timeStep, int nTimes, double minElev) {
		List<SatellitePass> passes = new ArrayList<SatellitePass>();
		Date startDate = new Date();
		for(int i=0; i < nTimes; i++){
			SatellitePass pass = getNextSatellitePass(satName, timeStep, startDate, minElev);
			if(pass != null){
				passes.add(pass);
				startDate = new Date(pass.getSatPassTime().getEndTime().getTime() + 1000L);
			}
		}
		
		return passes;
	}
	
	public static SatellitePass getNextSatellitePass(String satName, int timeStep){
		return getNextSatellitePass(satName, timeStep, new Date(), 0);
	}
	
	public static SatellitePass getNextSatellitePass(String satName, int timeStep, Date date, double minElev){
		if (minElev > 70) {
			System.err.println("Warning! A high minElev will result in very sparse satellite passes, perhaps even none.");
		}
		
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
			SatPassTime spt = null;
			do {
				spt = pp.nextSatPass(date);
				date = new Date(spt.getEndTime().getTime() + 1000L); //1s past end of previous pass
			} while (spt.getMaxEl() < minElev);

			satPass.setGroundStation(ames);
			satPass.setSatPassTime(spt);
			
			satPass.setTimeStep(timeStep);

			List<SatPos> passPoints = pp.getPositions(spt.getStartTime(), timeStep, 0, (int) ((spt.getEndTime().getTime() - spt.getStartTime().getTime())/1000/60));
			satPass.setPassPoints(passPoints, true);
			
			return satPass;
		} catch(Exception e){
			return null;
		}
	}
	
	public static CommandSet getRotatorCommandSet(List<SatPos> list, long baseTime, int timeStep){
		CommandSet cmdSet = new CommandSet(new Date(baseTime), timeStep);

		for(SatPos satPos : list){
			cmdSet.add(String.format("%03d %03.0f", (int) ((satPos.getAzimuth() + 180) % 360), satPos.getElevation()));
		}

		return cmdSet;
	}
	
	public static int getDuration(SatPassTime spt){
		return (int) ((spt.getEndTime().getTime() - spt.getStartTime().getTime()) / 1000.0);
	}
}
