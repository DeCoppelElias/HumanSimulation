package DataAnalytics;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class LineChart extends JFrame {

    public LineChart(
            String applicationTitle,
            String chartTitle,
            String seriesName,
            String xAxisName,
            String yAxisName,
            ArrayList<Integer> data) {
        super(applicationTitle);
        JFreeChart lineChart = ChartFactory.createXYLineChart(
                chartTitle,
                xAxisName,
                yAxisName,
                createDataset(seriesName, data),
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        setContentPane(chartPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private XYDataset createDataset(String seriesName, ArrayList<Integer> data) {
        final XYSeries series = new XYSeries(seriesName);
        int counter = 0;
        for (Integer number : data) {
            series.add(counter, number);
            counter++;
        }
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

    public static void Create(
            String applicationTitle,
            String chartTitle,
            String seriesName,
            String xAxisName,
            String yAxisName,
            ArrayList<Integer> data) {
        LineChart chart = new LineChart(applicationTitle, chartTitle, seriesName, xAxisName, yAxisName, data);
        chart.pack();
        chart.setVisible(true);
    }
}
