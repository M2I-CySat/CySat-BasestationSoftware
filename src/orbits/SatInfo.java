package orbits;

import uk.me.g4dpz.satellite.SatPassTime;

public class SatInfo {
	private String satName;
	private SatPassTime nextPassTime;
	
	public SatInfo(String satName, SatPassTime nextPassTime){
		this.satName = satName;
		this.nextPassTime = nextPassTime;
	}
	
	public String getName(){
		return satName;
	}
	
	public SatPassTime getNextPassTime(){
		return nextPassTime;
	}
}
