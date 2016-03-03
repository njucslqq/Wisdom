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
public class OBV extends WIN<BarInfo> {

    public final SUM pVolume, nVolume;

    public final Intermediate obv;

    public final int period;

    public OBV(int period) {
        super(1);
        this.period = period;
        pVolume = new SUM(period);
        nVolume = new SUM(period);
        obv = new Intermediate();
    }

    @Override
    public double update(BarInfo input) {
        double pSum = pVolume.update(input.closePrice > cache.tail().closePrice ? input.volume : 0.0);
        double nSum = nVolume.update(input.closePrice < cache.tail().closePrice ? input.volume : 0.0);
        cache.push(input);
        return obv.update(pSum - nSum);
    }

    @Override
    public String getName() {
        return "OBV[" + period + "]";
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return obv;
    }
}
