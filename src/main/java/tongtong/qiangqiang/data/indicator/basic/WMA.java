package tongtong.qiangqiang.data.indicator.basic;

import java.util.List;

import static tongtong.qiangqiang.func.GeneralUtilizer.defaultWeights;


/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class WMA extends MovingAverage {

    public final List<Double> weights;

    public WMA(int n) {
        this(defaultWeights(n));
    }

    public WMA(List<Double> weights) {
        super(weights.size());
        this.weights = weights;
    }

    @Override
    public String name() {
        return "WMA" + n;
    }

    @Override
    public double action(Object o) {
        if (window.size() < n)
            data.addLast(wma(window, defaultWeights(window.size())));
        else
            data.addLast(wma(window, weights));
        return data.getLast();
    }

    public static double wma(List<Double> value, List<Double> weight) {
        assert value.size() == weight.size();
        double sum = 0.;
        for (int i = 0; i < value.size(); i++)
            sum += value.get(i) * weight.get(i);
        return sum;
    }
}
