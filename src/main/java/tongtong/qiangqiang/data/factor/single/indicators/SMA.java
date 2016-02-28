package tongtong.qiangqiang.data.factor.single.indicators;

import tongtong.qiangqiang.data.factor.MAVG;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class SMA extends SingleIndicator<Double> implements MAVG {

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
            push(previous.all().stream().mapToDouble(d -> d).average().getAsDouble());
        }
        else {
            push(data.last(0) + (input - previous.first(0)) / period);
            previous.add(input);
        }
        return data.last(0);
    }

    @Override
    public String name() {
        return "SMA[" + period + "]";
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
