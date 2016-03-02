package tongtong.qiangqiang.data.factor;

import tongtong.qiangqiang.data.factor.tool.SlidingWindow;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-24.
 */
public abstract class WIN<T> implements Indicator<T> {

    public final SlidingWindow<T> cache;

    public WIN(int cacheCapacity) {
        cache = new SlidingWindow<>(cacheCapacity);
    }
}