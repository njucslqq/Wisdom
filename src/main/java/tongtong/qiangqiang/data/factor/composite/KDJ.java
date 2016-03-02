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
 * Created on 2016-02-25.
 */
public class KDJ extends WIN<BarInfo> {

    public final Intermediate rsv;

    public final EMA k, d;

    public final Intermediate j;

    public final int period;

    public KDJ(int period) {
        this(period, MAX_VALUE);
    }

    public KDJ(int period, int cacheSize) {
        super(period);
        this.period = period;
        rsv = new Intermediate(cacheSize);
        k = new EMA(cacheSize, period);
        d = new EMA(cacheSize, period);
        j = new Intermediate(cacheSize);
    }

    @Override
    public String getName() {
        return "KDJ[" + period + "]";
    }

    @Override
    public double update(BarInfo input) {
        cache.push(input);
        double high = cache.all().stream().mapToDouble(d -> d.highPrice).max().getAsDouble();
        double low = cache.all().stream().mapToDouble(d -> d.lowPrice).min().getAsDouble();
        double _rsv = rsv.update(100 * (input.closePrice - low) / (high - low));
        double _k = k.update(_rsv);
        double _d = d.update(_k);
        return j.update(3 * _k - 2 * _d);
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return j;
    }
}
