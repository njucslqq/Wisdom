package tongtong.qiangqiang.data.factor.composite;

import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SUM;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-02.
 */
public class PVT extends WIN<BarInfo> {

    public final SUM pv;

    public final Intermediate pvt;

    public final int period;

    public PVT(int period) {
        super(1);
        this.period = period;
        pv = new SUM(period);
        pvt = new Intermediate();
    }

    @Override
    public double update(BarInfo input) {
        if (cache.isEmpty())
            return pvt.update(pv.update(0.0));
        else
            return pvt.update(pv.update(input.volume * (input.closePrice - cache.tail().closePrice) / cache.tail().closePrice));
    }

    @Override
    public String getName() {
        return "PVT[" + period + "]";
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return pvt;
    }
}
