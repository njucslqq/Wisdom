package tongtong.qiangqiang.mind.order;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class RealOrder implements Order {

    @Override
    public boolean buy(String id, int share, double price) {
        return false;
    }

    @Override
    public boolean sell(String id, int share, double price) {
        return false;
    }

    @Override
    public boolean buyClose(String id, int share, double price) {
        return false;
    }

    @Override
    public boolean sellOpen(String id, int share, double price) {
        return false;
    }

    @Override
    public void conclude() {

    }
}
