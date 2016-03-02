package tongtong.qiangqiang.data.factor.single.indicators;

import tongtong.qiangqiang.data.factor.MAVG;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class EMA extends SingleIndicator<Double> implements MAVG {

    public final int period;

    public EMA(int period) {
        this(MAX_VALUE, period);
    }

    public EMA(int cacheSize, int period) {
        super(cacheSize, 0);
        this.period = period;
    }

    @Override
    public double update(Double input) {
        if (value.isEmpty())
            value.push(input);
        else {
            int n = min(value.size(), period);
            double k = 2. / (n + 1.);
            value.push(value.tail() * (1 - k) + input * k);
        }
        return value.last(0);
    }

    @Override
    public String getName() {
        return "EMA[" + period + "]";
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
