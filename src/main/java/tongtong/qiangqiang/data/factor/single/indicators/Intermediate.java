package tongtong.qiangqiang.data.factor.single.indicators;

import tongtong.qiangqiang.data.factor.single.SingleIndicator;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class Intermediate extends SingleIndicator<Double> {

    public Intermediate() {
        this(MAX_VALUE);
    }

    public Intermediate(int cacheSize) {
        super(cacheSize, 0);
    }

    @Override
    public double update(Double input) {
        data.add(input);
        return data.getLast();
    }

    @Override
    public String name() {
        return "Intermediate";
    }
}
