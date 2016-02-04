package tongtong.qiangqiang.data.indicator.advance;

import tongtong.qiangqiang.data.indicator.basic.BasicIndicator;
import tongtong.qiangqiang.data.indicator.basic.DEF;
import tongtong.qiangqiang.data.indicator.basic.MovingAverage;
import tongtong.qiangqiang.data.indicator.basic.SMA;

import static java.lang.Math.sqrt;
import static tongtong.qiangqiang.func.GeneralUtilizer.valueDefault;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class BOLL extends AdvanceWindowIndicator {

    public final MovingAverage middle;

    public final DEF up;

    public final DEF down;

    public final double k;

    public BOLL(int n, double k) {
        super(n);
        this.k = k;
        down = new DEF();
        up = new DEF();
        middle = new SMA(n);
    }

    @Override
    public int skip() {
        return n;
    }

    @Override
    public int size() {
        return middle.size();
    }

    @Override
    public String name() {
        return "BOLL[" + n + "][" + k + "]";
    }

    @Override
    public BasicIndicator primary() {
        return middle;
    }

    @Override
    public double action(Object o) {
        double v = valueDefault(o);
        double exp = middle.update(v);
        double sigma = 0.0;
        for (Double d : window)
            sigma += (d - exp) * (d - exp);
        sigma = sqrt(sigma / n);
        up.update(exp + k * sigma);
        down.update(exp - k * sigma);
        return exp;
    }
}
