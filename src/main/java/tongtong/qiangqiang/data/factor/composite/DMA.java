package tongtong.qiangqiang.data.factor.composite;

import tongtong.qiangqiang.data.factor.MovingAverage;
import tongtong.qiangqiang.data.factor.WindowIndicator;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class DMA extends WindowIndicator<Double> {

    public final MovingAverage fast;

    public final MovingAverage slow;

    public final Intermediate dma;

    public final MovingAverage ama;

    public final int m;

    public final int n;

    public final int k;

    public DMA(MovingAverage fast, MovingAverage slow, MovingAverage ama) {
        super(0);
        m = fast.period();
        n = slow.period();
        k = ama.period();
        this.fast = fast;
        this.slow = slow;
        this.ama = ama;
        this.dma = new Intermediate();
    }

    public DMA(int m, int n, int k) {
        this(m, n, k, MAX_VALUE);
    }

    public DMA(int m, int n, int k, int cacheSize) {
        super(0);
        this.m = m;
        this.n = n;
        this.k = k;
        ama = new SMA(cacheSize, k);
        slow = new SMA(cacheSize, n);
        fast = new SMA(cacheSize, m);
        dma = new Intermediate(cacheSize);
    }

    @Override
    public String name() {
        return "DMA[" + m + "][" + n + "][" + k + "]";
    }

    @Override
    public double update(Double input) {
        double f = fast.update(input);
        double s = slow.update(input);
        double d = dma.update(f - s);
        return ama.update(d);
    }

    @Override
    public SingleIndicator<?> primary() {
        return dma;
    }
}