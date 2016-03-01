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
 * Created on 2016-03-01.
 */
public class PPO extends WIN<Double> {

    public final MAVG mavg;

    public final Intermediate ppo;

    public final int period;

    public PPO(int period) {
        this(period, MAX_VALUE);
    }

    public PPO(int period, int cacheSize) {
        super(0);
        this.period = period;
        mavg = new EMA(cacheSize, period);
        ppo = new Intermediate(cacheSize);
    }

    @Override
    public String name() {
        return "PPO[" + period + "]";
    }

    @Override
    public double update(Double input) {
        double _mavg = mavg.update(input);
        return ppo.update((input - _mavg) / _mavg);
    }

    @Override
    public SingleIndicator<?> primary() {
        return ppo;
    }
}
