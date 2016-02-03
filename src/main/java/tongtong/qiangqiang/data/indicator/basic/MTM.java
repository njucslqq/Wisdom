package tongtong.qiangqiang.data.indicator.basic;

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
