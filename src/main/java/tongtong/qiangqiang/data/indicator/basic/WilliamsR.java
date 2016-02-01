package tongtong.qiangqiang.data.indicator.basic;


import tongtong.qiangqiang.data.indicator.basic.BasicWindowIndicator;
import tongtong.qiangqiang.data.indicator.basic.DEF;
import tongtong.qiangqiang.func.GeneralUtilizer;

import static tongtong.qiangqiang.func.GeneralUtilizer.valueDefault;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class WilliamsR extends BasicWindowIndicator {

    public WilliamsR(int n) {
        super(n);
    }

    @Override
    public int skip() {
        return n;
    }

    @Override
    public String name() {
        return "WilliamR" + n;
    }

    @Override
    public double action(Object o) {
        double v = valueDefault(o);
        double high = window.stream().mapToDouble(d -> d).max().getAsDouble();
        double low = window.stream().mapToDouble(d -> d).min().getAsDouble();
        data.add((high - v) / (high - low));
        return data.getLast();
    }
}
