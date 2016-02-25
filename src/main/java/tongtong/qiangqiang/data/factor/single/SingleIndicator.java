package tongtong.qiangqiang.data.factor.single;

import tongtong.qiangqiang.data.factor.WindowIndicator;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-02-01.
 */
public abstract class SingleIndicator<T> extends WindowIndicator<T> {

    public final LinkedList<Double> data = new LinkedList<>();

    public final int cacheSize;

    public SingleIndicator(int cacheSize, int windowCapacity) {
        super(windowCapacity);
        this.cacheSize = cacheSize;
    }

    protected double push(double value) {
        data.addLast(value);
        if (data.size() > cacheSize)
            data.removeFirst();
        return data.getLast();
    }

    @Override
    public int dataSize() {
        return data.size();
    }

    @Override
    public SingleIndicator<?> primary() {
        return this;
    }

    @Override
    public List<Double> sub(int from, int to) {
        if (from < 0 || to < 0 || from >= data.size() || to >= data.size() || from >= to)
            throw new RuntimeException("index is illegal");
        return data.subList(from, to);
    }
}
