package tongtong.qiangqiang.mock;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.datacenter.DataCenterUtil;
import com.google.common.collect.ImmutableList;
import tongtong.qiangqiang.data.factor.advance.indicators.DEMA;
import tongtong.qiangqiang.data.factor.advance.indicators.DMA;
import tongtong.qiangqiang.data.factor.advance.indicators.MACD;
import tongtong.qiangqiang.data.factor.basic.indicators.EMA;
import tongtong.qiangqiang.data.factor.basic.indicators.WMA;
import tongtong.qiangqiang.vis.TimeSeriesChart;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;

import java.util.*;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_5;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.Historical.bars;
import static tongtong.qiangqiang.hunt.LearningDirection.Direction;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public class MockDriver extends MockBase {

    String code = "rb1605";

    String train = "./../../signal/learning-train-driver.arff";

    String test = "./../../signal/learning-predict-driver.arff";

    TimeSeriesChart comp = new TimeSeriesChart("Comparison");

    Classifier m_classifier = new J48();

    LinkedList<Double> close = new LinkedList<>();

    LinkedList<Direction> dir = new LinkedList<>();

    List<SuperIndicator> indicators = ImmutableList.of(
            new WMA(5), new WMA(7), new WMA(9), new WMA(11), new WMA(13), new WMA(15), new WMA(17), new WMA(21), new WMA(27), new WMA(31),
            new MACD(7, 17, 5), new MACD(11, 23, 7), new MACD(15, 27, 9), new MACD(19, 33, 13), new MACD(23, 39, 19), new MACD(33, 43, 31),
            new EMA(7), new EMA(13), new EMA(17), new EMA(21), new EMA(25), new EMA(29), new EMA(33),
            new DMA(7, 13, 5), new DMA(13, 17, 9), new DMA(17, 25, 13), new DMA(25, 34, 21)
    );

    DEMA dema = new DEMA(21, 21);

    EMA ema = new EMA(21);

    WMA wma = new WMA(21);

    @Override
    void init() {
        setSecurity(code);
        setResolution(MIN_5);
        setStart(of(2016, 1, 1));
        setEnd(of(2016, 3, 2));
    }

    @Override
    void onData(BaseData dataUnit, int index) {
        BarInfo bar = (BarInfo) dataUnit;
        close.add(bar.closePrice);

        wma.update(bar);
        ema.update(bar);
        dema.update(bar);
        if (index > 150){
            wma.data.removeFirst();
            ema.data.removeFirst();
            dema.dema.data.removeFirst();
            close.removeFirst();
        }
        comp.vis("HH:mm:ss", wma.data, ema.data, dema.dema.data, close);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*LearningDirection.appendGenerate(test, indicators, bar);

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
            int size = dir.size();
            if (size >= 2 && dir.get(size - 1) == UP && dir.get(size - 2) == UP) {
                buyClose(bar.closePrice);
                buyOpen(bar.closePrice);
            }

            if (size >= 2 && dir.get(size - 1) == DOWN && dir.get(size - 2) == DOWN) {
                sellClose(bar.closePrice);
                sellOpen(bar.closePrice);
            }

            comp.vis("HH-mm", close.subList(0, index + 1));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    void onComplete() {
        System.out.println("多头盈利：" + longDiff);
        System.out.println("空头盈利：" + shortDiff);
    }

    public static void main(String[] args) {
        DataCenterUtil.setNetDomain(CONST.INTRA_QUANDIS_URL);
        MockDriver m = new MockDriver();
        m.init();
        m.simulate();
        m.onComplete();
    }
}
