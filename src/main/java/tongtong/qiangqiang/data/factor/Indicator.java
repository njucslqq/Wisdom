package tongtong.qiangqiang.data.factor;

import tongtong.qiangqiang.data.factor.single.SingleIndicator;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;

import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public interface Indicator<T> {

    default Intermediate of(Indicator another) {
        return getPrimary().of(another.getPrimary());
    }

    default Intermediate derivative(int gap) {
        return getPrimary().derivative(gap);
    }

    default Intermediate plus(double delta) {
        return getPrimary().plus(delta);
    }

    default Intermediate plus(Indicator another) {
        return getPrimary().plus(another.getPrimary());
    }

    default Intermediate minus(double delta) {
        return getPrimary().minus(delta);
    }

    default Intermediate minus(Indicator another) {
        return getPrimary().minus(another.getPrimary());
    }

    default Intermediate times(double coefficient) {
        return getPrimary().times(coefficient);
    }

    default Intermediate log(double n) {
        return getPrimary().log(n);
    }

    default List<Double> lastn(int n) {
        return getPrimary().lastn(n);
    }

    default List<Double> all() {
        return getPrimary().all();
    }

    default List<Double> sub(int from, int to) {
        return getPrimary().sub(from, to);
    }

    default int size() {
        return getPrimary().size();
    }

    abstract double update(T input);

    abstract String getName();

    abstract SingleIndicator<?> getPrimary();
}
