package tongtong.qiangqiang.data.factor.composite;

import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-25.
 */
public class BBI extends WIN<Double> {

    public final SMA a, b, c, d;

    public final Intermediate bbi;

    public final double n1, n2, n3, n4;

    public BBI(int n1, int n2, int n3, int n4, int cacheSize) {
        super(0);
        this.n1 = n1;
        this.n2 = n2;
        this.n3 = n3;
        this.n4 = n4;
        a = new SMA(cacheSize, n1);
        b = new SMA(cacheSize, n2);
        c = new SMA(cacheSize, n3);
        d = new SMA(cacheSize, n4);
        bbi = new Intermediate(cacheSize);
    }

    public BBI(int n1, int n2, int n3, int n4) {
        this(n1, n2, n3, n4, MAX_VALUE);
    }

    @Override
    public String name() {
        return "BBI[" + n1 + "][" + n2 + "][" + n3 + "][" + n4 + "]";
    }

    @Override
    public double update(Double input) {
        double _a = a.update(input);
        double _b = b.update(input);
        double _c = c.update(input);
        double _d = d.update(input);
        return bbi.update((_a + _b + _c + _d) / 4.);
    }

    @Override
    public SingleIndicator<?> primary() {
        return bbi;
    }
}
