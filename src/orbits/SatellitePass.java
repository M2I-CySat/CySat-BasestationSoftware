package orbits;

import java.util.ArrayList;
import java.util.List;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.SatPassTime;
import uk.me.g4dpz.satellite.SatPos;

public class SatellitePass {
	private CelestrakSatellite satellite;
	private List<SatPos> passPoints;
	private int timeStep;
	private GroundStationPosition groundStation;
	private SatPassTime spt;

	public SatellitePass() {

	}

	public SatellitePass(CelestrakSatellite satellite, List<SatPos> passPoints, boolean inRad, int timeStep,
			GroundStationPosition groundStation, SatPassTime spt) {
		this.satellite = satellite;
		this.timeStep = timeStep;
		this.groundStation = groundStation;
		this.spt = spt;

		if (inRad) {
			radToDegrees(passPoints);
		}
		this.passPoints = passPoints;
	}

	public SatPassTime getSatPassTime() {
		return spt;
	}

	public void setSatPassTime(SatPassTime spt) {
		this.spt = spt;
	}

	public CelestrakSatellite getSatellite() {
		return satellite;
	}

	public void setSatellite(CelestrakSatellite satellite) {
		this.satellite = satellite;
	}

	public List<SatPos> getPassPoints() {
		return passPoints;
	}

	public List<SatPos> getPassPoints(double minElev) {
		List<SatPos> ret = new ArrayList<>();
		for (SatPos sp : passPoints) {
			if (sp.getElevation() >= minElev) {
				ret.add(sp);
			}
		}

		return ret;
	}

	public void setPassPoints(List<SatPos> passPoints, boolean inRad) {
		if (inRad) {
			radToDegrees(passPoints);
		}
		this.passPoints = passPoints;
	}

	public long getBaseTime() {
		return spt.getStartTime().getTime();
	}

	public void setBaseTime(long baseTime) {
		spt.getStartTime().setTime(baseTime);
	}

	public int getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(int timeStep) {
		this.timeStep = timeStep;
	}

	public GroundStationPosition getGroundStation() {
		return groundStation;
	}

	public void setGroundStation(GroundStationPosition groundStation) {
		this.groundStation = groundStation;
	}

	private static void radToDegrees(List<SatPos> list) {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).setElevation(Math.toDegrees(list.get(i).getElevation()));
			list.get(i).setAzimuth(Math.toDegrees(list.get(i).getAzimuth()));
		}
	}
}
