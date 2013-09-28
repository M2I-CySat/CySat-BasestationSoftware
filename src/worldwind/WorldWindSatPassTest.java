package worldwind;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polyline;
import gui.LabelText;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import orbits.AzElPair;
import orbits.CelestrakSatellite;
import orbits.CommandSet;
import serial.client.SerialClient;
import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatPassTime;
import uk.me.g4dpz.satellite.TLE;
import util.SatelliteUtils;
import api.AntennaRotator;

public class WorldWindSatPassTest {
	public static void main(String[] args) throws ParseException{
		// PassPath pp = new PassPath(25, 162, 25, (int) (13.8 * 60));
		//
		// int timeStep = 15;
		// List<AzElPair> list = pp.getLatLongs(timeStep);
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		//
		// int i=0;
		// long baseTime = sdf.parse("2013/02/17 04:42:15").getTime();
		// for(AzElPair pair : list){
		// System.out.println(String.format("[W%03.0f %03.0f]", pair.getAz(),
		// pair.getEl()) +
		// " @ " + sdf.format(new Date(baseTime + timeStep*1000*i++)));
		// }

		new WorldWindSatPassTest();
	}

	private static final int FRAME_WIDTH = 1024;
	private static final int FRAME_HEIGHT = 600;

	private JFrame frame;

	private CommandSet cmdSet = null;
	private Timer timer = new Timer();

	private AntennaRotator rotator = null;
	private SerialClient client = null;

