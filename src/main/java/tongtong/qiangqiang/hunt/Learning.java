package tongtong.qiangqiang.hunt;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.datacenter.DataCenterUtil;
import com.google.common.collect.ImmutableList;
import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies5;
import org.apache.commons.lang3.tuple.Pair;
import tongtong.qiangqiang.data.FileEcho;
import tongtong.qiangqiang.data.indicator.SuperIndicator;
import tongtong.qiangqiang.data.indicator.advance.*;
import tongtong.qiangqiang.data.indicator.basic.*;
import tongtong.qiangqiang.vis.TimeSeriesChart;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static java.lang.Runtime.getRuntime;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.Historical.bars;
import static tongtong.qiangqiang.func.GeneralUtilizer.extract;
import static tongtong.qiangqiang.hunt.Filter.lowPassFilter;
import static tongtong.qiangqiang.hunt.Learning.Direction.DOWN;
import static tongtong.qiangqiang.hunt.Learning.Direction.UP;
import static tongtong.qiangqiang.hunt.Learning.Stage.*;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-01-27.
 */
public class Learning {

    public enum Stage {
        TOP, BOTTOM, MIDDLE
    }

    public enum Direction {
        UP, DOWN
    }

    public static class WaveletConfig {

        public final Transform transform;

        public final int size;

        public final int top;

        public WaveletConfig(Transform transform, int size, int top) {
            this.transform = transform;
            this.size = size;
            this.top = top;
        }
    }

    public static List<Direction> judge(List<Double> output) {
        Stage[] stage = new Stage[output.size()];
        int offset = 1;
        double dif = 0.01;
        for (int i = 0; i < output.size(); i++) {
            int left = i - offset;
            int right = i + offset;
            if (left >= 0 && right < output.size())
                if (output.get(left) + dif < output.get(i) && output.get(right) + dif < output.get(i))
                    stage[i] = TOP;
                else if (output.get(left) - dif > output.get(i) && output.get(right) - dif > output.get(i))
                    stage[i] = BOTTOM;
                else
                    stage[i] = MIDDLE;
            else
                stage[i] = MIDDLE;
        }

        Direction[] dir = new Direction[stage.length];
        for (int i = 0; i < stage.length; i++) {
            if (stage[i] == TOP) {
                dir[i] = UP;
                //if (i - 1 >= 0) dir[i - 1] = UNKNOW;
                //if (i + 1 < stage.length) dir[i + 1] = UNKNOW;
                //i += 2;
            } else if (stage[i] == BOTTOM) {
                dir[i] = DOWN;
                //if (i - 1 >= 0) dir[i - 1] = UNKNOW;
                //if (i + 1 < stage.length) dir[i + 1] = UNKNOW;
                //i += 2;
            } else {
                if ((i - 1 >= 0 && output.get(i) > output.get(i - 1)) || (i + 1 < stage.length && output.get(i) < output.get(i + 1)))
                    dir[i] = UP;
                else
                    dir[i] = DOWN;
                //i++;
            }
        }
        return Arrays.asList(dir);
    }

    public static FileEcho writeAttributes(List<Pair<String, BasicIndicator>> attributes, String file) {
        FileEcho echo = new FileEcho(file);
        echo.writeln("@relation indicators-direction");
        for (Pair<String, BasicIndicator> p : attributes)
            echo.writeln("@attribute " + p.getLeft() + " real");
        echo.writeln("@attribute direction {UP, DOWN}");
        echo.writeln("@data");
        return echo;
    }

    public static int log2(int size) {
        int exp = 1, count = 0;
        while (exp < size) {
            exp <<= 1;
            count++;
        }
        return count;
    }

    public static RandomForest randomForest(int nFeatures, int nTrees) {
        RandomForest rf = new RandomForest();
        rf.setNumExecutionSlots(getRuntime().availableProcessors() * 2);
        rf.setNumFeatures(nFeatures);
        rf.setNumTrees(nTrees);
        rf.setSeed(3);
        return rf;
    }

    public static J48 j48(int nMin) {
        J48 c45 = new J48();
        c45.setReducedErrorPruning(true);
        c45.setMinNumObj(nMin);
        return c45;
    }

