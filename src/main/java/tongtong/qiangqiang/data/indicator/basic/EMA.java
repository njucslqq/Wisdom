package tongtong.qiangqiang.data.indicator.basic;


import static tongtong.qiangqiang.func.GeneralUtilizer.valueDefault;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class EMA extends MovingAverage {

    public EMA(int n) {
        super(n);
    }

    @Override
    public double action(Object o) {
        double v = valueDefault(o);
        if (window.size() == 1)
            data.add(v);
        else {
            double k = 2. / (window.size() + 1.);
            double prev = data.getLast();
            data.add(prev * (1 - k) + v * k);
        }
        return data.getLast();
    }

    @Override
    public String name() {
        return "EMA" + n;
    }
}
