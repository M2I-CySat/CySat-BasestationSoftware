package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

import util.SatelliteUtils;

@SuppressWarnings("serial")
public class NextPassesView extends JScrollPane {
	private JPanel viewPanel;
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
		List<SatellitePass> passes = SatelliteUtils.getNextSatellitePasses(satName, timeStep, nPasses);
		if(passes == null){
			return;
		}

		for(SatellitePass pass : passes){
			doNextPass(pass, timeStep);
		}
	}

	public void removePass(int index){
		viewPanel.remove(index);
		refresh();
	}
	
	public void removeAllPasses(){
		viewPanel.removeAll();
		refresh();
	}
	
	public void refresh(){
		viewPanel.invalidate();
		System.out.println("INVALIDATING!!!");
		invalidate();
		repaint();
	}

	private void doNextPass(SatellitePass pass, int timeStep){
		if(pass != null){
			int duration = SatelliteUtils.getDuration(pass.getSatPassTime());
			int nPoints = duration / timeStep;

			double[] x = new double[nPoints];
			double[] y = new double[nPoints];
			if(pass.getPassPoints() != null){
				for(int i = 0; i < nPoints; i++){
					x[i] = (int) ((double) i / nPoints * duration);
					y[i] = pass.getPassPoints().get(i).getEl();
				}
			}

			TimeSeries ts = new TimeSeries("Pass Info");
			for(int i = 0; i < x.length && i < y.length; i++){
				ts.add(new FixedMillisecond(new Date(pass.getSatPassTime().getStartTime().getTime() + 1000 * i
						* duration / x.length)), y[i]);
			}

			TimeSeriesCollection dataset = new TimeSeriesCollection();
			dataset.addSeries(ts);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("US/Central"));
			String title = sdf.format(pass.getSatPassTime().getStartTime().getTime());
			System.out.println(title);
			JFreeChart chart = ChartFactory.createTimeSeriesChart(title, "Time", "Elevation", dataset, false, false,
					false);
			XYPlot plot = (XYPlot) chart.getPlot();
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
			renderer.setSeriesLinesVisible(0, true);
			plot.setRenderer(renderer);

			ValueAxis rangeAxis = plot.getRangeAxis();
			rangeAxis.setRange(0.0, 90.0);
			
			ChartPanel chartPanel = new ChartPanel(chart);
			chartPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
			chartPanel.setPreferredSize(new Dimension(PASS_PANE_WIDTH, PASS_PANE_HEIGHT));
			
			chartPanel.setDomainZoomable(false);
			chartPanel.setRangeZoomable(false);
			
	        TextTitle t = chart.getTitle();
	        t.setHorizontalAlignment(HorizontalAlignment.CENTER);
	        t.setPaint(new Color(0, 0, 130));
	        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
	        
	        viewPanel.add(chartPanel);
		}
	}
}