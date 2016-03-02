package tongtong.qiangqiang.data.factor.single.indicators;

import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class WilliamsR extends SingleIndicator<BarInfo> {

    public final int period;

    public WilliamsR(int period) {
        this(MAX_VALUE, period);
    }

    public WilliamsR(int cacheSize, int period) {
        super(cacheSize, period);
        this.period = period;
    }

    @Override
    public String getName() {
        return "WilliamR[" + period + "]";
    }

    @Override
    public double update(BarInfo input) {
        cache.push(input);
        double high = cache.all().stream().mapToDouble(d -> d.highPrice).max().getAsDouble();
        double low = cache.all().stream().mapToDouble(d -> d.lowPrice).min().getAsDouble();
        value.push(100. * (high - input.closePrice) / (high - low));
        return value.last(0);
    }
}
