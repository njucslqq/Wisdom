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
public class CR extends WIN<BarInfo> {

    public final SUM htp, tpl;

    public final Intermediate cr;

    public final int period;

    public CR(int period, int valueCapacity) {
        super(0);
        this.period = period;
        htp = new SUM(valueCapacity, period);
        tpl = new SUM(valueCapacity, period);
        cr = new Intermediate(valueCapacity);
    }

    public CR(int period) {
        this(period, MAX_VALUE);
    }

    @Override
    public double update(BarInfo input) {
        double tp = (input.highPrice + input.lowPrice + input.closePrice) / 3.0;
        double HTP = htp.update(input.highPrice - tp);
        double TPL = tpl.update(tp - input.lowPrice);
        return cr.update(HTP / TPL);
    }

    @Override
    public String getName() {
        return "CR[" + period + "]";
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return cr;
    }
}
