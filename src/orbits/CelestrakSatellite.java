package orbits;

import uk.me.g4dpz.satellite.TLE;

public class CelestrakSatellite {
	private String satName;
	private TLEInfoPage infoPage;

	public CelestrakSatellite(String satName, TLEInfoPage infoPage) {
		this.satName = satName;
		this.infoPage = infoPage;
	}

	public String getSatName() {
		return satName;
	}

	public TLEInfoPage getInfoPage() {
		return infoPage;
	}

	public TLE getTLE() {
		return infoPage.getTLEForSatellite(satName);
	}

	public String toString() {
		return satName;
	}
}
