package tongtong.qiangqiang.data.indicator.advance;

import tongtong.qiangqiang.data.indicator.basic.*;
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
public class RSI extends AdvanceWindowIndicator {

    public final MovingAverage upper;

    public final MovingAverage down;

    public final DEF rsi = new DEF();

    public RSI(int n) {
        super(n);
        upper = new EMA(n);
        down = new EMA(n);
    }

    @Override
    public int skip() {
        return n;
    }

    @Override
    public int size() {
        return rsi.size();
    }

    @Override
    public String name() {
        return "RSI[" + n + "]";
    }

    @Override
    public BasicIndicator primary() {
        return rsi;
    }

    @Override
    public double action(Object o) {
        double u, d;
        if (window.size() == 1)
            u = d = 0.;
        else {
            u = max(window.getLast() - window.get(window.size() - 2), 0);
            d = max(window.get(window.size() - 2) - window.getLast(), 0);
        }
        double eu = upper.update(u);
        double ed = down.update(d);
        return rsi.update(eu / (eu + ed));
    }
}
