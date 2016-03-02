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

    public SMA(int valueCapacity, int period) {
        super(valueCapacity, period);
        this.period = period;
    }

    @Override
    public double update(Double input) {
        if (cache.size() < period) {
            cache.push(input);
            value.push(cache.all().stream().mapToDouble(d -> d).average().getAsDouble());
        } else {
            value.push(value.tail() + (input - cache.head()) / period);
            cache.push(input);
        }
        return value.tail();
    }

    @Override
    public String getName() {
        return "SMA[" + period + "]";
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
