package tongtong.qiangqiang.hunt;

import cn.quanttech.quantera.common.datacenter.HistoricalData;
import cn.quanttech.quantera.common.datacenter.source.QuandisSource;
import cn.quanttech.quantera.common.factor.Indicator;
import cn.quanttech.quantera.common.factor.IndicatorIdentity;
import cn.quanttech.quantera.common.factor.composite.*;
import cn.quanttech.quantera.common.factor.single.indicators.*;
import cn.quanttech.quantera.common.type.data.TimeFrame;
import cn.quanttech.quantera.common.type.quotation.BarInfo;
import com.google.common.collect.ImmutableList;
import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies5;
import tongtong.qiangqiang.data.FileEcho;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static tongtong.qiangqiang.hunt.Filter.lowPassFilter;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/3/20.
 */
public class LearnDirection {

    public enum Stage {
        TOP, BOTTOM, MIDDLE
    }

    public enum Direction {
        UP(0), DOWN(1);

        private int value;

        Direction(int value){
            this.value = value;
        }

        public int value(){return value;}
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

    public static class DirectionResult {

        public final int start;

        public final int end;

        public final List<Direction> direction;

        public DirectionResult(int start, int end, List<Direction> direction) {
            this.start = start;
            this.end = end;
            this.direction = direction;
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
                    stage[i] = Stage.TOP;
                else if (output.get(left) - dif > output.get(i) && output.get(right) - dif > output.get(i))
                    stage[i] = Stage.BOTTOM;
                else
                    stage[i] = Stage.MIDDLE;
            else
                stage[i] = Stage.MIDDLE;
        }

        Direction[] dir = new Direction[stage.length];
        for (int i = 0; i < stage.length; i++) {
            if (stage[i] == Stage.TOP) {
                dir[i] = Direction.UP;
                //if (i - 1 >= 0) dir[i - 1] = UNKNOW;
                //if (i + 1 < stage.length) dir[i + 1] = UNKNOW;
                //i += 2;
            } else if (stage[i] == Stage.BOTTOM) {
                dir[i] = Direction.DOWN;
                //if (i - 1 >= 0) dir[i - 1] = UNKNOW;
                //if (i + 1 < stage.length) dir[i + 1] = UNKNOW;
                //i += 2;
            } else {
                if ((i - 1 >= 0 && output.get(i) > output.get(i - 1)) || (i + 1 < stage.length && output.get(i) < output.get(i + 1)))
                    dir[i] = Direction.UP;
                else
                    dir[i] = Direction.DOWN;
                //i++;
            }
        }
        return Arrays.asList(dir);
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
        rf.setNumExecutionSlots(Runtime.getRuntime().availableProcessors() * 2);
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

    public static DirectionResult judgeDirection(List<Double> data, WaveletConfig config) {
        List<Direction> res = new ArrayList<>();

        int size = config.size;
        int top = config.top;
        int gap = config.gap;
        int len = size - 2 * gap;

        int i = gap;
        for (; i <= data.size() - (len + gap); i += len) {
            int from = i - gap;
            List<Double> window = data.subList(from, from + size);
            List<Double> smooth = lowPassFilter(config.transform, window, top);
            List<Direction> direction = judge(smooth);
            res.addAll(direction.subList(gap, gap + len));
        }

        return new DirectionResult(gap, i - 1, res);
    }

    public static FileEcho writeAttributes(List<IndicatorIdentity> attributes, String file) {
        FileEcho echo = new FileEcho(file);
        echo.writeln("@relation indicators-direction");
        for (IndicatorIdentity p : attributes)
            echo.writeln("@attribute " + p.name + " real");
        echo.writeln("@attribute direction {UP, DOWN}");
        echo.writeln("@data");
        return echo;
    }

