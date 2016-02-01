package tongtong.qiangqiang.data.indicator.advance;

import java.util.LinkedList;

import static tongtong.qiangqiang.func.GeneralUtilizer.valueDefault;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-02-01.
 */
public abstract class AdvanceWindowIndicator extends AdvanceIndicator {

    public final int n;

    public final LinkedList<Double> window = new LinkedList<>();

    protected AdvanceWindowIndicator(int n) {
        this.n = n;
    }

    public void insert(Double v){
        if(window.size()<n)
            window.add(v);
        else {
            window.removeFirst();
            window.addLast(v);
        }
    }

    @Override
    public double update(Object o){
        double v = valueDefault(o);
        insert(v);
        return action(o);
    }

    public abstract double action(Object o);
}
