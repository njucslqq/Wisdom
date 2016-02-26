package tongtong.qiangqiang.data.factor.composite;

import tongtong.qiangqiang.data.factor.MovingAverage;
import tongtong.qiangqiang.data.factor.WindowIndicator;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class OSC extends WindowIndicator<Double> {

    public final MovingAverage mavg;

    public final Intermediate osc;

    public final int period;

    public OSC(int period) {
        this(period, MAX_VALUE);
    }

    public OSC(int period, int cacheSize) {
        super(0);
        this.period = period;
        mavg = new SMA(cacheSize, period);
        osc = new Intermediate(cacheSize);
    }

    public OSC(MovingAverage mavg) {
        super(0);
        this.period = mavg.getPeriod();
        this.mavg = mavg;
        this.osc = new Intermediate(period);
    }

    @Override
    public String name() {
        return "OSC[" + period + "]";
    }

    @Override
    public double update(Double input) {
        double _mavg = mavg.update(input);
        return osc.update(input - _mavg);
    }

    @Override
    public SingleIndicator<?> primary() {
        return osc;
    }
}