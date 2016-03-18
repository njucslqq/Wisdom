package tongtong.qiangqiang.mind.order;

import java.util.List;

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

    String sellSilent(String id, int share, double price);

    String buyCloseSilent(String id, int share, double price);

    double floatLongProfit(double lastPrice);

    double floatShortProfit(double lastPrice);

    double longReturn();

    double shortReturn();

    double totalReturn();

    List<Double> profit();

    String conclude();
}
