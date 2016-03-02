package tongtong.qiangqiang.data.factor.single.indicators;


import tongtong.qiangqiang.data.factor.single.SingleIndicator;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-25.
 */
public class ROC extends SingleIndicator<Double> {

    public final int period;

    public ROC(int period) {
        this(MAX_VALUE, period);
    }

    public ROC(int cacheSize, int period) {
        super(cacheSize, period);
        this.period = period;
    }

    @Override
    public String getName() {
        return "ROC[" + period + "]";
    }

    @Override
    public double update(Double input) {
        if (cache.size() < period)
            value.push(0.);
        else
            value.push(100 * (input - cache.first(0)) / cache.first(0));
        cache.push(input);
        return value.last(0);
    }
}
