package tongtong.qiangqiang.mock;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.datacenter.DataCenterUtil;
import com.google.common.collect.ImmutableList;
import tongtong.qiangqiang.data.FileEcho;
import tongtong.qiangqiang.data.indicator.SuperIndicator;
import tongtong.qiangqiang.data.indicator.advance.*;
import tongtong.qiangqiang.data.indicator.basic.*;
import tongtong.qiangqiang.hunt.Learning;
import tongtong.qiangqiang.vis.TimeSeriesChart;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.Historical.bars;
import static tongtong.qiangqiang.hunt.Learning.Direction;
import static tongtong.qiangqiang.hunt.Learning.Direction.*;
import static tongtong.qiangqiang.hunt.Learning.writeAttributes;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public class MockDriver extends MockBase {

    String code = "rb1605";

    String train = "./signal/learning-train.arff";

    String test = "./signal/learning-test.arff";

    TimeSeriesChart comp = new TimeSeriesChart("Comparison");

    J48 m_classifier = new J48();

    LinkedList<Double> close = new LinkedList<>();

    LinkedList<Direction> dir = new LinkedList<>();

    FileEcho echo = null;

    List<SuperIndicator> indicators = ImmutableList.of(
            new WMA(5), new WMA(7), new WMA(9), new WMA(11), new WMA(13), new WMA(15), new WMA(17), new WMA(21), new WMA(27), new WMA(31), new WMA(41), new WMA(53), new WMA(61), new WMA(67), new WMA(73), new WMA(79), new WMA(87), new WMA(93),
            new MACD(11, 23, 7), new MACD(15, 27, 9), new MACD(19, 33, 13), new MACD(23, 39, 19), new MACD(33, 43, 31), new MACD(41, 53, 39), new MACD(53, 67, 43), new MACD(61, 73, 53), new MACD(71, 83, 63), new MACD(83, 97, 71),
            new EMA(7), new EMA(13), new EMA(17), new EMA(21), new EMA(25), new EMA(29), new EMA(33), new EMA(39), new EMA(43), new EMA(51), new EMA(61), new EMA(67), new EMA(79), new EMA(87), new EMA(97),
            new WilliamsR(11), new WilliamsR(13), new WilliamsR(17), new WilliamsR(23), new WilliamsR(35), new WilliamsR(39), new WilliamsR(47), new WilliamsR(59), new WilliamsR(73), new WilliamsR(87),
            new BOLL(7, 0.15), new BOLL(13, 0.15), new BOLL(17, 0.15), new BOLL(23, 0.15), new BOLL(27, 0.15), new BOLL(34, 0.15), new BOLL(43, 0.15), new BOLL(51, 0.15), new BOLL(62, 0.15), new BOLL(79, 0.15), new BOLL(87, 0.15),
            new MTM(),
            new DMA(7, 13, 5), new DMA(13, 17, 9), new DMA(17, 25, 13), new DMA(25, 34, 21), new DMA(34, 47, 31), new DMA(47, 59, 31), new DMA(59, 71, 41), new DMA(67, 81, 51), new DMA(79, 91, 61), new DMA(89, 101, 71),
            new OSC(7), new OSC(11), new OSC(15), new OSC(21), new OSC(27), new OSC(34), new OSC(41), new OSC(57), new OSC(69), new OSC(81), new OSC(91),
            new RSI(11), new RSI(15), new RSI(21), new RSI(31), new RSI(41), new RSI(51), new RSI(67), new RSI(81), new RSI(91),
            new TRIX(13, 7), new TRIX(17, 11), new TRIX(27, 17), new TRIX(39, 21), new TRIX(47, 27), new TRIX(61, 39), new TRIX(73, 49), new TRIX(83, 59)
    );

    @Override
    void init() {
        setSecurity(code);
        setResolution(MIN_1);
        setStart(of(2016, 1, 26));
        setEnd(of(2016, 2, 2));
    }

    @Override
    void onData(BaseData dataUnit, int index) {
        BarInfo bar = (BarInfo) dataUnit;
        close.add(bar.closePrice);

        Map<String, BasicIndicator> attributes = null;
        if (echo == null)
            echo = writeAttributes(attributes, test);

        List<Object> line = new ArrayList<>();
        for (BasicIndicator ind : attributes.values())
            line.add(ind.data.getLast());
        line.add("?");
        echo.writeln(line);

        try {
            ArffLoader arf = new ArffLoader();
            arf.setFile(new File(test));

            Instances instancesTest = arf.getDataSet();
            instancesTest.setClassIndex(instancesTest.numAttributes() - 1);

            double predicted = m_classifier.classifyInstance(instancesTest.lastInstance());
            String clazz = instancesTest.classAttribute().value((int) predicted);
            Direction direction = valueOf(clazz);
            dir.add(direction);
            System.out.println("分类值： " + direction);
            comp.vis("HH-mm", close.subList(0, index + 1));

            //int size = dir.size();
            if (direction == UP) {//size >= 1 && dir.get(size - 1) == UP && dir.get(size - 2) == UP && !LONG)
                buyClose(bar.closePrice);
                buyOpen(bar.closePrice);
            }
            if (direction == DOWN){
                sellClose(bar.closePrice);
                sellOpen(bar.closePrice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    void onComplete() {
        System.out.println("多头盈利：" + longDiff);
        System.out.println("空头盈利：" + shortDiff);
        echo.close();
        //FileEcho echo = new FileEcho(BASE + FILE);
        /*for (Double d : price)
            echo.writeln(d);
        echo.close();*/
    }

    public static void main(String[] args) {
        DataCenterUtil.setNetDomain(CONST.INTRA_QUANDIS_URL);
        MockDriver m = new MockDriver();
        m.init();
        m.simulate();
        m.onComplete();
    }
}
