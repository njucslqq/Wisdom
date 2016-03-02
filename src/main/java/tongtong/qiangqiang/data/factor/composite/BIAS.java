package tongtong.qiangqiang.data.factor.composite;

import tongtong.qiangqiang.data.factor.MAVG;
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
public class BIAS extends WIN<Double> {

    public final MAVG mavg;

    public final Intermediate bias;

    public final int period;

    public BIAS(int period) {
        this(period, MAX_VALUE);
    }

    public BIAS(int period, int cacheSize) {
        super(0);
        this.period = period;
        mavg = new SMA(cacheSize, period);
        bias = new Intermediate(cacheSize);
    }

    public BIAS(MAVG mavg) {
        super(0);
        this.period = mavg.getPeriod();
        this.mavg = mavg;
        this.bias = new Intermediate(period);
    }

    @Override
    public String getName() {
        return "BIAS[" + period + "]";
    }

    @Override
    public double update(Double input) {
        double _mavg = mavg.update(input);
        return bias.update(100 * (input - _mavg) / _mavg);
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return bias;
    }
}
