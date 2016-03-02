package tongtong.qiangqiang.data.factor.composite;

import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class RSI extends WIN<Double> {

    public final EMA upper;

    public final EMA down;

    public final Intermediate rsi;

    public final int period;

    public RSI(int period) {
        this(period, MAX_VALUE);
    }

    public RSI(int period, int cacheSize) {
        super(1);
        this.period = period;
        upper = new EMA(cacheSize, period);
        down = new EMA(cacheSize, period);
        rsi = new Intermediate(cacheSize);
    }

    @Override
    public String getName() {
        return "RSI[" + period + "]";
    }

    @Override
    public double update(Double input) {
        double u, d;
        if (cache.isEmpty())
            u = d = 0.;
        else {
            u = max(input - cache.tail(), 0);
            d = max(cache.tail() - input, 0);
        }
        double eu = upper.update(u);
        double ed = down.update(d);
        return rsi.update(100 * eu / (eu + ed));
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return rsi;
    }
}
