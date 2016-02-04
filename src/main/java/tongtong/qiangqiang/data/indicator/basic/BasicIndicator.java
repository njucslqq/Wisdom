package tongtong.qiangqiang.data.indicator.basic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import tongtong.qiangqiang.data.indicator.SuperIndicator;

import java.util.LinkedList;
import java.util.List;
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
    public int size(){
        return data.size();
    }

    @Override
    public BasicIndicator primary() {
        return this;
    }

    @Override
    public List<Pair<String, BasicIndicator>> fields(String prefix) {
        try {
            String str = prefix + "-" + name();
            return ImmutableList.of(
                    Pair.of(str, this),
                    Pair.of(str + "-derivative", this.derivative(new DEF()))
            );
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return ImmutableList.of();
        }
    }
}
