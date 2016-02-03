package tongtong.qiangqiang.hunt;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.TimeFrame;
import cn.quanttech.quantera.datacenter.DataCenter;
import cn.quanttech.quantera.datacenter.DataCenterUtil;
import com.google.common.collect.ImmutableList;
import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.*;
import tongtong.qiangqiang.data.FileEcho;
import tongtong.qiangqiang.data.Historical;
import tongtong.qiangqiang.data.indicator.SuperIndicator;
import tongtong.qiangqiang.data.indicator.advance.*;
import tongtong.qiangqiang.data.indicator.basic.*;
import tongtong.qiangqiang.func.GeneralUtilizer;
import tongtong.qiangqiang.vis.TimeSeriesChart;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static java.time.LocalDate.MIN;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.Historical.bars;
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

    public static Map<String, BasicIndicator> calculate(List<BarInfo> data, List<SuperIndicator> indicators) {
        for (BarInfo b : data)
            indicators.parallelStream().forEach(si -> si.update(b));

        Map<String, BasicIndicator> attributes = new ConcurrentHashMap<>();
        indicators.parallelStream().forEach(si -> attributes.putAll(si.fields("")));

        return attributes;
    }

    public static void generate(List<BarInfo> data, Map<String, BasicIndicator> attributes, int from, int end, String file) {
        List<Double> close = GeneralUtilizer.extract(data, "closePrice");
        WMA wma = new WMA(3);
        for (Double d : close)
            wma.update(d);
        List<Double> smooth = wma.data;
        FileEcho echo = attributes(attributes, file);
        TimeSeriesChart comp = new TimeSeriesChart("Comparison");
        TimeSeriesChart wave = new TimeSeriesChart("wavelet");
        int size = 128;
        int top = 3;
        for (int i = from; i < end; i += size) {
            int to = Math.min(i + size, end);
            if (to - i != size)
                continue;
            Transform t = new Transform(new FastWaveletTransform(new Daubechies5()));
            List<Double> input = Filter.lowPassFilter(t, close.subList(i, i + size), top);
            List<Direction> dir = judge(input);
            for (int j = 10; j < size - 10; j++) {
                List<Object> line = new ArrayList<>();
                for (BasicIndicator ind : attributes.values())
                    line.add(ind.data.get(j + i));
                line.add(dir.get(j));
                echo.writeln(line);
            }

            /*comp.vis("HH-mm", close.subList(i, i + size), smooth.subList(i, i + size));//, output);
            wave.vis("HH-mm", input);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        echo.close();
    }

    public static FileEcho attributes(Map<String, BasicIndicator> attributes, String file) {
        FileEcho echo = new FileEcho(file);
        echo.writeln("@relation direction");
        for (String key : attributes.keySet())
            echo.writeln("@attribute " + key + " real");
        echo.writeln("@attribute direction {UP, DOWN}");
        echo.writeln("@data");
        return echo;
    }

    public static void predict(String train, List<SuperIndicator> indicators, String code, TimeFrame resolution, LocalDate date, String test) {
        ArffLoader arf = new ArffLoader();
        File input = new File(train);
        try {
            arf.setFile(input);
            Instances instancesTrain = arf.getDataSet();
            instancesTrain.setClassIndex(instancesTrain.numAttributes() - 1);

            J48 m_classifier = new J48();
            String options[] = new String[3];
            options[0] = "-R";
            options[1] = "-M";
            options[2] = "3";
            m_classifier.setOptions(options);
            m_classifier.buildClassifier(instancesTrain);

            TimeSeriesChart comp = new TimeSeriesChart("Comparison");
            FileEcho echo = null;
            List<BarInfo> oneDay = bars(code, resolution, date);
            List<Double> close = GeneralUtilizer.extract(oneDay, "closePrice");
            for (int i = 0; i < oneDay.size(); i++) {
                Map<String, BasicIndicator> attributes = step(oneDay.get(i), indicators);
                if (echo == null)
                    echo = attributes(attributes, test);
                List<Object> line = new ArrayList<>();
                for (BasicIndicator ind : attributes.values())
                    line.add(ind.data.getLast());
                line.add("?");
                echo.writeln(line);

                input = new File(test);
                arf.setFile(input);
                Instances instancesTest = arf.getDataSet();
                instancesTest.setClassIndex(instancesTest.numAttributes() - 1);
                double predicted = m_classifier.classifyInstance(instancesTest.lastInstance());
                System.out.println("分类值： " + instancesTest.classAttribute().value((int) predicted));
                //Thread.sleep(1000);

                comp.vis("HH-mm", close.subList(0, i + 1));
            }
            echo.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public static Map<String, BasicIndicator> step(BarInfo bar, List<SuperIndicator> indicators) {
        indicators.parallelStream().forEach(si -> si.update(bar));
        Map<String, BasicIndicator> attributes = new ConcurrentHashMap<>();
        indicators.parallelStream().forEach(si -> attributes.putAll(si.fields("")));
        return attributes;
    }

    public static void cross(String train) {
        ArffLoader arf = new ArffLoader();
        File input = new File(train);
        try {
            arf.setFile(input);
            Instances instancesTrain = arf.getDataSet();
            instancesTrain.setClassIndex(instancesTrain.numAttributes() - 1);

            J48 m_classifier = new J48();
            String options[] = new String[3];
            options[0] = "-R";
            options[1] = "-M";
            options[2] = "3";
            m_classifier.setOptions(options);//设置训练参数
            m_classifier.buildClassifier(instancesTrain);
            //System.out.println(m_classifier);

            Evaluation eval = new Evaluation(instancesTrain); //构造评价器
            eval.crossValidateModel(m_classifier, instancesTrain, 10, new Random(3));
            System.out.println(eval.toSummaryString("=== J48 ===\n", false));  //输出信息
            System.out.println(eval.toClassDetailsString("=== Details ===\n"));
            System.out.println(eval.toMatrixString("=== Confusion Matrix ===\n"));//Confusion Matrix

            RandomForest mp = new RandomForest();
            /*MultilayerPerceptron mp = new MultilayerPerceptron();
            mp.setGUI(false);//是否进行图形交互
            mp.setAutoBuild(true);//设置网络中的连接和隐层
            mp.setDebug(false);//控制打印信息
            mp.setDecay(false);//如果为true会降低学习速率
            mp.setHiddenLayers("a");//对预测结果几乎没用影响
            mp.setLearningRate(0.3);//Weights被更新的数量,对预测结果影响很大
            mp.setMomentum(0.8);//当更新weights时设置的动量
            mp.setNormalizeAttributes(true);//可以优化网络性能
            mp.setNormalizeNumericClass(true);//如果预测的是数值型可以提高网络的性能
            mp.setReset(false);//必须要在AutoBuild为true的条件下进行设置否则默认即可
            mp.setSeed(0);//随机种子数，对预测结果影响大
            mp.setTrainingTime(300);//迭代的次数,有一定影响，但是不大
            mp.setValidationSetSize(20);//验证百分比，影响大
            mp.setValidationThreshold(50);//几乎没用影响
            mp.setNominalToBinaryFilter(true);//可以提高性能*/
            mp.buildClassifier(instancesTrain);

            eval = new Evaluation(instancesTrain); //构造评价器
            eval.crossValidateModel(mp, instancesTrain, 10, new Random(3));
            //eval.evaluateModel(mp, instancesTest);//用测试数据集来评价m_classifier
            System.out.println(eval.toSummaryString("=== Random Forest ===\n", false));  //输出信息
            System.out.println(eval.toClassDetailsString("=== Details ===\n"));
            System.out.println(eval.toMatrixString("=== Confusion Matrix ===\n"));//Confusion Matrix*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void use(String train, String test) {
        ArffLoader arf = new ArffLoader();
        File input = new File(train);
        File testInput = new File(test);
        try {
            arf.setFile(input);
            Instances instancesTrain = arf.getDataSet();
            instancesTrain.setClassIndex(instancesTrain.numAttributes() - 1);

            arf.setFile(testInput);
            Instances instancesTest = arf.getDataSet();
            instancesTest.setClassIndex(instancesTest.numAttributes() - 1);

            J48 m_classifier = new J48();
            String options[] = new String[3];
            options[0] = "-R";
            options[1] = "-M";
            options[2] = "3";
            m_classifier.setOptions(options);//设置训练参数
            m_classifier.buildClassifier(instancesTrain);
            //System.out.println(m_classifier);

            Evaluation eval = new Evaluation(instancesTrain); //构造评价器
            //eval.crossValidateModel(m_classifier, instancesTrain, 10, new Random(3));
            eval.evaluateModel(m_classifier, instancesTest);//用测试数据集来评价m_classifier
            System.out.println(eval.toSummaryString("=== J48 ===\n", false));  //输出信息
            System.out.println(eval.toClassDetailsString("=== Details ===\n"));
            System.out.println(eval.toMatrixString("=== Confusion Matrix ===\n"));//Confusion Matrix


            RandomForest mp = new RandomForest();
            //MultilayerPerceptron mp = new MultilayerPerceptron();
            /*mp.setGUI(false);//是否进行图形交互
            mp.setAutoBuild(true);//设置网络中的连接和隐层
            mp.setDebug(false);//控制打印信息
            mp.setDecay(false);//如果为true会降低学习速率
            mp.setHiddenLayers("a");//对预测结果几乎没用影响
            mp.setLearningRate(0.3);//Weights被更新的数量,对预测结果影响很大
            mp.setMomentum(0.8);//当更新weights时设置的动量
            mp.setNormalizeAttributes(true);//可以优化网络性能
            mp.setNormalizeNumericClass(true);//如果预测的是数值型可以提高网络的性能
            mp.setReset(false);//必须要在AutoBuild为true的条件下进行设置否则默认即可
            mp.setSeed(0);//随机种子数，对预测结果影响大
            mp.setTrainingTime(300);//迭代的次数,有一定影响，但是不大
            mp.setValidationSetSize(20);//验证百分比，影响大
            mp.setValidationThreshold(50);//几乎没用影响
            mp.setNominalToBinaryFilter(true);//可以提高性能*/
            mp.buildClassifier(instancesTrain);

            eval = new Evaluation(instancesTrain); //构造评价器
            //eval.crossValidateModel(m_classifier, instancesTrain, 10, new Random(3));
            eval.evaluateModel(mp, instancesTest);//用测试数据集来评价m_classifier
            System.out.println(eval.toSummaryString("=== random forest ===\n", false));  //输出信息
            System.out.println(eval.toClassDetailsString("=== Details ===\n"));
            System.out.println(eval.toMatrixString("=== Confusion Matrix ===\n"));//Confusion Matrix*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DataCenterUtil.setNetDomain(CONST.INTRA_QUANDIS_URL);

        String train = "./signal/learning-train.arff";
        String test = "./signal/learning-test.arff";

        String code = "rb1605";
        LocalDate start = of(2015, 11, 16);
        LocalDate end = of(2016, 1, 20);

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

        List<BarInfo> data = bars(code, MIN_1, start, end);
        Map<String, BasicIndicator> attributes = calculate(data, indicators);
        int split = (int) (data.size() * 0.9);
        generate(data, attributes, 512, split, train);
        generate(data, attributes, split, data.size(), test);



        try {
            EMA fast = new EMA(12);
            EMA slow = new EMA(26);
            DEF dif = fast.minus(slow);
            EMA dea = new EMA(9);
            for(Double v : dif.data)
                dea.update(v);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }


        //predict(train, indicators, code, MIN_1, end.plusDays(1), test);

        System.out.println("slef****************************");
        use(train, train);
        System.out.println("cross****************************");
        cross(train);
        System.out.println("predict****************************");
        use(train, test);
    }
}
