package tongtong.qiangqiang.data.factor.single;

import tongtong.qiangqiang.data.factor.Indicator;
import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.tool.SlidingWindow;

import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-02-01.
 */
public abstract class SingleIndicator<T> extends WIN<T> {

    public final SlidingWindow<Double> value;

    public SingleIndicator(int valueCapacity, int cacheCapacity) {
        super(cacheCapacity);
        this.value = new SlidingWindow<>(valueCapacity);
    }

    @Override
    public Intermediate of(Indicator another) {
        return null;
    }

    @Override
    public Intermediate derivative(int gap) {
        return getPrimary().derivative(gap);
    }

    @Override
    public Intermediate plus(double delta) {
        return getPrimary().plus(delta);
    }

    @Override
    public Intermediate plus(Indicator another) {
        return getPrimary().plus(another.getPrimary());
    }

    @Override
    public Intermediate minus(double delta) {
        return getPrimary().minus(delta);
    }

    @Override
    public Intermediate minus(Indicator another) {
        return getPrimary().minus(another.getPrimary());
    }

    @Override
    public Intermediate times(double coefficient) {
        return getPrimary().times(coefficient);
    }

    @Override
    public Intermediate log(double n) {
        return getPrimary().log(n);
    }

    @Override
    public List<Double> lastn(int n) {
        return value.lastn(n);
    }

    @Override
    public List<Double> all() {
        return value.all();
    }

    @Override
    public List<Double> sub(int from, int to) {
        return value.sub(from, to);
    }

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return this;
    }
}
