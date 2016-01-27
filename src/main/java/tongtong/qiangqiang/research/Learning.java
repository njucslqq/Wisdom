package tongtong.qiangqiang.research;

import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies3;
import jwave.transforms.wavelets.daubechies.Daubechies5;
import tongtong.qiangqiang.vis.TimeSeriesChart;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.H.bars;
import static tongtong.qiangqiang.data.H.ticks;
import static tongtong.qiangqiang.func.Util.extract;
import static tongtong.qiangqiang.func.Util.smooth;
import static tongtong.qiangqiang.research.Learning.Direction.DOWN;
import static tongtong.qiangqiang.research.Learning.Direction.UP;
import static tongtong.qiangqiang.research.Learning.Stage.*;

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

    public static void gen(LocalDate date, LocalDate end, String file) {
        String code = "IF1601";
        TimeSeriesChart comp = new TimeSeriesChart("Comparison");
        TimeSeriesChart wave = new TimeSeriesChart("wavelet");

        FileEcho echo = new FileEcho(file);
        echo.writeln("@relation direction\n");

        echo.writeln("@attribute price real");
        echo.writeln("@attribute smoothprice5 real");
        echo.writeln("@attribute smoothprice5-derivative real");
        echo.writeln("@attribute smoothprice10 real");
        echo.writeln("@attribute smoothprice10-derivative real");
        echo.writeln("@attribute smoothprice25 real");
        echo.writeln("@attribute smoothprice25-derivative real");
        echo.writeln("@attribute direction {UP, DOWN}\n");

        echo.writeln("@data");

        int size = 4096;
        int top = 6;

        int len1 = 5;
        int len2 = 13;
        int len3 = 27;

        while (date.isBefore(end)) {
            if (date.getDayOfWeek() == SATURDAY || date.getDayOfWeek() == SUNDAY) {
                date = date.plusDays(1);
                continue;
            }
            List<Double> close = extract(ticks(code, date), "lastPrice");
            if (close.size() < (size >> 1)) {
                date = date.plusDays(1);
                continue;
            }
            close.remove(0);
            date = date.plusDays(1);

            List<Double> input = null;
            if (close.size() >= size)
                input = close.subList(0, size);
            else {
                int lack = size - close.size();
                List<Double> gap = new ArrayList<>();
                gap.addAll(close.subList(close.size() - lack, close.size()));
                Collections.reverse(gap);

                input = new ArrayList<>();
                input.addAll(close);
                input.addAll(gap);
            }

            List<Double> smooth1 = smooth(input, len1);
            List<Double> smooth2 = smooth(input, len2);
            List<Double> smooth3 = smooth(input, len3);

            Transform t = new Transform(new FastWaveletTransform(new Daubechies5()));
            List<Double> output = Filter.lowPassFilter(t, input, top);
            List<Direction> dir = judge(output);

            for (int i = 1; i < input.size(); i++) {
                echo.writeln(input.get(i),
                        smooth1.get(i), smooth1.get(i) - smooth1.get(i - 1),
                        smooth2.get(i), smooth2.get(i) - smooth2.get(i - 1),
                        smooth3.get(i), smooth3.get(i) - smooth3.get(i - 1),
                        dir.get(i)
                );
            }

            /*comp.vis("HH-mm", smooth3, input);//, output);
            wave.vis("HH-mm", output);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
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

            Evaluation eval = new Evaluation(instancesTrain); //构造评价器
            eval.evaluateModel(m_classifier, instancesTest);//用测试数据集来评价m_classifier
            System.out.println(eval.toSummaryString("=== Summary ===\n", false));  //输出信息
            System.out.println(eval.toMatrixString("=== Confusion Matrix ===\n"));//Confusion Matrix

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String train = "./signal/learning-train.arff";
        String test = "./signal/learning-test.arff";
        gen(of(2015, 12, 15), of(2015, 12, 20), train);
        gen(of(2015, 12, 21), of(2015, 12, 24), test);

        use(train, test);
    }
}
