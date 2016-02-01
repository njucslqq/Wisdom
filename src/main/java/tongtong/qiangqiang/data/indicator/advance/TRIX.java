package tongtong.qiangqiang.data.indicator.advance;

import tongtong.qiangqiang.data.indicator.basic.BasicIndicator;
import tongtong.qiangqiang.data.indicator.basic.MovingAverage;
import tongtong.qiangqiang.data.indicator.SuperIndicator;
import tongtong.qiangqiang.data.indicator.basic.EMA;
import tongtong.qiangqiang.data.indicator.basic.SMA;
import tongtong.qiangqiang.func.GeneralUtilizer;

import static java.lang.Math.max;
import static tongtong.qiangqiang.func.GeneralUtilizer.*;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class TRIX extends AdvanceIndicator {

    public final MovingAverage ema;

    public final MovingAverage ema2;

    public final MovingAverage ema3;

    public final MovingAverage trix;

    public final int m;

    public final int n;

    public TRIX(int m, int n) {
        this.m = m;
        this.n = n;
        ema = new EMA(m);
        ema2 = new EMA(m);
        ema3 = new EMA(m);
        trix = new SMA(n);
    }

    @Override
    public int skip() {
        return max(m, n);
    }

    @Override
    public String name() {
        return "TRIX[" + m + "][" + n + "]";
    }

    @Override
    public double update(Object o) {
        double v = valueDefault(o);
        double a = ema.update(v);
        double b = ema2.update(a);
        double c = ema3.update(b);
        return trix.update(c);
    }

    @Override
    public BasicIndicator primary() {
        return trix;
    }
}
