package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import orbits.SatellitePass;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.HorizontalAlignment;

import uk.me.g4dpz.satellite.SatPos;
import util.SatelliteUtils;

@SuppressWarnings("serial")
public class NextPassesView extends JScrollPane {
	private JPanel viewPanel;
	private SatellitePass selectedPass;
	private Map<ChartPanel, SatellitePass> charts = new HashMap<>();
	
	private static final double ASPECT_RATIO = 1.4;
	private static final int PASS_PANE_WIDTH = 300;
	private static final int PASS_PANE_HEIGHT = (int) (PASS_PANE_WIDTH / ASPECT_RATIO);

	public NextPassesView() {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		viewPanel = new JPanel(){
//			@Override
//			public void paint(Graphics g){
//				super.paint(g);
//			}
			
			@Override
			public void paintComponent(Graphics g){
				super.paintComponent(g);
			}
		};
		viewPanel.setLayout(new GridLayout(0, 1));

		setViewportView(viewPanel);
		getVerticalScrollBar().setUnitIncrement(10);
		setBackground(Color.LIGHT_GRAY);

		// doNextPasses("ISS (ZARYA)", 5);
		// doNextPass("NOAA 6 [P]");
		// doNextPass("NOAA 7 [-]");
		// doNextPass("NOAA 8 [-]");
		// doNextPass("NOAA 9 [P]");
		// doNextPass("NOAA 10 [-]");
		// doNextPass("NOAA 11 [-]");
	}

	public void showNextPasses(String satName, int nPasses){
		int timeStep = 10;
		List<SatellitePass> passes = SatelliteUtils.getNextSatellitePasses(satName, timeStep, nPasses, SatelliteUtils.MIN_ELEV);
		if(passes != null){
			for(SatellitePass pass : passes){
				doNextPass(pass, timeStep);
			}
		}
	}

	public void removePass(int index){
		viewPanel.remove(index);
		refresh();
	}
	
	public void removeAllPasses(){
		viewPanel.removeAll();
		selectedPass = null;
		refresh();
	}
	
	public void refresh(){
		viewPanel.invalidate();
		invalidate();
		repaint();
	}

	private void doNextPass(SatellitePass pass, int timeStep){
		if(pass != null){
			List<SatPos> passPoints = pass.getPassPoints(SatelliteUtils.MIN_ELEV);
			
			if(passPoints != null && !passPoints.isEmpty()) {
				TimeSeries ts = new TimeSeries("Pass Info");
				for(int i = 0; i < passPoints.size(); i++){
					ts.add(new FixedMillisecond(passPoints.get(i).getTime()), passPoints.get(i).getElevation());
				}
	
				TimeSeriesCollection dataset = new TimeSeriesCollection();
				dataset.addSeries(ts);
	
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				sdf.setTimeZone(TimeZone.getTimeZone("US/Central"));
				String title = sdf.format(passPoints.get(0).getTime());
				System.out.println(title);
				final JFreeChart chart = ChartFactory.createTimeSeriesChart(title, "Time", "Elevation", dataset, false, false,
						false);
				
				String subtitleText = "-- Max Elev: " + String.format("%.2f", pass.getSatPassTime().getMaxEl()) + "\u00B0 --";
				chart.setSubtitles(Arrays.asList(new TextTitle(subtitleText)));
				XYPlot plot = (XYPlot) chart.getPlot();
				XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
				renderer.setSeriesLinesVisible(0, true);
				plot.setRenderer(renderer);
	
				ValueAxis rangeAxis = plot.getRangeAxis();
				rangeAxis.setRange(0.0, 90.0);
				
				final ChartPanel chartPanel = new ChartPanel(chart);
				chartPanel.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						selectChart(chartPanel);
						System.out.println(getSelectedPass());
					}
				});
				chartPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
				chartPanel.setPreferredSize(new Dimension(PASS_PANE_WIDTH, PASS_PANE_HEIGHT));
				
				chartPanel.setDomainZoomable(false);
				chartPanel.setRangeZoomable(false);
				
		        TextTitle t = chart.getTitle();
		        t.setHorizontalAlignment(HorizontalAlignment.CENTER);
		        t.setPaint(new Color(0, 0, 130));
		        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		        
		        viewPanel.add(chartPanel);
		        charts.put(chartPanel, pass);
		        if(selectedPass == null) {
		        	selectChart(chartPanel);
		        }
			}
		}
	}
	
	private void selectChart(ChartPanel chartPanel) {
		for(ChartPanel cp : charts.keySet()) {
			cp.getChart().setBackgroundPaint(null);
		}
		
		if (chartPanel != null) {
			chartPanel.getChart().setBackgroundPaint(new Color(100, 150, 230));
			selectedPass = charts.get(chartPanel);
		}
	}
	
	public SatellitePass getSelectedPass() {
		return selectedPass;
	}
}