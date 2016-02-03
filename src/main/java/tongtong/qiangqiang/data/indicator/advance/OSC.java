package tongtong.qiangqiang.data.indicator.advance;

import tongtong.qiangqiang.data.indicator.basic.BasicIndicator;
import tongtong.qiangqiang.data.indicator.basic.MovingAverage;
import tongtong.qiangqiang.data.indicator.basic.DEF;
import tongtong.qiangqiang.data.indicator.basic.SMA;
import tongtong.qiangqiang.func.GeneralUtilizer;

import static tongtong.qiangqiang.func.GeneralUtilizer.*;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class OSC extends AdvanceIndicator {

    public final MovingAverage mavg;

    public final DEF osc = new DEF();

    public final int n;

    public OSC(MovingAverage mavg) {
        this.mavg = mavg;
        this.n = mavg.n;
    }

    public OSC(int n) {
        this.n = n;
        mavg = new SMA(n);
    }

    @Override
    public int skip() {
        return n;
    }

    @Override
    public String name() {
        return "OSC[" + n + "]";
    }

    @Override
    public double update(Object o) {
        double v = valueDefault(o);
        double a = mavg.update(v);
        return osc.update(v - a);
    }

    @Override
    public BasicIndicator primary() {
        return osc;
    }
}