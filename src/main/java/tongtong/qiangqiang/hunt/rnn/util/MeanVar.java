package tongtong.qiangqiang.hunt.rnn.util;

import cn.quanttech.quantera.common.datacenter.HistoricalData;
import cn.quanttech.quantera.common.type.data.TimeFrame;
import cn.quanttech.quantera.common.type.quotation.BarInfo;
import org.apache.commons.math3.util.Pair;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-05-05.
 */
public class MeanVar {

    public static Pair<Double, Double> meanVariance(List<Number> data) {
        double mean = data.stream().mapToDouble(Number::doubleValue).average().getAsDouble();
        double sum = 0.0;
        for (Number d : data)
            sum += (d.doubleValue() - mean) * (d.doubleValue() - mean);
        double variance = Math.sqrt(sum / data.size());
        return Pair.create(mean, variance);
    }

    public static Pair<Double, Double> largeSmall(List<Number> data) {
        double max = data.stream().mapToDouble(Number::doubleValue).max().getAsDouble();
        double min = data.stream().mapToDouble(Number::doubleValue).min().getAsDouble();
        return Pair.create(max, min);
    }

    public static void main(String[] args) {
        List<BarInfo> bars = HistoricalData.bars("rb1610", TimeFrame.MIN_1, LocalDate.of(2016, 1, 1), LocalDate.of(2016, 6, 8));
        ArrayList<Number> o, h, l, c, v;
        o = new ArrayList<>();
        h = new ArrayList<>();
        l = new ArrayList<>();
        c = new ArrayList<>();
        v = new ArrayList<>();
        for (int i = 1; i < bars.size(); i++) {
            BarInfo pre = bars.get(i - 1);
            BarInfo cur = bars.get(i);

            o.add(cur.open - pre.open);
            h.add(cur.high - pre.high);
            l.add(cur.low - pre.low);
            c.add(cur.close - pre.close);
            v.add((cur.volume - pre.volume));
        }

        Pair<Double, Double> mv_o = meanVariance(o);
        Pair<Double, Double> mv_h = meanVariance(h);
        Pair<Double, Double> mv_l = meanVariance(l);
        Pair<Double, Double> mv_c = meanVariance(c);
        Pair<Double, Double> mv_v = meanVariance(v);

        Pair<Double, Double> ls_o = largeSmall(o);
        Pair<Double, Double> ls_h = largeSmall(h);
        Pair<Double, Double> ls_l = largeSmall(l);
        Pair<Double, Double> ls_c = largeSmall(c);
        Pair<Double, Double> ls_v = largeSmall(v);

        o.sort((a, b) -> ((Double) b).compareTo(a.doubleValue()));

        System.out.println("open  : " + mv_o.toString() + ", " + ls_o.toString());
        System.out.println("high  : " + mv_h.toString() + ", " + ls_h.toString());
        System.out.println("low   : " + mv_l.toString() + ", " + ls_l.toString());
        System.out.println("close : " + mv_c.toString() + ", " + ls_c.toString());
        System.out.println("volume: " + mv_v.toString() + ", " + ls_v.toString());
    }
}
