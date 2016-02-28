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
    public String name() {
        return "ROC[" + period + "]";
    }

    @Override
    public double update(Double input) {
        if (previous.size() < period)
            data.add(0.);
        else
            data.add(100 * (input - previous.first(0)) / previous.first(0));
        previous.add(input);
        return data.last(0);
    }
}
