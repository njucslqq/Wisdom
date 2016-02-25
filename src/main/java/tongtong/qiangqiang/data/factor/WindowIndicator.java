package tongtong.qiangqiang.data.factor;

import tongtong.qiangqiang.data.factor.tool.SlidingWindow;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-24.
 */
public abstract class WindowIndicator<T> implements Indicator<T> {

    public final SlidingWindow<T> previous;

    public WindowIndicator(int windowCapacity) {
        previous = new SlidingWindow<>(windowCapacity);
    }
}