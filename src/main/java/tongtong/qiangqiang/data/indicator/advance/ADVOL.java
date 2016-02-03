package tongtong.qiangqiang.data.indicator.advance;

import tongtong.qiangqiang.data.indicator.basic.BasicIndicator;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-02-02.
 */
public class ADVOL extends AdvanceIndicator {
    @Override
    public int skip() {
        return 0;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public double update(Object o) {
        return 0;
    }

    @Override
    public BasicIndicator primary() {
        return null;
    }
}
