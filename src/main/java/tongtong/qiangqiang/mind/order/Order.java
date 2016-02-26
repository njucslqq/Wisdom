package tongtong.qiangqiang.mind.order;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public interface Order {

    boolean buy(String id, int share, double price);

    boolean sell(String id, int share, double price);

    boolean buyClose(String id, int share, double price);

    boolean sellOpen(String id, int share, double price);

    void conclude();
}
