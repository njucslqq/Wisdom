package tongtong.qiangqiang.data.factor.composite;


import tongtong.qiangqiang.data.factor.MovingAverage;
import tongtong.qiangqiang.data.factor.WindowIndicator;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class MACD extends WindowIndicator<Double> {

    public final MovingAverage fast;

    public final MovingAverage slow;

    public final MovingAverage dea;

    public final Intermediate dif;

    public final Intermediate macd;

    public final int m;

    public final int n;

    public final int k;

    public MACD(MovingAverage fast, MovingAverage slow, MovingAverage dea) {
        super(0);
        m = fast.getPeriod();
        n = slow.getPeriod();
        k = dea.getPeriod();
        this.fast = fast;
        this.slow = slow;
        this.dea = dea;
        this.dif = new Intermediate();
        this.macd = new Intermediate();
    }

    public MACD(int m, int n, int k, int cacheSize) {
        super(0);
        ;
        this.m = m;
        this.n = n;
        this.k = k;
        fast = new EMA(cacheSize, m);
        slow = new EMA(cacheSize, n);
        dea = new EMA(cacheSize, k);
        dif = new Intermediate(cacheSize);
        macd = new Intermediate(cacheSize);
    }

    public MACD(int m, int n, int k) {
        this(m, n, k, MAX_VALUE);
    }

    @Override
    public double update(Double input) {
        double f = fast.update(input);
        double s = slow.update(input);
        double d = dif.update(f - s);
        double e = dea.update(d);
        return macd.update(2 * (d - e));
    }

    @Override
    public String name() {
        return "MACD[" + m + "][" + n + "][" + k + "]";
    }

    @Override
    public SingleIndicator<?> primary() {
        return macd;
    }
}
