package tongtong.qiangqiang.research;

import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.Wavelet;
import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import tongtong.qiangqiang.func.ListMap;
import tongtong.qiangqiang.mock.TradeBase;
import tongtong.qiangqiang.vis.TimeSeriesChart;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZoneId.systemDefault;
import static java.util.Date.from;
import static tongtong.qiangqiang.research.Filter.lowPassFilter;
import static tongtong.qiangqiang.research.WaveChart.Direction.DOWN;
import static tongtong.qiangqiang.research.WaveChart.Direction.MEDIUM;
import static tongtong.qiangqiang.research.WaveChart.Direction.UP;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-01-14.
 */
public class WaveChart extends TradeBase {

    public enum Action {
        BUY_OPEN, SELL_CLOSE, SELL_OPEN, BUY_CLOSE, NOTHING
    }

    public enum Direction {
        UP, DOWN, MEDIUM
    }

    public static final String FMT = "HH-mm-ss.S";

    public final List<Double> before;

    public final List<Double> after;

    private int ALL = 4096;

    private int TAIL = 137;

    private int HEAD = ALL - TAIL;

    private int TOP = 3;

    public WaveChart(List<Double> before, ListMap map) {
        this.before = before;
        this.after = map.map(before);
    }

    public WaveChart setALL(int ALL) {
        this.ALL = ALL;
        HEAD = ALL - TAIL;
        return this;
    }

    public WaveChart setTAIL(int TAIL) {
        this.TAIL = TAIL;
        HEAD = ALL - TAIL;
        return this;
    }

    public WaveChart setTOP(int TOP) {
        this.TOP = TOP;
        return this;
    }

    public void show(Wavelet wavelet, long ms) {
        showList(new TimeSeriesChart("Time Series"), before, "raw data");
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TimeSeriesChart tsTarget = new TimeSeriesChart("Time Series");
        TimeSeriesChart tsTransform = new TimeSeriesChart("Time Series");

        for (int index = 0; index < after.size(); index++) {
            List<Double> data = new ArrayList<>();
            int aStart = 0, bStart = 0, count = 1;
            if (index + 1 < HEAD) {
                aStart = 0;
                count = index + 1;
            } else {
                aStart = index + 1 - HEAD;
                count = HEAD;
            }
            bStart = HEAD - count;
            data.addAll(after.subList(0, HEAD - count));
            data.addAll(after.subList(aStart, aStart + count));
            for (int j = 0; j < TAIL; j++)
                data.add(after.get(index));

            Transform t = new Transform(new FastWaveletTransform(wavelet));
            List<Double> res = lowPassFilter(t, data, TOP);

            List<Double> smooth = after.subList(aStart, aStart + count);
            List<Double> wave = res.subList(bStart, bStart + count);
            showList(tsTarget, smooth, "smooth data");
            showList(tsTransform, wave, "wave data");

            int focus = 3;
            int length = smooth.size();
            if (length >= focus) {
                Pair<Double, Double> smoothID = evaluate(smooth.subList(length - focus, length));
                Pair<Double, Double> waveID = evaluate(wave.subList(length - focus, length));

                Direction shortDir = estimate(smoothID, 0.75, 0.75);
                Direction longDir = estimate(waveID, 0.75, 0.75);
                double curPrice = before.get(index);
                /*if(shortDir == UP && longDir == UP)
                    buyOpen(before.get(index));
                if(shortDir == DOWN && longDir == DOWN)
                    sellOpen(before.get(index));
                    */
                if(shortDir == UP) {
                    //buyClose(curPrice);
                    buyOpen(curPrice);
                }
                else if (shortDir == DOWN){
                    sellClose(curPrice);
                    //sellOpen(curPrice);
                }
            }

            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("多头盈利：" + longDiff);
        System.out.println("空头盈利：" + shortDiff);
    }

    private void showList(TimeSeriesChart tsc, List<Double> data, String title) {
        LocalDateTime start = LocalDateTime.now();
        TimeSeries ts = new TimeSeries(title);
        for (int i = 0; i < data.size(); i++)
            ts.add(new Second(from(start.plusSeconds(i).atZone(systemDefault()).toInstant())), data.get(i));
        tsc.vis(FMT, ts);
    }

    private Pair<Double, Double> evaluate(List<Double> smooth) {
        return Pair.of(LIS(smooth) * 1.0 / smooth.size(), LDS(smooth) * 1.0 / smooth.size());
    }

    private Direction estimate(Pair<Double, Double> IncDec, double up, double down){
        if(IncDec.getLeft() > up)
            return UP;
        if(IncDec.getRight() > down)
            return DOWN;
        return MEDIUM;
    }

    private int LIS(List<Double> data) {
        int[] len = new int[data.size()];
        len[0] = 1;
        int res = 1;
        for (int i = 1; i < len.length; i++) {
            int maxLen = 0;
            for (int j = 0; j < i; j++)
                if (data.get(j) < data.get(i))
                    maxLen = Math.max(maxLen, len[j]);
            len[i] = maxLen + 1;
            res = Math.max(res, len[i]);
        }
        return res;
    }

    private int LDS(List<Double> data) {
        int[] len = new int[data.size()];
        len[0] = 1;
        int res = 1;
        for (int i = 1; i < len.length; i++) {
            int maxLen = 0;
            for (int j = 0; j < i; j++)
                if (data.get(j) > data.get(i))
                    maxLen = Math.max(maxLen, len[j]);
            len[i] = maxLen + 1;
            res = Math.max(res, len[i]);
        }
        return res;
    }
}
