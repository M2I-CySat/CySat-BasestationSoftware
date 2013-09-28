package worldwind;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polyline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class WorldWindTest {
	private static final double EARTH_RADIUS = 6353000;
	private static final int FRAME_WIDTH = 1024;
	private static final int FRAME_HEIGHT = 600;
	
	private JFrame frame;
	
	public static void main(String[] args){
		new WorldWindTest();
	}
	
	public WorldWindTest(){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				createAndShowGUI();
			}
		});
	}
	
	private void createAndShowGUI(){
		frame = new JFrame("WorldWind Test");
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);

		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((scrSize.width - frame.getWidth()) / 2, (scrSize.height - frame.getHeight()) / 2);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		//create a WorldWind main object
		final WorldWindowGLCanvas worldWindCanvas = new WorldWindowGLCanvas();
		worldWindCanvas.setModel(new BasicModel());
		
		final int BASE_LAYERS = worldWindCanvas.getModel().getLayers().size();

		frame.setLayout(new BorderLayout());
		JPanel panel = new JPanel();
	
		JButton addOrbitsButton = new JButton("Add Orbits");
		addOrbitsButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				fillOrbit(worldWindCanvas);
			}
		});
		
		JButton eraseOrbitsButton = new JButton("Erase all orbits");
		eraseOrbitsButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				while(worldWindCanvas.getModel().getLayers().size() > BASE_LAYERS)
					worldWindCanvas.getModel().getLayers().remove(BASE_LAYERS);
			}
		});
		
		panel.add(addOrbitsButton);
		panel.add(eraseOrbitsButton);
		panel.setBackground(Color.BLACK);
		frame.add(panel, BorderLayout.NORTH);
		frame.add(worldWindCanvas);
		fillOrbit(worldWindCanvas);
	}
		
	private void fillOrbit(WorldWindowGLCanvas worldWindCanvas){
		worldWindCanvas.setView(new ClippingBasicOrbitView());
		
		// create some "Position" to build a polyline
		int inc = 20;
		double sma = EARTH_RADIUS + 10000000;
		double ecc = 0.4;
		int startLat = 45;
		int startLon = 0;
		Orbit orbit = new Orbit(startLat, startLon, inc, ecc, sma);
		drawOrbit(worldWindCanvas, orbit, Color.BLUE);
		
		Orbit orbit2 = new Orbit(0, startLon, 0, 0, EARTH_RADIUS + 35786000);
		drawOrbit(worldWindCanvas, orbit2, Color.RED);
		
		drawOrbit(worldWindCanvas, new Orbit(0, -90, -45, 0.2, EARTH_RADIUS + 5000000), Color.CYAN);
		
		drawOrbit(worldWindCanvas, new Orbit(0, 0, 51.64, 0.00137, 6788000), Color.GREEN);
		
		drawOrbit(worldWindCanvas, new Orbit(0, 0, 28.47, 0.00035, 6935000), Color.YELLOW);
	}
	
	private void drawOrbit(WorldWindowGLCanvas worldWindCanvas, Orbit orbit, Color color){
		LinkedList<Position> list = new LinkedList<Position>();
		for (double theta = 0; theta <= 360; theta+=1) {
			// in this case, points are in geographic coordinates.
			// If you are using cartesian coordinates, you have to convert them
			// to geographic coordinates.
			// Maybe, there are some functions doing that in WWJ API...
			list.add(calcPosition(orbit, theta));
		}	
		
		// create "Polyline" with list of "Position" and set color / thickness
		Polyline polyline = new Polyline(list);
		polyline.setColor(color);
		polyline.setLineWidth(3.0);

		// create a layer and add Polyline
		RenderableLayer layer = new RenderableLayer();
		layer.addRenderable(polyline);

		// add layer to WorldWind
		worldWindCanvas.getModel().getLayers().add(layer);
	}
	
	/**
	 * Calculates a satellite's position given a starting position, inclination, elevation, and an angle
	 * along the orbit (all in degrees)
	 * @param startLat
	 * The starting latitude of the orbit [-90 - 90]
	 * @param startLon
	 * The starting longitude of the orbit [0 - 360]
	 * @param inc
	 * The inclination of the orbit with respect to Earth's equator [0 - 360]
	 * @param theta
	 * The angle along the orbit (0 for the starting position, 180 for its antipode, etc.) [0 - 360]
	 * @param alt
	 * The altitude (in meters) above the surface of the Earth
	 * @return
	 * The position of the satellite at that angle along its orbit
	 */
	private static Position calcPosition(double startLat, double startLon, double inc, double theta, double alt){
		inc = 90 - inc;
		
		double lat1 = Math.toRadians(startLat);
		double lon1 = Math.toRadians(startLon);
		
		double brng = Math.toRadians(inc);
		double thetar = Math.toRadians(theta);
		double lat2 = Math.asin(Math.sin(lat1)*Math.cos(thetar) + 
	              Math.cos(lat1)*Math.sin(thetar)*Math.cos(brng) );
		double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(thetar)*Math.cos(lat1), 
	                     Math.cos(thetar)-Math.sin(lat1)*Math.sin(lat2));
		
		return Position.fromRadians(lat2, lon2, alt);
	}
	
	/**
	 * Calculates a satellite's position given an angle around its orbit
	 * @param orbit
	 * The orbit of the satellite
	 * @param theta
	 * The angle around the orbit (0 for the starting position, 180 for its antipode, etc.) [0-360]
	 * @return
	 * The position of the satellite at that angle along its orbit
	 */
	private static Position calcPosition(Orbit orbit, double theta){
		return calcPosition(orbit.getStartLatitude(), orbit.getStartLongitude(), 
				orbit.getInclination(), theta, getAltitude(orbit, theta));
	}
	
	private static double getAltitude(Orbit orbit, double theta){
		double a = orbit.getSemiMajorAxis();
		double e = orbit.getEccentricity();
		
		double radius = -a*(1 - e*e)/(1 - e*Math.cos(Math.toRadians(theta)));
		double distance = radius - EARTH_RADIUS;
		
		return distance;
	}
}
