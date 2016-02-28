package tongtong.qiangqiang.hunt;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.datacenter.DataCenterUtil;
import com.google.common.collect.ImmutableList;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies5;
import org.apache.commons.lang3.tuple.Pair;
import tongtong.qiangqiang.data.FileEcho;
import tongtong.qiangqiang.data.factor.Indicator;
import tongtong.qiangqiang.data.factor.composite.*;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.MTM;
import tongtong.qiangqiang.data.factor.single.indicators.WMA;
import tongtong.qiangqiang.data.factor.single.indicators.WilliamsR;
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

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Runtime.getRuntime;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.Historical.bars;
import static tongtong.qiangqiang.func.GeneralUtilizer.extract;
import static tongtong.qiangqiang.hunt.Filter.lowPassFilter;
import static tongtong.qiangqiang.hunt.LearningCorner.Stage.*;
import static tongtong.qiangqiang.hunt.LearningCorner.Stage.BOTTOM;
import static tongtong.qiangqiang.hunt.LearningCorner.Stage.DOWN;
import static tongtong.qiangqiang.hunt.LearningCorner.Stage.UP;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-02-18.
 */
public class LearningCorner {

    public enum Stage {
        TOP, DOWN, BOTTOM, UP
    }

    public static class WaveletConfig {

        public final Transform transform;

        public final int size;

        public final int top;

        public final int gap;

        public WaveletConfig(Transform transform, int size, int top, int gap) {
            this.transform = transform;
            this.size = size;
            this.top = top;
            this.gap = gap;
        }
    }

    public static List<Stage> judge(List<Double> lst) {
        /**
         * decide TOP and BOTTOM
         */
        int n = lst.size();
        Stage[] stage = new Stage[n];

        for (int i = 0; i < n; i++) {
            if (i == 0)
                stage[i] = lst.get(i) < lst.get(i + 1) ? UP : DOWN;
            else if (i == n - 1)
                stage[i] = lst.get(i) < lst.get(i - 1) ? DOWN : UP;
            else {
                if (lst.get(i) < min(lst.get(i - 1), lst.get(i + 1)))
                    stage[i] = BOTTOM;
                if (lst.get(i) > max(lst.get(i - 1), lst.get(i + 1)))
                    stage[i] = BOTTOM;
            }
        }

        /**
         * decide UP and DOWN
         */
        int neighbour = 1;
        for (int i = 0; i < n - 1; )
            if (stage[i] == TOP || stage[i] == BOTTOM) {
                for (int j = i - neighbour; j <= i + neighbour; j++)
                    if (j >= 0 && j < n)
                        stage[j] = stage[i];
                i += neighbour + 1;
            } else {
                if (lst.get(i) < lst.get(i + 1))
                    stage[i] = UP;
                else
                    stage[i] = DOWN;
                i++;
            }

        return Arrays.asList(stage);
    }

    public static FileEcho writeAttributes(List<Pair<String, SingleIndicator>> attributes, String file) {
        FileEcho echo = new FileEcho(file);
        echo.writeln("@relation indicators-stage");
        for (Pair<String, SingleIndicator> p : attributes)
            echo.writeln("@attribute " + p.getLeft() + " real");
        echo.writeln("@attribute direction {TOP, BOTTOM, UP, DOWN}");
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
        c45.setUseLaplace(false);
        c45.setMinNumObj(nMin);
        return c45;
    }

    public static MultilayerPerceptron multilayerPerceptron() {
        MultilayerPerceptron mp = new MultilayerPerceptron();
        mp.setAutoBuild(true);
        mp.setDecay(false);
        mp.setLearningRate(0.3);
        mp.setMomentum(0.8);
        mp.setNormalizeAttributes(true);
        mp.setNormalizeNumericClass(true);
        mp.setValidationSetSize(20);
        mp.setNominalToBinaryFilter(true);
        return mp;
    }

