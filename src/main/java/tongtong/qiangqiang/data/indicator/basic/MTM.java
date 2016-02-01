package tongtong.qiangqiang.data.indicator.basic;

import tongtong.qiangqiang.data.indicator.advance.AdvanceWindowIndicator;
import tongtong.qiangqiang.data.indicator.basic.BasicIndicator;
import tongtong.qiangqiang.data.indicator.basic.BasicWindowIndicator;
import tongtong.qiangqiang.func.GeneralUtilizer;

import static tongtong.qiangqiang.func.GeneralUtilizer.valueDefault;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class MTM extends BasicWindowIndicator {

    public MTM() {
        super(2);
    }

    @Override
    public int skip() {
        return 2;
    }

    @Override
    public String name() {
        return "MTM";
    }

    @Override
    public double action(Object o) {
        if (window.size() == 1)
            data.addLast(0.);
        else
            data.addLast(window.getLast() - window.getFirst());
        return data.getLast();
    }
}
