package tongtong.qiangqiang.data.indicator.basic;


import static tongtong.qiangqiang.func.GeneralUtilizer.valueDefault;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class DEF extends BasicIndicator {

    @Override
    public int skip() {
        return 0;
    }

    @Override
    public String name() {
        return "DEFAULT";
    }

    @Override
    public double update(Object o) {
        add(valueDefault(o));
        return data.getLast();
    }
}
