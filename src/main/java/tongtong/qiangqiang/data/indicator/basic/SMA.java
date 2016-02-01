package tongtong.qiangqiang.data.indicator.basic;


import static tongtong.qiangqiang.func.GeneralUtilizer.valueDefault;

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
        return "SMA" + n;
    }

    @Override
    public double action(Object o) {
        double v = valueDefault(o);
        data.addLast(window.stream().mapToDouble(d -> d).average().getAsDouble());
        return data.getLast();
    }
}
