package tongtong.qiangqiang.data.factor.composite;

import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SUM;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-02.
 */
public class VOSC extends WIN<Double> {

    public final SUM fast, slow;

    public final Intermediate vosc;

    public final int m, n;

    public VOSC(int m, int n) {
        super(0);
        this.m = m;
        this.n = n;
        fast = new SUM(m);
        slow = new SUM(n);
        vosc = new Intermediate();
    }

    @Override
    public double update(Double input) {
        double f = fast.update(input);
        double s = slow.update(input);
        return vosc.update((s - f) / s);
    }

    @Override
    public String getName() {
        return "VOSC[" + m + "][" + n + "]";
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return vosc;
    }
}
