package tongtong.qiangqiang.data.factor.composite;

import cn.quanttech.quantera.common.data.BarInfo;
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
public class ADVOL extends WIN<BarInfo> {

    public final Intermediate adv;

    public final Intermediate advol;

    public final int period;

    public ADVOL(int period) {
        this(period, MAX_VALUE);
    }

    public ADVOL(int period, int cacheSize) {
        super(0);
        this.period = period;
        adv = new Intermediate(cacheSize);
        advol = new Intermediate(cacheSize);
    }

    @Override
    public String name() {
        return "ADVOL[" + period + "]";
    }

    @Override
    public double update(BarInfo input) {
        double tmp = 0.0;
        if (input.highPrice != input.lowPrice)
            tmp = ((input.closePrice - input.lowPrice) - (input.highPrice - input.closePrice)) / (input.highPrice - input.lowPrice);
        adv.update(tmp * input.volume);
        double _advol = 0.0;
        if (adv.dataSize() < period)
            _advol = adv.data.all().stream().mapToDouble(d -> d).sum();
        else
            _advol = adv.last(period).stream().mapToDouble(d -> d).sum();
        return advol.update(_advol);
    }

    @Override
    public SingleIndicator<?> primary() {
        return advol;
    }
}
