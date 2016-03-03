package tongtong.qiangqiang.data.factor.composite;

import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.SUM;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-03.
 */
public class PSY extends WIN<Double> {

    public final SUM psy;

    public final int period;

    public PSY(int period, int valueCapacity) {
        super(1);
        this.period = period;
        psy = new SUM(valueCapacity, period);
    }

    public PSY(int period) {
        this(period, MAX_VALUE);
    }

    @Override
    public double update(Double input) {
        double PSY = psy.update((input > cache.tail() ? 1.0 : 0.0) / period);
        cache.push(input);
        return PSY;
    }

    @Override
    public String getName() {
        return "PSY[" + period + "]";
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return psy;
    }
}
