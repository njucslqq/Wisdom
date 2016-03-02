package tongtong.qiangqiang.data.factor.composite;

import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class TRIX extends WIN<Double> {

    public final EMA ema1;

    public final EMA ema2;

    public final EMA ema3;

    public final SMA trix;

    public final int m;

    public final int n;

    public TRIX(int m, int n) {
        this(m, n, MAX_VALUE);
    }

    public TRIX(int m, int n, int cacheSize) {
        super(0);
        this.m = m;
        this.n = n;
        ema1 = new EMA(cacheSize, m);
        ema2 = new EMA(cacheSize, m);
        ema3 = new EMA(cacheSize, m);
        trix = new SMA(cacheSize, n);
    }


    @Override
    public String getName() {
        return "TRIX[" + m + "][" + n + "]";
    }

    @Override
    public double update(Double input) {
        double a = ema1.update(input);
        double b = ema2.update(a);
        double c = ema3.update(b);
        return trix.update(c);
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return trix;
    }
}
