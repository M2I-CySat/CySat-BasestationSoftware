package orbits;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class TrackerTest {
	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				createAndShowGUI(new double[]{ 1, 2, 3, 4, 5 }, new double[]{ 1, 4, 9, 16, 25 });
			}
		});
	}

	public static void createAndShowGUI(double[] azs, double[] els){
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((scrDim.width - frame.getWidth()) / 2, (scrDim.height - frame.getHeight()) / 2);

		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		XYSeries series = new XYSeries("Azs & els", false);
		for(int i = 0; i < azs.length && i < els.length; i++){
			series.add(azs[i], els[i]);
		}
		
		xySeriesCollection.addSeries(series);

		JFreeChart chart = ChartFactory.createScatterPlot("Azs & Els", "Azs", "Els", xySeriesCollection,
				PlotOrientation.VERTICAL, false, false, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesLinesVisible(0, true);
		plot.setRenderer(renderer);

		ValueAxis rangeAxis = plot.getRangeAxis();

		rangeAxis.setRange(0.0, 90.0);
		
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		frame.add(chartPanel);

		frame.setVisible(true);
	}
}
