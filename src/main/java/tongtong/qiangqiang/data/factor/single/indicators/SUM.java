package tongtong.qiangqiang.data.factor.single.indicators;

import tongtong.qiangqiang.data.factor.single.SingleIndicator;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-02.
 */
public class SUM extends SingleIndicator<Double> {

    public final int period;

    public SUM(int period) {
        this(MAX_VALUE, period);
    }

    public SUM(int valueCapacity, int period) {
        super(valueCapacity, period);
        this.period = period;
    }

    @Override
    public String getName() {
        return "SUM[" + period + "]";
    }

    @Override
    public double update(Double input) {
        if (cache.size() < period) {
            cache.push(input);
            value.push(cache.all().stream().mapToDouble(d -> d).sum());
        } else {
            value.push(value.tail() - cache.head() + input);
            cache.push(input);
        }
        return value.tail();
    }
}
