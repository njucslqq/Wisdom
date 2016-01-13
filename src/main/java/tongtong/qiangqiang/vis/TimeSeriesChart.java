package tongtong.qiangqiang.vis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-01-13.
 */
public class TimeSeriesChart extends ApplicationFrame {

    static {
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
    }

    public TimeSeriesChart(String title) {
        super(title);
    }

    public void vis(String fmt, TimeSeries... series) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for (TimeSeries ts : series)
            dataset.addSeries(ts);

        JFreeChart chart = createChart(dataset, fmt);
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new java.awt.Dimension(1900, 1000));
        setContentPane(panel);

        pack();
        //RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    private JFreeChart createChart(XYDataset dataset, String fmt) {

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Time Series Data",  // title
                "Time",             // x-axis label
                "Price",   // y-axis label
                dataset,            // data
                true,               // create legend?
                true,               // generate tooltips?
                false               // generate URLs?
        );

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
        }

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat(fmt));

        return chart;

    }
}
