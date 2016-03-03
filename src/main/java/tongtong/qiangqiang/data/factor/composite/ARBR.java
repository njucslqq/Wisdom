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
 * Created on 2016-03-02.
 */
public class ARBR extends WIN<BarInfo> {

    public final SUM ho, ol, hc, cl;

    public final Intermediate ar, br;

    public final int period;

    public ARBR(int period, int valueCapacity) {
        super(0);
        this.period = period;
        ho = new SUM(valueCapacity, period);
        ol = new SUM(valueCapacity, period);
        hc = new SUM(valueCapacity, period);
        cl = new SUM(valueCapacity, period);
        ar = new Intermediate(valueCapacity);
        br = new Intermediate(valueCapacity);
    }

    public ARBR(int period) {
        this(period, MAX_VALUE);
    }

    @Override
    public double update(BarInfo input) {
        double HO = ho.update(input.highPrice - input.openPrice);
        double OL = ol.update(input.openPrice - input.lowPrice);
        double HC = hc.update(input.highPrice - input.closePrice);
        double CL = cl.update(input.closePrice - input.lowPrice);
        br.update(HC / CL);
        return ar.update(HO / OL);
    }

    @Override
    public String getName() {
        return "ARBR[" + period + "]";
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return ar;
    }
}
