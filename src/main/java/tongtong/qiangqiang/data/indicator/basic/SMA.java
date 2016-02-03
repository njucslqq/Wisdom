package tongtong.qiangqiang.data.indicator.basic;


/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class SMA extends MovingAverage {

    public SMA(int n) {
        super(n);
    }

    @Override
    public String name() {
        return "SMA[" + n + "]";
    }

    @Override
    public double action(Object o) {
        data.addLast(window.stream().mapToDouble(d -> d).average().getAsDouble());
        return data.getLast();
    }
}
