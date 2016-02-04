package tongtong.qiangqiang.data.indicator.advance;


import tongtong.qiangqiang.data.indicator.basic.BasicIndicator;
import tongtong.qiangqiang.data.indicator.basic.MovingAverage;
import tongtong.qiangqiang.data.indicator.SuperIndicator;
import tongtong.qiangqiang.data.indicator.basic.DEF;
import tongtong.qiangqiang.data.indicator.basic.EMA;

import static java.lang.Math.max;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class MACD extends AdvanceIndicator {

    public final MovingAverage fast;

    public final MovingAverage slow;

    public final MovingAverage dea;

    public final DEF dif;

    public final DEF macd;

    public final int m;

    public final int n;

    public final int k;

    public MACD(MovingAverage fast, MovingAverage slow, MovingAverage dea) {
        m = fast.n;
        n = slow.n;
        k = dea.n;
        this.fast = fast;
        this.slow = slow;
        this.dea = dea;
        this.dif = new DEF();
        this.macd = new DEF();
    }

    public MACD(int m, int n, int k) {
        this.m = m;
        this.n = n;
        this.k = k;
        fast = new EMA(m);
        slow = new EMA(n);
        dea = new EMA(k);
        dif = new DEF();
        macd = new DEF();
    }

    @Override
    public int skip() {
        return max(k, max(m, n));
    }

    @Override
    public int size() {
        return macd.size();
    }

    @Override
    public String name() {
        return "MACD[" + m + "][" + n + "][" + k + "]";
    }

    @Override
    public double update(Object o) {
        double f = fast.update(o);
        double s = slow.update(o);
        double d = dif.update(f - s);
        double e = dea.update(d);
        return macd.update(2 * (d - e));
    }

    @Override
    public BasicIndicator primary() {
        return macd;
    }
}
