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
    public String name() {
        return "WilliamR[" + period + "]";
    }

    @Override
    public double update(BarInfo input) {
        previous.add(input);
        double high = previous.all().stream().mapToDouble(d -> d.highPrice).max().getAsDouble();
        double low = previous.all().stream().mapToDouble(d -> d.lowPrice).min().getAsDouble();
        data.add(100. * (high - input.closePrice) / (high - low));
        return data.last(0);
    }
}
