package tongtong.qiangqiang.hunt;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.TimeFrame;
import cn.quanttech.quantera.datacenter.DataCenter;
import cn.quanttech.quantera.datacenter.DataCenterUtil;
import com.google.common.collect.ImmutableList;
import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies3;
import jwave.transforms.wavelets.daubechies.Daubechies5;
import tongtong.qiangqiang.data.FileEcho;
import tongtong.qiangqiang.data.Historical;
import tongtong.qiangqiang.data.indicator.SuperIndicator;
import tongtong.qiangqiang.data.indicator.advance.*;
import tongtong.qiangqiang.data.indicator.basic.*;
import tongtong.qiangqiang.func.GeneralUtilizer;
import tongtong.qiangqiang.vis.TimeSeriesChart;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
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

    public static void generate(List<BarInfo> data, List<SuperIndicator> indicators, String file) {
        List<Double> close = GeneralUtilizer.extract(data, "closePrice");

        for (BarInfo b : data)
            for (SuperIndicator ind : indicators)
                ind.update(b);

        Map<String, BasicIndicator> attributes = new HashMap<>();
        for (SuperIndicator ind : indicators)
            attributes.putAll(ind.fields("train"));

        FileEcho echo = new FileEcho(file);
        echo.writeln("@relation direction");
        for (String key : attributes.keySet())
            echo.writeln("@attribute " + key + " real");
        echo.writeln("@attribute direction {UP, DOWN}");
        echo.writeln("@data");

        TimeSeriesChart comp = new TimeSeriesChart("Comparison");
        TimeSeriesChart wave = new TimeSeriesChart("wavelet");

        int size = 512;
        int top = 3;
        for (int i = 512; i < data.size(); i += size) {
            int to = Math.min(i + size, data.size());
            if (to - i != size)
                continue;
            Transform t = new Transform(new FastWaveletTransform(new Daubechies5()));
            List<Double> input = Filter.lowPassFilter(t, close.subList(i, i + size), top);
            List<Direction> dir = judge(input);
            for (int j = 50; j < size - 50; j++) {
                List<Object> line = new ArrayList<>();
                for (BasicIndicator ind : attributes.values())
                    line.add(ind.data.get(j + i));
                line.add(dir.get(j));
                echo.writeln(line);
            }

            /*comp.vis("HH-mm", close.subList(i, i + size));//, output);
            wave.vis("HH-mm", input);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        echo.close();
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
            System.out.println(m_classifier);

            Evaluation eval = new Evaluation(instancesTrain); //构造评价器
            eval.evaluateModel(m_classifier, instancesTest);//用测试数据集来评价m_classifier
            System.out.println(eval.toSummaryString("=== Summary ===\n", false));  //输出信息
            System.out.println(eval.toMatrixString("=== Confusion Matrix ===\n"));//Confusion Matrix

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DataCenterUtil.setNetDomain(CONST.INTRA_QUANDIS_URL);

        String train = "./signal/learning-train.arff";
        String test = "./signal/learning-test.arff";

        String code = "m1605";
        LocalDate start = of(2016, 1, 10);
        LocalDate split = of(2016, 1, 26);
        LocalDate end = of(2016, 2, 1);

        List<SuperIndicator> indicators = ImmutableList.of(
                new WMA(5), new WMA(7), new WMA(9), new WMA(11), new WMA(13), new WMA(15), new WMA(17), new WMA(21), new WMA(27), new WMA(31), new WMA(41), new WMA(53), new WMA(61), new WMA(67), new WMA(73), new WMA(79), new WMA(87), new WMA(93),
                new MACD(11, 23, 7), new MACD(15, 27, 9), new MACD(19, 33, 13), new MACD(23, 39, 19), new MACD(33, 43, 31), new MACD(41, 53, 39), new MACD(53, 67, 43),
                new EMA(7), new EMA(13), new EMA(17), new EMA(21), new EMA(25), new EMA(29), new EMA(33), new EMA(39), new EMA(43), new EMA(51), new EMA(61), new EMA(67), new EMA(79), new EMA(87),
                new WilliamsR(11), new WilliamsR(13), new WilliamsR(17), new WilliamsR(23), new WilliamsR(35), new WilliamsR(39), new WilliamsR(47), new WilliamsR(59), new WilliamsR(73),
                new BOLL(7, 0.15), new BOLL(13, 0.15), new BOLL(17, 0.15), new BOLL(23, 0.15), new BOLL(27, 0.15), new BOLL(34, 0.15),  new BOLL(43, 0.15),  new BOLL(51, 0.15),  new BOLL(62, 0.15),  new BOLL(79, 0.15),
                new MTM(),
                new DMA(7, 13, 5), new DMA(13, 17, 9), new DMA(17, 25, 13), new DMA(25, 34, 21), new DMA(34, 47, 31),  new DMA(47, 59, 31),
                new OSC(7), new OSC(11), new OSC(15), new OSC(21), new OSC(27), new OSC(34), new OSC(41), new OSC(57), new OSC(69), new OSC(81),
                new RSI(11), new RSI(15), new RSI(21), new RSI(31), new RSI(41), new RSI(51), new RSI(67), new RSI(81),
                new TRIX(13, 7), new TRIX(17, 11), new TRIX(27, 17), new TRIX(39, 21), new TRIX(47, 27), new TRIX(61, 39)
        );

        generate(bars(code, MIN_1, start, split), indicators, train);
        generate(bars(code, MIN_1, split.plusDays(1), end), indicators, test);
        use(train, train);
        use(train, test);
    }
}