	public WorldWindSatPassTest() {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				WorldWind.setOfflineMode(true);
				createAndShowGUI();
			}
		});
	}

	private void createAndShowGUI(){
		try{
			SatelliteUtils.init();
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Unable to load satellites information! Exiting...");
			System.exit(1);
		}

		frame = new JFrame("WorldWind Test");
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);

		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((scrSize.width - frame.getWidth()) / 2, (scrSize.height - frame.getHeight()) / 2);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		// create a WorldWind main object
		final WorldWindowGLCanvas worldWindCanvas = new WorldWindowGLCanvas();
		worldWindCanvas.setModel(new BasicModel());

		final int BASE_LAYERS = worldWindCanvas.getModel().getLayers().size() - 17 * 0;
		System.out.println(BASE_LAYERS);
		while(worldWindCanvas.getModel().getLayers().size() > BASE_LAYERS - 1)
			worldWindCanvas.getModel().getLayers().remove(BASE_LAYERS - 1);

		addCompass(worldWindCanvas);
		worldWindCanvas.setView(new ClippingBasicOrbitView());

		frame.setLayout(new BorderLayout());
		final JPanel panel = new JPanel();
		panel.setBackground(Color.BLACK);

		final JComboBox<CelestrakSatellite> satChooseBox = new JComboBox<CelestrakSatellite>();
		for(CelestrakSatellite satellite : SatelliteUtils.getSatellites()){
			satChooseBox.addItem(satellite);
		}

		JTextArea satLabelText = new JTextArea("    Choose Satellite:");
		satLabelText.setBackground(null);
		satLabelText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		satLabelText.setForeground(Color.WHITE);
		satLabelText.setEditable(false);
		satLabelText.setFocusable(false);

		final JButton pathButton = new JButton("Get Path");

		final JButton startTracking = new JButton("Begin Tracking");
		startTracking.setEnabled(false);
		startTracking.setFocusable(false);
		startTracking.addActionListener(new ActionListener(){
			private JTextArea status;
			private JButton stop;
			private JTextArea updateStatus;

			private void finishTracking(){
				startTracking.setEnabled(true);
				satChooseBox.setEnabled(true);
				pathButton.setEnabled(true);

				timer.cancel();
				timer = new Timer();

				panel.remove(status);
				panel.remove(stop);
				panel.remove(updateStatus);
				frame.pack();
				frame.repaint();
			}

			@Override
			public void actionPerformed(ActionEvent e){
				if(rotator == null){
					JOptionPane.showMessageDialog(null,
							"You need to connect to the rotator first, or nothing will happen!");
				}

				if(cmdSet != null && cmdSet.getBase() != null){
					status = new JTextArea("   Waiting for AOS...");
					status.setBackground(null);
					status.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
					status.setForeground(new Color(0, 200, 0));
					status.setEditable(false);
					status.setFocusable(false);

					updateStatus = new JTextArea("Next update in: 0 seconds");
					updateStatus.setBackground(null);
					updateStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
					updateStatus.setForeground(Color.WHITE);
					updateStatus.setEditable(false);
					updateStatus.setFocusable(false);

					stop = new JButton("Stop");
					stop.setFocusable(false);
					stop.addActionListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e){
							finishTracking();
						}
					});

					startTracking.setEnabled(false);
					satChooseBox.setEnabled(false);
					pathButton.setEnabled(false);

					panel.add(stop);
					panel.add(status);
					panel.add(updateStatus);
					frame.pack();
					frame.repaint();

					cmdSet.base = new Date(new Date().getTime() + 5000);
					// cmdSet.base = new Date(cmdSet.base.getTime() + 125*1000);
					System.out.println(new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(cmdSet.base));
					timer.schedule(new TimerTask(){
						private Iterator<String> iter;
						private int count = 0;
						private boolean started = false;
						private int offset = 0;

						@Override
						public synchronized void run(){
							if(iter == null)
								iter = cmdSet.iterator();

							if(!iter.hasNext()){
								finishTracking();
								return;
							}

							if(started){
								if(++count == cmdSet.getTimeStep()){
									count = 0;

									String cmd = iter.next();
									issueCommand(cmd);
								}
							} else{
								Date now = new Date();
								if(cmdSet.getBase().before(now)){
									started = true;
									offset = 0;
									count = 0;

									String cmd = iter.next();
									issueCommand(cmd);
								} else{
									offset = (int) ((cmdSet.getBase().getTime() - now.getTime()) / 1000)
											- cmdSet.getTimeStep() + 1;
								}
							}

							updateStatus.setText("   Next update in: " + (offset + cmdSet.getTimeStep() - count)
									+ " seconds");
						}

						private void issueCommand(String cmd){
							System.out.println(cmd);
							status.setText("   Az: " + cmd.substring(0, 3) + "; El: " + cmd.substring(4));

							try{
								if(rotator != null)
									rotator.RotatorSet(Integer.parseInt(cmd.substring(0, 3)),
											Integer.parseInt(cmd.substring(4)));
							} catch(NumberFormatException | IOException e){
								e.printStackTrace();
							}
						}
					}, 0, 1000);
				}
			}
		});

		pathButton.setFocusable(false);
		pathButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				try{
					CelestrakSatellite satellite = (CelestrakSatellite) satChooseBox.getSelectedItem();
					TLE tle = satellite.getTLE();
					if(tle == null){
						JOptionPane.showMessageDialog(null, "Invalid satellite! Unable to show pass data.");
						return;
					}

					GroundStationPosition ames = new GroundStationPosition(SatelliteUtils.AMES_LATITUDE, 
								SatelliteUtils.AMES_LONGITUDE, SatelliteUtils.AMES_ELEVATION_METERS);
					PassPredictor pp = new PassPredictor(tle, ames);
					// SimpleDateFormat sdf = new
					// SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
					SatPassTime spt = pp.nextSatPass(new Date());
					System.out.println();
					System.out.println("For satellite: " + satellite.getSatName() + " @"
							+ satellite.getInfoPage().getURL());
					System.out.println();
					System.out.println(spt);

					while(worldWindCanvas.getModel().getLayers().size() > BASE_LAYERS)
						worldWindCanvas.getModel().getLayers().remove(BASE_LAYERS);

					worldWindCanvas.setView(new ClippingBasicOrbitView());

					int timeStep = 5;
					List<AzElPair> list = SatelliteUtils.getPassPathPoints(spt, timeStep);
					cmdSet = SatelliteUtils.getRotatorCommandSet(list, spt.getStartTime().getTime(), timeStep);
					addPath(worldWindCanvas, list);

					startTracking.setEnabled(true);
				} catch(Exception e2){
					e2.printStackTrace();
					System.err.println("Unable to track satellite path! Exiting...");
					System.exit(1);
				}
			}
		});

		satChooseBox.setEditable(false);
		satChooseBox.setFocusable(false);
		satChooseBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				startTracking.setEnabled(false);
			}
		});

		final JButton connectButton = new JButton("Connect to serial server");
		connectButton.setFocusable(false);
		connectButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				String serverIP = JOptionPane.showInputDialog(null, new LabelText("Enter server IP: "),
						"penthouse.aere.iastate.edu");
				if(serverIP != null){
					String portNum = JOptionPane.showInputDialog(null, new LabelText("Enter port number: "), "2809");
					if(portNum != null && new Scanner(portNum).hasNextInt()){
						String username = JOptionPane.showInputDialog(null, new LabelText("Enter username: "), "joe");
						if(username != null){
							String password = JOptionPane.showInputDialog(null, new LabelText("Enter password: "),
									"password23");
							if(password != null){
								client = new SerialClient(serverIP, Integer.parseInt(portNum), username, password);
								if(client.getState() == SerialClient.State.ALIVE){
									rotator = new AntennaRotator(client, 0);
									JOptionPane.showMessageDialog(null, "Success! Server connection established :D");
									connectButton.setEnabled(false);
								} else if(client.getState() == SerialClient.State.DEAD){
									JOptionPane.showMessageDialog(null, "Unable to connect to " + serverIP + ":"
											+ portNum);
								} else if(client.getState() == SerialClient.State.INVALID_PASSWORD){
									JOptionPane.showMessageDialog(null, "Invalid username or password!");
								}
							}
						}
					} else if(portNum != null){
						JOptionPane.showMessageDialog(null, "Invalid port number: " + portNum);
					}
				}
			}
		});

		panel.add(connectButton);

		panel.add(satLabelText);
		panel.add(satChooseBox);
		panel.add(pathButton);
		panel.add(startTracking);

		frame.add(panel, BorderLayout.NORTH);
		frame.add(worldWindCanvas);
	}

	private void addCompass(WorldWindowGLCanvas worldWindCanvas){
		ArrayList<Position> positions = new ArrayList<Position>();
		for(int i = -6533000; i < 1000000; i += 100000){
			positions.add(Position.fromDegrees(0, 0, i));
		}

		positions.add(Position.fromDegrees(0, -5, 1000000));
		positions.add(Position.fromDegrees(0, 5, 1000000));

		// create "Polyline" with list of "Position" and set color / thickness
		Polyline polyline = new Polyline(positions);
		polyline.setColor(Color.BLUE);
		polyline.setLineWidth(4.0);

		// create a layer and add Polyline
		RenderableLayer layer = new RenderableLayer();
		layer.addRenderable(polyline);

		// add layer to WorldWind
		worldWindCanvas.getModel().getLayers().add(layer);
	}

	private void addPath(WorldWindowGLCanvas worldWindCanvas, List<AzElPair> list){
		LinkedList<Position> positions = new LinkedList<Position>();
		for(AzElPair latLong : list){
			positions.add(Position.fromDegrees(latLong.getEl(), -latLong.getAz(), 100000));
		}

		// create "Polyline" with list of "Position" and set color / thickness
		Polyline polyline = new Polyline(positions.subList(0, positions.size() / 2 + 1));
		polyline.setColor(Color.RED);
		polyline.setLineWidth(4.0);

		// create a layer and add Polyline
		RenderableLayer layer = new RenderableLayer();
		layer.addRenderable(polyline);

		// add layer to WorldWind
		worldWindCanvas.getModel().getLayers().add(layer);

		// create "Polyline" with list of "Position" and set color / thickness
		polyline = new Polyline(positions.subList(positions.size() / 2, positions.size()));
		polyline.setColor(Color.GREEN);
		polyline.setLineWidth(4.0);

		// create a layer and add Polyline
		layer = new RenderableLayer();
		layer.addRenderable(polyline);

		// add layer to WorldWind
		worldWindCanvas.getModel().getLayers().add(layer);
	}
}
