package orbits;


public class AzElPair {
	private double az;
	private double el;
	
	public AzElPair(double az, double el){
		this.az = az;
		this.el = el;
	}
	
	public double getAz(){
		return az;
	}
	
	public double getEl(){
		return el;
	}
}
