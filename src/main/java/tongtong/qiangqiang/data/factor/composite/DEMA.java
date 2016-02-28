package tongtong.qiangqiang.data.factor.composite;

import tongtong.qiangqiang.data.factor.MAVG;
import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class DEMA extends WIN<Double> implements MAVG {

    public final MAVG mavg1, mavg2;

    public final Intermediate dema;

    public final int period;

    public DEMA(int period, int cacheSize) {
        super(0);
        this.period = period;
        mavg1 = new EMA(cacheSize, period);
        mavg2 = new EMA(cacheSize, period);
        dema = new Intermediate(cacheSize);
    }

    public DEMA(int period) {
        this(period, MAX_VALUE);
    }

    public DEMA(MAVG mavg1, MAVG mavg2) {
        super(0);
        this.period = mavg1.getPeriod();
        this.mavg1 = mavg1;
        this.mavg2 = mavg2;
        this.dema = new Intermediate();
    }

    @Override
    public String name() {
        return "DEMA[" + period + "]";
    }

    @Override
    public double update(Double input) {
        double a = mavg1.update(input);
        double b = mavg2.update(a);
        return dema.update(2 * a - b);
    }

    @Override
    public SingleIndicator<?> primary() {
        return dema;
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
