package tongtong.qiangqiang.hunt.rnn.util;

import cn.quanttech.quantera.common.datacenter.HistoricalData;
import cn.quanttech.quantera.common.type.data.BarInfo;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.api.Updater;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import tongtong.qiangqiang.hunt.rnn.BarIterator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static cn.quanttech.quantera.common.datacenter.HistoricalData.bars;
import static cn.quanttech.quantera.common.type.data.TimeFrame.MIN_1;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.hunt.rnn.util.MathUtil.logDenorm;
import static tongtong.qiangqiang.hunt.rnn.util.MathUtil.logNorm;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-05-06.
 */
public class ModelUtil {

    public static String coefficients, updater, conf;

    public static String target;

    public static LocalDate start, end;

    public static List<BarInfo> realBars;

    public static BarInfo startBar;

    static {
        coefficients = "net/coefficients.bin";
        updater = "net/updater.bin";
        conf = "net/conf.json";

        target = "rb1610";
        start = of(2016, 1, 1);
        end = of(2016, 5, 5);
    }

    public static void saveModel(MultiLayerNetwork net) {
        try {
            try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Paths.get(coefficients)))) {
                Nd4j.write(net.params(), dos);
            }

            FileUtils.write(new File(conf), net.getLayerWiseConfigurations().toJson());

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(updater))) {
                oos.writeObject(net.getUpdater());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MultiLayerNetwork loadModel() {
        try {
            INDArray newParams;
            try (DataInputStream dis = new DataInputStream(new FileInputStream(coefficients))) {
                newParams = Nd4j.read(dis);
            }

            MultiLayerConfiguration confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(conf)));

            Updater updaters = null;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(updater))) {
                updaters = (Updater) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            MultiLayerNetwork savedNetwork = new MultiLayerNetwork(confFromJson);
            savedNetwork.init();
            savedNetwork.setParameters(newParams);
            savedNetwork.setUpdater(updaters);
            return savedNetwork;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<BarInfo> getDiffs(List<BarInfo> bars) {
        ArrayList<BarInfo> barDiff = new ArrayList<>();
        for (int i = 1; i < bars.size(); i++) {
            BarInfo pre = bars.get(i - 1);
            BarInfo cur = bars.get(i);
            BarInfo b = new BarInfo(null, null, null, null, null);
            b.open = logNorm(cur.open - pre.open);
            b.high = logNorm(cur.high - pre.high);
            b.low = logNorm(cur.low - pre.low);
            b.close = logNorm(cur.close - pre.close);
            barDiff.add(b);
        }
        return barDiff;
    }

    public static BarIterator getBarIterator(int miniBatchSize, int exampleLength, Random rng) {
        List<BarInfo> bars = bars(target, MIN_1, start, end);
        return new BarIterator(getDiffs(bars), miniBatchSize, exampleLength, rng);
    }

    public static void getPrediction(MultiLayerNetwork net, BarIterator iter) {
        List<BarInfo> bars = HistoricalData.bars(target, MIN_1, end, end.plusDays(1));
        BarInfo begin = getDiffs(bars.subList(0, 2)).get(0);

        net.rnnClearPreviousState();
        List<BarInfo> diffs = new ArrayList<>();
        diffs.add(begin);

        INDArray input = getOneInput(begin, iter);
        for (int i = 0; i < bars.size(); i++) {
            INDArray output = net.rnnTimeStep(input);
            BarInfo dif = new BarInfo(null, null, null, null, null);
            dif.open = output.getDouble(new int[]{0, 0});
            dif.high = output.getDouble(new int[]{0, 1});
            dif.low = output.getDouble(new int[]{0, 2});
            dif.close = output.getDouble(new int[]{0, 3});
            diffs.add(dif);
            input = getOneInput(dif, iter);
        }

        List<BarInfo> predictBars = new ArrayList<>();
        BarInfo start = bars.get(0);
        for (BarInfo d : diffs){
            predictBars.add(start);
            BarInfo newDif = new BarInfo(null, null, null, null, null);
            newDif.open = start.open + logDenorm(d.open);
            newDif.high = start.high + logDenorm(d.high);
            newDif.low = start.low + logDenorm(d.low);
            newDif.close = start.close + logDenorm(d.close);
            start = newDif;
        }

        showBarList(bars);
        showBarList(predictBars);
    }

    public static INDArray getOneInput(BarInfo begin, BarIterator iter) {
        INDArray input = Nd4j.zeros(1, iter.inputColumns());
        input.putScalar(new int[]{0, 0}, begin.open);
        input.putScalar(new int[]{0, 1}, begin.high);
        input.putScalar(new int[]{0, 2}, begin.low);
        input.putScalar(new int[]{0, 3}, begin.close);
        return input;
    }

    public static void showBarList(List<BarInfo> bars){
        LocalDate startDate = of(2016, 5, 2);
        for (BarInfo cur : bars) {
            StringBuilder line = new StringBuilder();
            line.append("['").append(startDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))).append("',");
            line.append(cur.open).append(",");
            line.append(cur.close).append(",");
            line.append(cur.low).append(",");
            line.append(cur.high).append("],");
            System.out.println(line);
            startDate = startDate.plusDays(1);
        }
    }
}
