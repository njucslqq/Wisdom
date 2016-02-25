package tongtong.qiangqiang.data.factor.single.indicators;

import tongtong.qiangqiang.data.factor.MovingAverage;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class SMA extends SingleIndicator<Double> implements MovingAverage {

    public final int period;

    public SMA(int period) {
        this(MAX_VALUE, period);
    }

    public SMA(int cacheSize, int period) {
        super(cacheSize, period);
        this.period = period;
    }

    @Override
    public double update(Double input) {
        if (previous.size() < period) {
            previous.add(input);
            data.add(previous.window.stream().mapToDouble(d -> d).average().getAsDouble());
        } else {
            data.add(data.getLast() + (input - previous.first(0)) / 3.0);
            previous.add(input);
        }
        return data.getLast();
    }

    @Override
    public String name() {
        return "SMA[" + period + "]";
    }

    @Override
    public int period() {
        return period;
    }
}