    public static MultilayerPerceptron multilayerPerceptron() {
        MultilayerPerceptron mp = new MultilayerPerceptron();
        mp.setAutoBuild(true);              //设置网络中的连接和隐层
        mp.setDecay(false);                 //如果为true会降低学习速率
        mp.setLearningRate(0.3);            //Weights被更新的数量,对预测结果影响很大
        mp.setMomentum(0.8);                //当更新weights时设置的动量
        mp.setNormalizeAttributes(true);    //可以优化网络性能
        mp.setNormalizeNumericClass(true);  //如果预测的是数值型可以提高网络的性能
        mp.setValidationSetSize(20);        //验证百分比，影响大
        mp.setNominalToBinaryFilter(true);  //可以提高性能
        return mp;
    }

    public static void generateTrain(List<BarInfo> bars, List<SuperIndicator> indicators, WaveletConfig config, String file) {
        int priori = indicators.get(0).size();

        List<Double> close = extract(bars, "closePrice");
        TimeSeriesChart original = new TimeSeriesChart("Original");
        TimeSeriesChart wavelet = new TimeSeriesChart("Wavelet");

        indicators.parallelStream().forEach(indicator -> bars.forEach(indicator::update));
        List<Pair<String, BasicIndicator>> attributes = new LinkedList<>();
        indicators.forEach(indicator -> attributes.addAll(indicator.fields("")));
        FileEcho echo = writeAttributes(attributes, file);

        int size = config.size;
        int top = config.top;
        int boundary = (int) (size / 10.0);
        int len = size - 2 * boundary;

        for (int i = boundary; i <= bars.size() - (len + boundary); i += len) {
            int from = i - boundary;
            List<Double> window = close.subList(from, from + size);
            List<Double> smooth = lowPassFilter(config.transform, window, top);
            List<Direction> direction = judge(smooth);
            for (int j=0; j<len; j++) {
                List<Object> line = new ArrayList<>();
                for (Pair<String, BasicIndicator> p : attributes)
                    line.add(p.getRight().data.get(j + i + priori));
                line.add(direction.get(j+boundary));
                echo.writeln(line);
            }

            /*try {
                original.vis("HH-mm", window);
                wavelet.vis("HH-mm", smooth);
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        echo.close();
    }

    public static void validateModel(String train, String test, Classifier classifier) {
        try {
            ArffLoader loader = new ArffLoader();
            File trainFile = new File(train);
            File testFile = new File(test);

            loader.setFile(trainFile);
            Instances instancesTrain = loader.getDataSet();
            instancesTrain.setClassIndex(instancesTrain.numAttributes() - 1);

            loader.setFile(testFile);
            Instances instancesTest = loader.getDataSet();
            instancesTest.setClassIndex(instancesTest.numAttributes() - 1);

            classifier.buildClassifier(instancesTrain);

            Evaluation eval = new Evaluation(instancesTrain);
            eval.evaluateModel(classifier, instancesTest);
            System.out.println(eval.toSummaryString("=== Summary ===\n", false));
            System.out.println(eval.toClassDetailsString("=== Details ===\n"));
            System.out.println(eval.toMatrixString("=== Confusion Matrix ===\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void crossValidate(String train, Classifier classifier) {
        try {
            ArffLoader loader = new ArffLoader();
            File trainFile = new File(train);

            loader.setFile(trainFile);
            Instances instancesTrain = loader.getDataSet();
            instancesTrain.setClassIndex(instancesTrain.numAttributes() - 1);

            classifier.buildClassifier(instancesTrain);

            Evaluation eval = new Evaluation(instancesTrain);
            eval.crossValidateModel(classifier, instancesTrain, 10, new Random(3));
            System.out.println(eval.toSummaryString("=== Summary ===\n", false));
            System.out.println(eval.toClassDetailsString("=== Details ===\n"));
            System.out.println(eval.toMatrixString("=== Confusion Matrix ===\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DataCenterUtil.setNetDomain(CONST.INTRA_QUANDIS_URL);

        String train = "./../../signal/learning-train.arff";
        String test = "./../../signal/learning-test.arff";

        String code = "p1605";
        LocalDate start = of(2015, 12, 7);
        LocalDate end = of(2016, 1, 18);
        LocalDate extra = of(2016, 1, 19);

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

        int size = 512;
        int top = 3;
        Transform t = new Transform(new FastWaveletTransform(new Daubechies5()));
        WaveletConfig config = new WaveletConfig(t, size, top);

        List<BarInfo> data = bars(code, MIN_1, start, end);
        generateTrain(data, indicators, config, train);
        crossValidate(train, randomForest(20, 257));
        crossValidate(train, j48(3));

        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        List<BarInfo> testData = bars(code, MIN_1, end.plusDays(1), extra);
        generateTrain(testData, indicators, config, test);
        validateModel(train, test, randomForest(20, 257));
        validateModel(train, test, j48(3));
    }
}
