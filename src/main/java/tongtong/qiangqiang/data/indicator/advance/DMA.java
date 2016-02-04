package tongtong.qiangqiang.data.indicator.advance;

import tongtong.qiangqiang.data.indicator.basic.BasicIndicator;
import tongtong.qiangqiang.data.indicator.basic.MovingAverage;
import tongtong.qiangqiang.data.indicator.SuperIndicator;
import tongtong.qiangqiang.data.indicator.basic.DEF;
import tongtong.qiangqiang.data.indicator.basic.SMA;

import static java.lang.Math.max;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class DMA extends AdvanceIndicator {

    public final MovingAverage fast;

    public final MovingAverage slow;

    public final DEF dma;

    public final MovingAverage ama;

    public final int m;

    public final int n;

    public final int k;

    public DMA(MovingAverage fast, MovingAverage slow, MovingAverage ama) {
        m = fast.n;
        n = slow.n;
        k = ama.n;
        this.fast = fast;
        this.slow = slow;
        this.ama = ama;
        this.dma = new DEF();
    }

    public DMA(int m, int n, int k) {
        this.m = m;
        this.n = n;
        this.k = k;
        ama = new SMA(k);
        dma = new DEF();
        slow = new SMA(n);
        fast = new SMA(m);
    }

    @Override
    public int skip() {
        return max(k, max(m, n));
    }

    @Override
    public int size() {
        return ama.size();
    }

    @Override
    public String name() {
        return "DMA[" + m + "][" + n + "][" + k + "]";
    }

    @Override
    public double update(Object o) {
        double f = fast.update(o);
        double s = slow.update(o);
        double d = dma.update(f - s);
        return ama.update(d);
    }

    @Override
    public BasicIndicator primary() {
        return ama;
    }
}
