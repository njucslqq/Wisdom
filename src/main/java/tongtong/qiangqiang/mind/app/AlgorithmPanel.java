package tongtong.qiangqiang.mind.app;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import tongtong.qiangqiang.mind.Algorithm;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.ZoneId.systemDefault;
import static java.util.Date.from;
import static org.jfree.chart.ChartFactory.createTimeSeriesChart;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-03.
 */
public class AlgorithmPanel extends JPanel {

    public final Algorithm algorithm;

    public final ChartPanel price;

    public final ChartPanel profit;

    public final JTextArea writer = new JTextArea();

    private final JScrollPane scroll = new JScrollPane(writer);

    private final JPanel log = new JPanel();

    public AlgorithmPanel(Algorithm algorithm) {
        super(new GridLayout(3, 1), false);
        this.algorithm = algorithm;

        price = new ChartPanel(createChart("Price Change", "Time", "Price"));
        profit = new ChartPanel(createChart("Profit Change", "Time", "Profit"));
        price.setFillZoomRectangle(true);
        price.setMouseWheelEnabled(true);
        profit.setFillZoomRectangle(true);
        profit.setMouseWheelEnabled(true);

        writer.setTabSize(4);
        writer.setLineWrap(true);
        writer.setWrapStyleWord(true);
        writer.setBackground(Color.white);
        writer.setEditable(false);
        writer.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                writer.setCaretPosition(writer.getText().length());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }
        });

        scroll.setAutoscrolls(true);
        scroll.setEnabled(true);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        log.setBorder(BorderFactory.createTitledBorder("Logs"));
        log.setLayout(new GridLayout(1, 1));
        log.add(scroll);

        add(price);
        add(profit);
        add(log);

        algorithm.setPanel(this);
    }

    public void visPrice(Pair<String, List<Double>>... series) {
        price.setChart(createChart("Price Change", "time", "price", series));
    }

    public void visProfit(Pair<String, List<Double>> value) {
        profit.setChart(createChart("Profit Change", "time", "profit", value));
    }

    public static JFreeChart createChart(String title, String xLabel, String yLabel, Pair<String, List<Double>>... series) {
        return createChart(createData(series), title, xLabel, yLabel);
    }

    public static JFreeChart createChart(XYDataset dataSet, String title, String xLabel, String yLabel) {
        JFreeChart chart = createTimeSeriesChart(title, xLabel, yLabel, dataSet, true, true, false);
        chart.setBackgroundPaint(Color.white);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0D, 5.0D, 5.0D, 5.0D));
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
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));
        return chart;
    }

    public static TimeSeriesCollection createData(Pair<String, List<Double>>... series) {
        LocalDateTime start = LocalDateTime.now();
        TimeSeriesCollection collection = new TimeSeriesCollection();
        for (Pair<String, List<Double>> s : series) {
            TimeSeries ts = new TimeSeries(s.getKey());
            for (int j = 0; j < s.getValue().size(); j++)
                ts.add(new Second(from(start.plusSeconds(j).atZone(systemDefault()).toInstant())), s.getValue().get(j));
            collection.addSeries(ts);
        }
        return collection;
    }
}
