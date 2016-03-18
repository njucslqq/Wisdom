package tongtong.qiangqiang.mind.order;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class MockOrder extends BaseOrder {

    public MockOrder(Double commision) {
        super(commision);
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
            return "[long close]: " + price + ", delta: " + longProfit.getLast() + ", longProfit: " + lDif + ", totalProfit: " + totalReturn();
        } else
            return "";
    }

    @Override
    public String buyClose(String id, int share, double price) {
        if (buyCloseAction(price)) {
            return "[short close]: " + price + ", delta: " + shortProfit.getLast() + ", shortProfit: " + sDif + ", totalProfit: " + totalReturn();
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

    @Override
    public String sellSilent(String id, int share, double price) {
        return "\n=======> [long  close]: stop loss: " + floatLongProfit(price);
    }

    @Override
    public String buyCloseSilent(String id, int share, double price) {
        return "\n=======> [short close]: stop loss: " + floatShortProfit(price);
    }
}
