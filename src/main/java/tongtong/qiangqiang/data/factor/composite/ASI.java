package tongtong.qiangqiang.data.factor.composite;


import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static tongtong.qiangqiang.data.factor.tool.Constant.EPSILON;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-23.
 */
public class ASI extends WIN<BarInfo> {

    public final Intermediate A, B, C, D, E, F, G, R, X, SI, ASI;

    public final double L = 3.0;

    public final int period;

    public ASI(int period) {
        this(MAX_VALUE, period);
    }

    public ASI(int cacheSize, int period) {
        super(1);
        this.period = period;
        A = new Intermediate(cacheSize);
        B = new Intermediate(cacheSize);
        C = new Intermediate(cacheSize);
        D = new Intermediate(cacheSize);
        E = new Intermediate(cacheSize);
        F = new Intermediate(cacheSize);
        G = new Intermediate(cacheSize);
        R = new Intermediate(cacheSize);
        X = new Intermediate(cacheSize);
        SI = new Intermediate(max(period, cacheSize));
        ASI = new Intermediate(cacheSize);
    }

    @Override
    public SingleIndicator<Double> primary() {
        return ASI;
    }

    @Override
    public double update(BarInfo input) {
        BarInfo pre = previous.prev();
        previous.add(input);

        double a, b, c, d, e, f, g;
        a = abs(input.highPrice - pre.closePrice);
        b = abs(input.lowPrice - pre.closePrice);
        c = abs(input.highPrice - pre.lowPrice);
        d = abs(pre.closePrice - pre.openPrice);
        e = input.closePrice - pre.closePrice;
        f = input.closePrice - input.openPrice;
        g = pre.closePrice - pre.openPrice;
        A.update(a);
        B.update(b);
        C.update(c);
        D.update(d);
        E.update(e);
        F.update(f);
        G.update(g);

        double r = 0.25 * d;
        double x = e + 0.5 * f + g;
        double l = L;
        double maxABC = max(a, max(b, c));
        if (abs(a - maxABC) < EPSILON)
            r += a + 0.5 * b;
        else if (abs(b - maxABC) < EPSILON)
            r += 0.5 * a + b;
        else
            r += c;
        R.update(r);
        X.update(x);

        double si;
        if (abs(r) < EPSILON)
            si = 0.;
        else
            si = 50 * x * max(a, b) / (r * l);
        SI.update(si);

        double asi = SI.last(SI.dataSize() < period ? SI.dataSize() : period).stream().mapToDouble(v -> v).sum();
        return ASI.update(asi);
    }

    @Override
    public String name() {
        return "ASI[" + period + "]";
    }
}
