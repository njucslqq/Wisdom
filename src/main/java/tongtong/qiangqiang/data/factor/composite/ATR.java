package tongtong.qiangqiang.data.factor.composite;


import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-25.
 */
public class ATR extends WIN<BarInfo> {

    public final Intermediate TR;

    public final SMA ATR;

    public final int period;

    public ATR(int cacheSize, int period) {
        super(1);
        this.period = period;
        this.TR = new Intermediate(cacheSize);
        this.ATR = new SMA(cacheSize, period);
    }

    public ATR(int period) {
        this(MAX_VALUE, period);
    }

    @Override
    public String getName() {
        return "ATR[" + period + "]";
    }

    @Override
    public double update(BarInfo input) {
        double tr = input.highPrice - input.lowPrice;
        if (cache.isEmpty()) {
            TR.update(tr);
            ATR.update(tr);
        } else {
            BarInfo pre = cache.tail();
            tr = max(tr, max(abs(pre.closePrice - input.highPrice), abs(pre.closePrice - input.lowPrice)));
            TR.update(tr);
            ATR.update(tr);
        }
        cache.push(input);
        return ATR.value.last(0);
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return ATR;
    }
}
