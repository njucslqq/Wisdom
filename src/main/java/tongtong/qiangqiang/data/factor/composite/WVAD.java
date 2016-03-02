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
public class WVAD extends WIN<BarInfo> {

    public final SUM rv;

    public final Intermediate wvad;

    public final int period;

    public WVAD(int period) {
        super(1);
        this.period = period;
        rv = new SUM(period);
        wvad = new Intermediate();
    }

    @Override
    public double update(BarInfo input) {
        return wvad.update(rv.update(input.volume * (input.closePrice - input.openPrice) / (input.highPrice - input.lowPrice)));
    }

    @Override
    public String getName() {
        return "WVAD[" + period + "]";
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return wvad;
    }
}
