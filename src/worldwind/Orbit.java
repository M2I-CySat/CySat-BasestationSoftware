package worldwind;



public class Orbit {
	private double inclination;
	private double eccentricity;
	private double startLatitude;
	private double startLongitude;
	private double semiMajorAxis;
	
	public Orbit(double startLat, double startLon, double inc, double ecc, double sma){
		this.startLatitude = startLat;
		this.startLongitude = startLon;
		this.inclination = inc;
		this.eccentricity = ecc;
		this.semiMajorAxis = sma;
	}
	
	public double getInclination(){
		return inclination;
	}
	
	public double getEccentricity(){
		return eccentricity;
	}
	
	public double getSemiMajorAxis(){
		return semiMajorAxis;
	}
	
	public double getStartLatitude(){
		return startLatitude;
	}
	
	public double getStartLongitude(){
		return startLongitude;
	}
}
