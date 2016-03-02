package tongtong.qiangqiang.data.factor.composite;


import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-25.
 */
public class MFI extends WIN<BarInfo> {

    public final Intermediate tp, pmf, nmf, mfi;

    public final int period;

    public MFI(int period) {
        this(period, MAX_VALUE);
    }

    public MFI(int period, int cacheSize) {
        super(0);
        this.period = period;
        tp = new Intermediate(cacheSize);
        pmf = new Intermediate(cacheSize);
        nmf = new Intermediate(cacheSize);
        mfi = new Intermediate(cacheSize);
    }

    @Override
    public String getName() {
        return "MFI";
    }

    @Override
    public double update(BarInfo input) {
        double _tp = (input.highPrice + input.lowPrice + input.closePrice) / 3.;
        if (tp.size() == 0) {
            tp.update(_tp);
            pmf.update(0.0);
            nmf.update(0.0);
            mfi.update(0.0);
        } else {
            double _mf = _tp * input.volume;
            pmf.update(_tp > tp.value.last(0) ? _mf : 0.0);
            nmf.update(_tp < tp.value.last(0) ? _mf : 0.0);
            double _pmf = 0.0, _nmf = 0.0;
            if (pmf.size() < period) {
                _pmf = pmf.value.all().stream().mapToDouble(d -> d).sum();
                _nmf = nmf.value.all().stream().mapToDouble(d -> d).sum();
            } else {
                _pmf = pmf.lastn(period).stream().mapToDouble(d -> d).sum();
                _nmf = nmf.lastn(period).stream().mapToDouble(d -> d).sum();
            }
            mfi.update(_pmf / (_pmf + _nmf));
            tp.update(_tp);
        }
        return mfi.value.last(0);
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return mfi;
    }
}
