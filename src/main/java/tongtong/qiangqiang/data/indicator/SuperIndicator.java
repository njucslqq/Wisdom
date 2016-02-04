package tongtong.qiangqiang.data.indicator;

import org.apache.commons.lang3.tuple.Pair;
import tongtong.qiangqiang.data.indicator.basic.BasicIndicator;

import java.util.List;
import java.util.Map;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public interface SuperIndicator {

    @SuppressWarnings("unchecked")
    default <T extends SuperIndicator> T derivative(T n) throws IllegalAccessException, InstantiationException {
        if (primary().data.isEmpty())
            return n;
        n.add(0.);
        for (int i = 1; i < primary().data.size(); i++)
            n.add(primary().data.get(i) - primary().data.get(i - 1));
        return n;
    }

    @SuppressWarnings("unchecked")
    default <T extends SuperIndicator> T plus(SuperIndicator another, T n) throws IllegalAccessException, InstantiationException {
        int len = Math.max(primary().data.size(), another.primary().data.size());
        for (int i = 0; i < len; i++) {
            double a = i >= primary().data.size() ? 0. : primary().data.get(i);
            double b = i >= another.primary().data.size() ? 0. : another.primary().data.get(i);
            n.add(a + b);
        }
        return n;
    }

    @SuppressWarnings("unchecked")
    default <T extends SuperIndicator> T minus(SuperIndicator another, T n) throws IllegalAccessException, InstantiationException {
        int len = Math.max(primary().data.size(), another.primary().data.size());
        for (int i = 0; i < len; i++) {
            double a = i >= primary().data.size() ? 0. : primary().data.get(i);
            double b = i >= another.primary().data.size() ? 0. : another.primary().data.get(i);
            n.add(a - b);
        }
        return n;
    }

    @SuppressWarnings("unchecked")
    default <T extends SuperIndicator> T times(double v, T n) throws IllegalAccessException, InstantiationException {
        for (Double d : primary().data)
            n.add(d * v);
        return n;
    }

    @SuppressWarnings("unchecked")
    default <T extends SuperIndicator> T of(SuperIndicator another, T n) throws IllegalAccessException, InstantiationException {
        for (Double d : primary().data)
            n.update(d);
        return n;
    }

    default void add(Double v) {
        primary().data.add(v);
    }

    int skip();

    int size();

    String name();

    double update(Object o);

    BasicIndicator primary();

    List<Pair<String, BasicIndicator>> fields(String prefix);
}
