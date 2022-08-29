package DataAnalytics;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.xml.DatasetReader;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class LineChart extends JFrame {

    public LineChart(String applicationTitle, String chartTitle, String seriesName, String xAxisName, String yAxisName, ArrayList<Integer> data) {
        super(applicationTitle);
        JFreeChart lineChart = ChartFactory.createXYLineChart(
                chartTitle,
                xAxisName,yAxisName,
                createDataset(seriesName, data),
                PlotOrientation.VERTICAL,
                true,true,false);

        ChartPanel chartPanel = new ChartPanel( lineChart );
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
        setContentPane(chartPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private XYDataset createDataset(String seriesName, ArrayList<Integer> data) {
        final XYSeries series = new XYSeries(seriesName);
        int counter = 0;
        for(Integer number : data){
            series.add(counter, number);
            counter++;
        }
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

    public static void main( String[ ] args ) {
        LineChart.Create(
                "Test Title" ,
                "Test Chart Title",
                "Test X-axis Name",
                "Test Y-axis Name",
                "Test Series Name",
                new ArrayList<>(Arrays.asList(1,1,1,0,0)));
    }

    public static void Create(String applicationTitle, String chartTitle, String seriesName, String xAxisName, String yAxisName, ArrayList<Integer> data){
        LineChart chart = new LineChart(
                applicationTitle ,
                chartTitle,
                seriesName,
                xAxisName,
                yAxisName,
                data);
        chart.pack();
        chart.setVisible( true );
    }
}
