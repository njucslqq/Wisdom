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
        return null;
    }

    default Intermediate derivative(int gap) {
        return null;
    }

    default Intermediate plus(double delta) {
        return null;
    }

    default Intermediate plus(Indicator another) {
        return null;
    }

    default Intermediate minus(double delta) {
        return null;
    }

    default Intermediate minus(Indicator another) {
        return null;
    }

    default Intermediate times(double coefficient) {
        return null;
    }

    default Intermediate log(double n) {
        return null;
    }

    default List<Double> sub(int from, int to) {
        return primary().sub(from, to);
    }

    default List<Double> last(int length) {
        int size = primary().dataSize();
        return sub(size - length, size);
    }

    default int dataSize() {
        return primary().dataSize();
    }

    abstract String name();

    abstract double update(T input);

    abstract SingleIndicator<?> primary();
}
