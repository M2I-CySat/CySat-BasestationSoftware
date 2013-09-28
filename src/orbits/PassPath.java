package orbits;

import java.util.ArrayList;
import java.util.List;

public class PassPath {
	private double startAz;
	private double endAz;
	private double maxElev;
	private int durationSec;
	private double angleSpan;

	public PassPath(double startAz, double endAz, double maxElev, int durationSec) {
		this.startAz = startAz;
		this.endAz = endAz;
		this.maxElev = maxElev;
		this.durationSec = durationSec;
		
		angleSpan = Math.toRadians(Math.min(Math.abs(endAz - startAz), 360 - endAz + startAz));
		if(angleSpan > Math.PI){
			angleSpan = 2*Math.PI - angleSpan;
		}
		
//		System.out.println("ANGLE: " + Math.toDegrees(angleSpan));
	}

	public AzElPair getLatLong(double thetaRad){
		double el = Math.asin(Math.sin(thetaRad)
				* Math.cos(Math.toRadians(90 - maxElev)));
		
		double az = 0;
		
		if(startAz > endAz){
			if(startAz - endAz <= 180){
				az = Math.toRadians(startAz)
					- Math.atan2(Math.sin(Math.toRadians(90 - maxElev)) * Math.sin(thetaRad), Math.cos(thetaRad));
			} else{
				az = Math.toRadians(startAz)
					+ Math.atan2(Math.sin(Math.toRadians(90 - maxElev)) * Math.sin(thetaRad), Math.cos(thetaRad));
			}
		} else{
			if(endAz - startAz <= 180){
				az = Math.toRadians(startAz)
					+ Math.atan2(Math.sin(Math.toRadians(90 - maxElev)) * Math.sin(thetaRad), Math.cos(thetaRad));
			} else{
				az = Math.toRadians(startAz)
					- Math.atan2(Math.sin(Math.toRadians(90 - maxElev)) * Math.sin(thetaRad), Math.cos(thetaRad));
			}
		}
		
		if(az >= 2*Math.PI){
			az -= 2*Math.PI;
		} else if(az < 0){
			az = az + 2*Math.PI;
		}
		
		return new AzElPair(Math.toDegrees(az), Math.toDegrees(el));
	}
	
	public AzElPair getLatLong(int timeSec){
		return getLatLong(Math.PI*timeSec / durationSec);
	}
	
	public List<AzElPair> getLatLongs(int secondInterval){
		List<AzElPair> list = new ArrayList<AzElPair>();
		for(int time=0; time < durationSec; time += secondInterval){
			list.add(getLatLong(time));
		}
		
		return list;
	}
}
