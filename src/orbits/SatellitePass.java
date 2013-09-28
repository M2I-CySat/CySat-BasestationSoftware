package orbits;

import java.util.List;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.SatPassTime;

public class SatellitePass {
	private CelestrakSatellite satellite;
	private List<AzElPair> passPoints;
	private int timeStep;
	private GroundStationPosition groundStation;
	private SatPassTime spt;
	
	public SatellitePass(){
		
	}
	
	public SatellitePass(CelestrakSatellite satellite, List<AzElPair> passPoints, 
				int timeStep, GroundStationPosition groundStation, SatPassTime spt){
		this.satellite = satellite;
		this.passPoints = passPoints;
		this.timeStep = timeStep;
		this.groundStation = groundStation;
		this.spt = spt;
	}

	public SatPassTime getSatPassTime(){
		return spt;
	}

	public void setSatPassTime(SatPassTime spt){
		this.spt = spt;
	}

	public CelestrakSatellite getSatellite(){
		return satellite;
	}
	
	public void setSatellite(CelestrakSatellite satellite){
		this.satellite = satellite;
	}

	public List<AzElPair> getPassPoints(){
		return passPoints;
	}

	public void setPassPoints(List<AzElPair> passPoints){
		this.passPoints = passPoints;
	}

	public long getBaseTime(){
		return spt.getStartTime().getTime();
	}

	public void setBaseTime(long baseTime){
		spt.getStartTime().setTime(baseTime);
	}

	public int getTimeStep(){
		return timeStep;
	}

	public void setTimeStep(int timeStep){
		this.timeStep = timeStep;
	}

	public GroundStationPosition getGroundStation(){
		return groundStation;
	}

	public void setGroundStation(GroundStationPosition groundStation){
		this.groundStation = groundStation;
	}
}
