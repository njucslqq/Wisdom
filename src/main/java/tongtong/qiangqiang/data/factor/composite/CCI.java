package tongtong.qiangqiang.data.factor.composite;

import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.WindowIndicator;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.abs;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-25.
 */
public class CCI extends WindowIndicator<BarInfo> {

    public final Intermediate tp;

    public final SMA m;

    public final SMA d;

    public final Intermediate cci;

    public final int period;

    public CCI(int period, int cacheSize) {
        super(0);
        this.period = period;
        tp = new Intermediate(cacheSize);
        m = new SMA(cacheSize, period);
        d = new SMA(cacheSize, period);
        cci = new Intermediate(cacheSize);
    }

    public CCI(int period) {
        this(period, MAX_VALUE);
    }

    @Override
    public String name() {
        return "CCI[" + period + "]";
    }

    @Override
    public double update(BarInfo input) {
        double _tp = tp.update((input.highPrice + input.closePrice + input.lowPrice) / 3.);
        double _m = m.update(_tp);
        double _d = d.update(abs(_tp - _m));
        return cci.update((_tp - _m) / (0.015 * _d));
    }

    @Override
    public SingleIndicator<?> primary() {
        return cci;
    }
}
