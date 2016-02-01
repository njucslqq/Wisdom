package tongtong.qiangqiang.data.indicator.basic;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public abstract class MovingAverage extends BasicWindowIndicator {

    protected MovingAverage(int n) {
        super(n);
    }

    @Override
    public int skip() {
        return n;
    }
}
