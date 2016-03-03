package tongtong.qiangqiang.data.factor.composite;

import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SUM;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-03.
 */
public class VR extends WIN<BarInfo> {

    public final SUM pv, nv;

    public final Intermediate vr;

    public final int period;

    public VR(int period, int valueCapacity) {
        super(1);
        this.period = period;
        pv = new SUM(valueCapacity, period);
        nv = new SUM(valueCapacity, period);
        vr = new Intermediate(valueCapacity);
    }

    public VR(int period) {
        this(period, MAX_VALUE);
    }

    @Override
    public double update(BarInfo input) {
        double PV = pv.update(input.volume * (input.closePrice > cache.tail().closePrice ? 1.0 : 0.0) +
                0.5 * input.volume * (input.closePrice == cache.tail().closePrice ? 1.0 : 0.0));
        double NV = nv.update(input.volume * (input.closePrice < cache.tail().closePrice ? 1.0 : 0.0) +
                0.5 * input.volume * (input.closePrice == cache.tail().closePrice ? 1.0 : 0.0));
        cache.push(input);
        return vr.update(PV / NV);
    }

    @Override
    public String getName() {
        return "VR[" + period + "]";
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return vr;
    }
}