    public static void generateTrain(List<BarInfo> bars, List<Indicator> indicators, WaveletConfig config, String file, boolean visualize) {
        int priori = indicators.get(0).dataSize();

        List<Double> close = extract(bars, "closePrice");
        TimeSeriesChart original = new TimeSeriesChart("Original");
        TimeSeriesChart wavelet = new TimeSeriesChart("Wavelet");

        indicators.parallelStream().forEach(indicator -> bars.forEach(indicator::update));
        List<Pair<String, SingleIndicator>> attributes = new LinkedList<>();
        indicators.forEach(indicator -> attributes.addAll(null));//indicator.fields("")));
        FileEcho echo = writeAttributes(attributes, file);

        Transform t = config.transform;
        int size = config.size;
        int top = config.top;
        int gap = config.gap;
        int len = size - 2 * gap;

        for (int i = 0; i < bars.size(); i += size) {
            if (i + size > bars.size())
                break;

            List<Double> window = close.subList(i, i + size);
            List<Double> smooth = lowPassFilter(t, window, top);
            List<Stage> stage = judge(smooth);

            int from = i + gap;
            for (int j = 0; j < len; j++) {
                List<Object> line = new ArrayList<>();
                for (Pair<String, SingleIndicator> p : attributes)
                    line.add(p.getRight().data.first(priori + i + gap + j));
                line.add(stage.get(j + gap));
                echo.writeln(line);
            }

            if (visualize) {
                try {
                    List<Double> fast = ((SingleIndicator) indicators.get(0)).data.sub(i + priori, i + priori + size);
                    List<Double> middle = ((SingleIndicator) indicators.get(2)).data.sub(i + priori, i + priori + size);
                    List<Double> slow = ((SingleIndicator) indicators.get(5)).data.sub(i + priori, i + priori + size);
                    original.vis("HH-mm",
                            fast.subList(gap, gap + len),
                            middle.subList(gap, gap + len),
                            slow.subList(gap, gap + len),
                            window.subList(gap, gap + len));
                    wavelet.vis("HH-mm", smooth.subList(gap, gap + len));
                    Thread.sleep(115000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
            System.out.println(classifier);

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

    public static Classifier buildClassifier(String train, Classifier classifier) {
        try {
            ArffLoader arf = new ArffLoader();
            arf.setFile(new File(train));

            Instances instancesTrain = arf.getDataSet();
            instancesTrain.setClassIndex(instancesTrain.numAttributes() - 1);

            classifier.buildClassifier(instancesTrain);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return classifier;
    }

    public static void main(String[] args) {
        DataCenterUtil.setNetDomain(CONST.INTRA_QUANDIS_URL);

        String train = "./../../signal/learning-train-1.arff";
        String test = "./../../signal/learning-test-1.arff";

        String code = "rb1605";
        LocalDate start = of(2015, 12, 20);
        LocalDate end = of(2016, 1, 2);
        LocalDate extra = of(2016, 1, 7);

        List<Indicator> indicators = ImmutableList.of(
                new WMA(5), new WMA(7), new WMA(9), new WMA(11), new WMA(13), new WMA(15), new WMA(17), new WMA(21), new WMA(27), new WMA(31), new WMA(41), new WMA(53), new WMA(61), new WMA(67), new WMA(73), new WMA(79), new WMA(87), new WMA(93),
                new MACD(11, 23, 7), new MACD(15, 27, 9), new MACD(19, 33, 13), new MACD(23, 39, 19), new MACD(33, 43, 31), new MACD(41, 53, 39), new MACD(53, 67, 43), new MACD(61, 73, 53), new MACD(71, 83, 63), new MACD(83, 97, 71),
                new EMA(7), new EMA(13), new EMA(17), new EMA(21), new EMA(25), new EMA(29), new EMA(33), new EMA(39), new EMA(43), new EMA(51), new EMA(61), new EMA(67), new EMA(79), new EMA(87), new EMA(97),
                new WilliamsR(11), new WilliamsR(13), new WilliamsR(17), new WilliamsR(23), new WilliamsR(35), new WilliamsR(39), new WilliamsR(47), new WilliamsR(59), new WilliamsR(73), new WilliamsR(87),
                new MTM(3),
                new DMA(7, 13, 5), new DMA(13, 17, 9), new DMA(17, 25, 13), new DMA(25, 34, 21), new DMA(34, 47, 31), new DMA(47, 59, 31), new DMA(59, 71, 41), new DMA(67, 81, 51), new DMA(79, 91, 61), new DMA(89, 101, 71),
                new OSC(7), new OSC(11), new OSC(15), new OSC(21), new OSC(27), new OSC(34), new OSC(41), new OSC(57), new OSC(69), new OSC(81), new OSC(91),
                new RSI(11), new RSI(15), new RSI(21), new RSI(31), new RSI(41), new RSI(51), new RSI(67), new RSI(81), new RSI(91),
                new TRIX(13, 7), new TRIX(17, 11), new TRIX(27, 17), new TRIX(39, 21), new TRIX(47, 27), new TRIX(61, 39), new TRIX(73, 49), new TRIX(83, 59)
        );

        List<Indicator> indicators_test = ImmutableList.of(
                new WMA(5), new WMA(7), new WMA(9), new WMA(11), new WMA(13), new WMA(15), new WMA(17), new WMA(21), new WMA(27), new WMA(31),
                new MACD(7, 17, 5), new MACD(11, 23, 7), new MACD(15, 27, 9), new MACD(19, 33, 13), new MACD(23, 39, 19), new MACD(33, 43, 31),
                new EMA(7), new EMA(13), new EMA(17), new EMA(21), new EMA(25), new EMA(29), new EMA(33),
                new DMA(7, 13, 5), new DMA(13, 17, 9), new DMA(17, 25, 13), new DMA(25, 34, 21)
        );

        int size = 256;
        int top = 4;
        int gap = 17;
        Transform t = new Transform(new FastWaveletTransform(new Daubechies5()));
        WaveletConfig config = new WaveletConfig(t, size, top, gap);

        List<BarInfo> data = bars(code, MIN_1, start, end);
        generateTrain(data, indicators_test, config, train, true);
        //crossValidate(train, randomForest(7, 201));
        crossValidate(train, j48(11));

        //validateModel(train, train, randomForest(7, 201));
        validateModel(train, train, j48(11));

        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        MInteger mi = new MInteger();
        Core c = new Core();


        List<BarInfo> testData = bars(code, MIN_1, end.plusDays(1), extra);
        generateTrain(testData, indicators_test, config, test, false);
        //validateModel(train, test, randomForest(7, 201));
        validateModel(train, test, j48(11));
    }
}
