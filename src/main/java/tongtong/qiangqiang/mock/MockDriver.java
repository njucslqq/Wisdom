package tongtong.qiangqiang.mock;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.datacenter.DataCenterUtil;
import com.google.common.collect.ImmutableList;
import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies5;
import tongtong.qiangqiang.data.indicator.SuperIndicator;
import tongtong.qiangqiang.data.indicator.advance.*;
import tongtong.qiangqiang.data.indicator.basic.*;
import tongtong.qiangqiang.hunt.LearningDirection;
import tongtong.qiangqiang.hunt.LearningDirection.WaveletConfig;
import tongtong.qiangqiang.vis.TimeSeriesChart;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.Historical.bars;
import static tongtong.qiangqiang.hunt.LearningDirection.Direction;
import static tongtong.qiangqiang.hunt.LearningDirection.Direction.*;
import static tongtong.qiangqiang.hunt.LearningDirection.j48;

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

    @Override
    void init() {
        setSecurity(code);
        setResolution(MIN_1);
        setStart(of(2016, 2, 1));
        setEnd(of(2016, 2, 2));

        LocalDate startTime = of(2015, 10, 20);
        LocalDate endTime = of(2016, 1, 29);
        List<BarInfo> data = bars(code, MIN_1, startTime, endTime);

        int size = 128;
        int top = 2;
        int gap = 17;
        Transform t = new Transform(new FastWaveletTransform(new Daubechies5()));
        WaveletConfig config = new WaveletConfig(t, size, top, gap);

        int leaf = 511;
        LearningDirection.generateTrain(data, indicators, config, train, false);
        LearningDirection.crossValidate(train, j48(leaf));
        LearningDirection.validateModel(train, train, j48(leaf));
        m_classifier = LearningDirection.buildClassifier(train, j48(leaf));
    }

    @Override
    void onData(BaseData dataUnit, int index) {
        BarInfo bar = (BarInfo) dataUnit;
        close.add(bar.closePrice);

        LearningDirection.appendGenerate(test, indicators, bar);

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
            if (size >= 2 && dir.get(size - 1) == UP && dir.get(size - 2) == UP){
                buyClose(bar.closePrice);
                buyOpen(bar.closePrice);
            }

            if (size >= 2 && dir.get(size - 1) == DOWN && dir.get(size - 2) == DOWN){
                sellClose(bar.closePrice);
                sellOpen(bar.closePrice);
            }

            comp.vis("HH-mm", close.subList(0, index + 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
