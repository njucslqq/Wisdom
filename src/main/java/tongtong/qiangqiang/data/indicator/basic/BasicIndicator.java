package tongtong.qiangqiang.data.indicator.basic;

import com.google.common.collect.ImmutableMap;
import tongtong.qiangqiang.data.indicator.SuperIndicator;

import java.util.LinkedList;
import java.util.Map;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-02-01.
 */
public abstract class BasicIndicator implements SuperIndicator {

    public final LinkedList<Double> data = new LinkedList<>();

    @Override
    public BasicIndicator primary() {
        return this;
    }

    @Override
    public Map<String, BasicIndicator> fields(String prefix) {
        try {
            return ImmutableMap.of(prefix + "." + name(), this, prefix + "." + name() + ".derivative", this.derivative(new DEF()));
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return ImmutableMap.of();
        }
    }
}
