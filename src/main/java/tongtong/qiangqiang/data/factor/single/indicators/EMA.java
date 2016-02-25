package tongtong.qiangqiang.data.factor.single.indicators;

import tongtong.qiangqiang.data.factor.MovingAverage;
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
public class EMA extends SingleIndicator<Double> implements MovingAverage {

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
        if (data.isEmpty())
            data.add(input);
        else {
            int n = min(data.size(), period);
            double k = 2. / (n + 1.);
            data.add(data.getLast() * (1 - k) + input * k);
        }
        return data.getLast();
    }

    @Override
    public String name() {
        return "EMA[" + period + "]";
    }

    @Override
    public int period() {
        return period;
    }
}
