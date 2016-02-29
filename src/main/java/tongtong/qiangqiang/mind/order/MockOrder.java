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
        if (!lPos) {
            lPos = true;
            lPrice = price;
            lTime++;
            profit.add(lDif + sDif);
            return "\n[long  open]: " + price;
        }
        return "";
    }

    @Override
    public String sell(String id, int share, double price) {
        if (lPos) {
            lPos = false;
            double delta = (price - lPrice) - 0.2;
            lDif += delta;
            profit.add(lDif + sDif);
            profitChart.vis("HH:mm:ss", profit);
            return "[long close]: " + price + ", delta: " + delta + ", profit: " + lDif;
        }
        return "";
    }

    @Override
    public String buyClose(String id, int share, double price) {
        if (sPos) {
            sPos = false;
            double delta = (sPrice - price) - 0.2;
            sDif += delta;
            profit.add(lDif + sDif);
            profitChart.vis("HH:mm:ss", profit);
            return "[short close]: " + price + ", delta: " + delta + ", profit: " + sDif;
        }
        return "";
    }

    @Override
    public String sellOpen(String id, int share, double price) {
        if (!sPos) {
            sPos = true;
            sPrice = price;
            sTime++;
            profit.add(lDif + sDif);
            return "\n[short open]: " + price;
        }
        return "";
    }

    @Override
    public double total() {
        return lDif + sDif;
    }

    @Override
    public void conclude() {
        System.out.println("\nLong  Profit: " + lDif);
        System.out.println("Short Profit: " + sDif);
        profitChart.vis("HH:mm:ss", profit);
    }
}
