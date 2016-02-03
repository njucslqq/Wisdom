package tongtong.qiangqiang.mock;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-01-14.
 */
public abstract class TradeBase {

    /**
     * trade
     */
    protected boolean LONG = false;

    protected boolean SHORT = false;

    protected double longPrice = 0;

    protected double shortPrice = 0;

    protected double longDiff = 0;

    protected double shortDiff = 0;

    protected int longTime = 0;

    protected int shortTime = 0;

    protected boolean buyOpen(double price) {
        if (!LONG) {
            LONG = true;
            longPrice = price;
            longTime++;
            System.out.println("多头开仓: " + price);
            return true;
        }
        return false;
    }

    protected boolean sellClose(double price) {
        if (LONG) {
            LONG = false;
            System.out.println("多头平仓，原来利润: " + longDiff + ", 增加利润：" + (price - longPrice));
            longDiff += price - longPrice;
            return true;
        }
        return false;
    }

    protected boolean sellOpen(double price) {
        if (!SHORT) {
            SHORT = true;
            shortPrice = price;
            shortTime++;
            System.out.println("空头开仓: " + price);
            return true;
        }
        return false;
    }

    protected boolean buyClose(double price) {
        if (SHORT) {
            SHORT = false;
            System.out.println("空头平仓，原来利润: " + shortDiff + ", 增加利润：" + (shortPrice - price));
            shortDiff += shortPrice - price;
            return true;
        }
        return false;
    }
}
