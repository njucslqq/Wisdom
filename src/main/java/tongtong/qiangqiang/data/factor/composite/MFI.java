package tongtong.qiangqiang.data.factor.composite;


import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.WindowIndicator;
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
public class MFI extends WindowIndicator<BarInfo> {

    public final Intermediate mf, pmf, nmf, mfi;

    public MFI() {
        this(MAX_VALUE);
    }

    public MFI(int cacheSize) {
        super(1);
        mf = new Intermediate(cacheSize);
        pmf = new Intermediate(cacheSize);
        nmf = new Intermediate(cacheSize);
        mfi = new Intermediate(cacheSize);
    }

    @Override
    public String name() {
        return "MFI";
    }

    @Override
    public double update(BarInfo input) {
        double tp = (input.highPrice + input.lowPrice + input.closePrice) / 3.;
        return 0;
    }

    @Override
    public SingleIndicator<?> primary() {
        return mfi;
    }
}
