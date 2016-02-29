package tongtong.qiangqiang.mind.order;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public interface IOrder {

    String buy(String id, int share, double price);

    String sell(String id, int share, double price);

    String buyClose(String id, int share, double price);

    String sellOpen(String id, int share, double price);

    double total();

    void conclude();
}
