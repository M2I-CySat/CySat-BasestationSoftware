package util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GUIUtils {
	public static void setAntiAlias(Graphics g, boolean isAntiAliased){
		Graphics2D g2d = (Graphics2D) g;
		RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				isAntiAliased ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2d.setRenderingHints(renderHints);
	}
}
