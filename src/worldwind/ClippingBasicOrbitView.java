package worldwind;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

public class ClippingBasicOrbitView extends BasicOrbitView {
	@Override
	public double computeNearClipDistance() {
		Position eyePos = getCurrentEyePosition();
		double near = computeNearDistance(eyePos);
		return near / 4.0;
	}

	@Override
	public double computeFarClipDistance() {
		Position eyePos = getCurrentEyePosition();
		return computeFarDistance(eyePos) * 4.0;
	}

	@Override
	protected double computeNearDistance(Position eyePosition) {
		double near = 0;
		if (eyePosition != null && this.dc != null) {
			double elevation = ViewUtil.computeElevationAboveSurface(this.dc, eyePosition);
			double tanHalfFov = getFieldOfView().tanHalfAngle();
			near = elevation / (2 * Math.sqrt(2 * tanHalfFov * tanHalfFov + 1));
		}
		return near < MINIMUM_NEAR_DISTANCE ? MINIMUM_NEAR_DISTANCE : near;
	}

	@Override
	protected double computeFarDistance(Position eyePosition) {
		double far = 0;
		if (eyePosition != null) {
			far = computeHorizonDistance(eyePosition);
		}
		return far < MINIMUM_FAR_DISTANCE ? MINIMUM_FAR_DISTANCE : far;
	}
}