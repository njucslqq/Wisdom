package tongtong.qiangqiang.data.factor.single.indicators;


import tongtong.qiangqiang.data.factor.single.SingleIndicator;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class MTM extends SingleIndicator<Double> {

    public final int period;

    public MTM(int period) {
        this(MAX_VALUE, period);
    }

    public MTM(int cacheSize, int period) {
        super(cacheSize, period);
        this.period = period;
    }

    @Override
    public double update(Double input) {
        if (cache.size() < period)
            value.push(0.);
        else
            value.push(input - cache.first(0));
        cache.push(input);
        return value.last(0);
    }

    @Override
    public String getName() {
        return "MTM[" + period + "]";
    }
}
