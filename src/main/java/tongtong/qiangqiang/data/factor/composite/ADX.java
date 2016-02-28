package tongtong.qiangqiang.data.factor.composite;

import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;

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
public class ADX extends WIN<BarInfo> {

    public final ATR atr;

    public final EMA posEma;

    public final EMA negEma;

    public final Intermediate posDI;

    public final Intermediate negDI;

    public final Intermediate dx;

    public final EMA adx;

    public final Intermediate adx100;

    public final int period;

    public ADX(int period) {
        this(MAX_VALUE, period);
    }

    public ADX(int cacheSize, int period) {
        super(1);
        this.period = period;
        atr = new ATR(cacheSize, period);
        posEma = new EMA(cacheSize, period);
        negEma = new EMA(cacheSize, period);
        posDI = new Intermediate(cacheSize);
        negDI = new Intermediate(cacheSize);
        dx = new Intermediate(cacheSize);
        adx = new EMA(cacheSize, period);
        adx100 = new Intermediate(cacheSize);
    }

    @Override
    public String name() {
        return "ADX[" + period + "]";
    }

    @Override
    public double update(BarInfo input) {
        double _atr = atr.update(input);
        if (previous.isEmpty()) {
            posEma.update(0.);
            negEma.update(0.);
            posDI.update(0.);
            negDI.update(0.);
            dx.update(0.);
            adx.update(0.);
            adx100.update(0.);
        } else {
            BarInfo prev = previous.prev();
            double high = input.highPrice - prev.highPrice;
            double low = prev.lowPrice - input.lowPrice;
            double _pos = high > max(0., low) ? high : 0.;
            double _neg = low > max(0., high) ? low : 0.;
            double _pdi = posDI.update(100 * posEma.update(_pos) / _atr);
            double _ndi = negDI.update(100 * negEma.update(_neg) / _atr);
            double _dx = dx.update(abs(_pdi - _ndi) / (_pdi + _ndi));
            adx100.update(100 * adx.update(_dx));
        }
        previous.add(input);
        return adx.data.last(0);
    }

    @Override
    public SingleIndicator<?> primary() {
        return adx;
    }
}
