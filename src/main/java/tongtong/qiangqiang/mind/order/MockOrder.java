package tongtong.qiangqiang.mind.order;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class MockOrder extends BaseOrder {

    public MockOrder(String name) {
        super(name);
    }

    @Override
    public String buy(String id, int share, double price) {
        if (buyAction(price))
            return "\n[long  open]: " + price;
        else
            return "";
    }

    @Override
    public String sell(String id, int share, double price) {
        if (sellAction(price)) {
            profitChart.vis("HH:mm:ss", profit);
            return "[long close]: " + price + ", delta: " + longProfit.getLast() + ", profit: " + lDif;
        } else
            return "";
    }

    @Override
    public String buyClose(String id, int share, double price) {
        if (buyCloseAction(price)) {
            profitChart.vis("HH:mm:ss", profit);
            return "[short close]: " + price + ", delta: " + shortProfit.getLast() + ", profit: " + sDif;
        } else
            return "";
    }

    @Override
    public String sellOpen(String id, int share, double price) {
        if (sellOpenAction(price))
            return "\n[short open]: " + price;
        else
            return "";
    }
}