    public static void generateData(List<BarInfo> bars, List<Indicator<Double>> doubleIndicators, List<Indicator<BarInfo>> barIndicators, WaveletConfig config, String train, String test, double percentage) {
        List<Double> data = new ArrayList<>();
        bars.forEach(b -> {
            data.add(b.close);
            doubleIndicators.forEach(i -> i.update(b.close));
            barIndicators.forEach(i -> i.update(b));
        });
        DirectionResult dr = judgeDirection(data, config);

        List<IndicatorIdentity> all = new ArrayList<>();
        doubleIndicators.forEach(i -> all.addAll(i.member("double")));
        barIndicators.forEach(i -> all.addAll(i.member("bar")));

        FileEcho trainFile = writeAttributes(all, train);
        FileEcho testFile = writeAttributes(all, test);

        int total = dr.direction.size();
        int trainLen = (int) (total * percentage);
        int offset = dr.start;

        for (int i = 0; i < total; i++) {
            List<Object> line = new ArrayList<>();
            for (IndicatorIdentity p : all)
                line.add(p.indicator.first(offset + i));
            line.add(dr.direction.get(i));
            if (i < trainLen)
                trainFile.writeln(line);
            else
                testFile.writeln(line);
        }

        trainFile.close();
        testFile.close();
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

    public static void main(String[] args) {
        QuandisSource.def = new QuandisSource(QuandisSource.OUTRA);

        List<Indicator<Double>> i1 = ImmutableList.of(
                new SMA(7), new SMA(17), new SMA(29), new SMA(39), new SMA(67), new SMA(87), new SMA(107),
                //new EMA(7), new EMA(17), new EMA(29), new EMA(39), new EMA(67), new EMA(87), new EMA(107),
                new WMA(7), new WMA(11), new WMA(19), new WMA(31),  new WMA(67), new WMA(93),
                new MACD(9, 21, 5), new MACD(12, 26, 9), new MACD(21, 49, 15), new MACD(39, 69, 23), new MACD(49, 83, 31),
                new BIAS(11), new BIAS(17), new BIAS(27), new BIAS(39), new BIAS(59), new BIAS(87),
                new MTM(1), new MTM(3), new MTM(7), new MTM(15),
                new ROC(13), new ROC(19), new ROC(27), new ROC(41),
                //new DEMA(7), new DEMA(13), new DEMA(19), new DEMA(39),new DEMA(67), new DEMA(87), new DEMA(107),

                //new DMA(new DEMA(21), new EMA(21), new SMA(7)),
                //new DMA(new DEMA(21), new SMA(21), new SMA(7)),
                //new DMA(new DEMA(47), new EMA(47), new SMA(11)),
                //new DMA(new DEMA(47), new SMA(47), new SMA(11)),


                new OSC(11), new OSC(17), new OSC(27), new OSC(39), new OSC(59), new OSC(87),
                //new PSY(11), new PSY(17), new PSY(27), new PSY(39), new PSY(59), new PSY(87),
                new RSI(11), new RSI(17), new RSI(27), new RSI(39), new RSI(59), new RSI(87),
                new VOSC(12, 26), new VOSC(27, 49)
                //new TRIX(13, 7), new TRIX(17, 11), new TRIX(27, 17), new TRIX(39, 21), new TRIX(47, 27), new TRIX(61, 39), new TRIX(73, 49), new TRIX(83, 59)
        );

        List<Indicator<BarInfo>> i2 = ImmutableList.of(
                new PVI(),
                new NVI(),
                new WilliamsR(11), new WilliamsR(17), new WilliamsR(27), new WilliamsR(39), new WilliamsR(59), new WilliamsR(87),
                new ADVOL(11), new ADVOL(17), new ADVOL(27), new ADVOL(39), new ADVOL(59), new ADVOL(87),
                new ADX(11), new ADX(17), new ADX(27), new ADX(39), new ADX(59), new ADX(87),
                new ARBR(11), new ARBR(17), new ARBR(27), new ARBR(39), new ARBR(59), new ARBR(87),
                //new ASI(11), new ASI(17), new ASI(27), new ASI(39), new ASI(59), new ASI(87),
                new ATR(11), new ATR(17), new ATR(27), new ATR(39), new ATR(59), new ATR(87),
                new CCI(11), new CCI(17), new CCI(27), new CCI(39), new CCI(59), new CCI(87),
                new CR(11), new CR(17), new CR(27), new CR(39), new CR(59), new CR(87),

                //new MFI(11), new MFI(17), new MFI(27), new MFI(39), new MFI(59), new MFI(87),
                new PVT(11), new PVT(17), new PVT(27), new PVT(39), new PVT(59), new PVT(87)
                //new VR(11), new VR(17), new VR(27), new VR(39), new VR(59), new VR(87),
                //new WVAD(11), new WVAD(17), new WVAD(27), new WVAD(39), new WVAD(59), new WVAD(87)
        );

        String code = "rb1605";
        LocalDate start = LocalDate.of(2015, 5, 1);
        LocalDate end = LocalDate.of(2016, 3, 1);
        List<BarInfo> data = HistoricalData.bars(code, TimeFrame.MIN_15, start, end);

        String train = "./../../signal/learning-train.arff";
        String test = "./../../signal/learning-test.arff";

        int size = 512;
        int top = 3;
        int gap = 25;
        Transform t = new Transform(new FastWaveletTransform(new Daubechies5()));
        WaveletConfig config = new WaveletConfig(t, size, top, gap);

        generateData(data, i1, i2, config, train, test, 0.85);

        crossValidate(train, randomForest(7, 201));
        crossValidate(train, j48(13));

        validateModel(train, train, randomForest(7, 201));
        validateModel(train, train, j48(13));

        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        validateModel(train, test, randomForest(7, 201));
        validateModel(train, test, j48(13));
    }
}
