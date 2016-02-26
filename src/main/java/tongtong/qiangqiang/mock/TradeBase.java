package tongtong.qiangqiang.mock;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-01-14.
 */
public abstract class TradeBase {

    protected boolean lPos = false;

    protected boolean sPos = false;

    protected double lPrice = 0.;

    protected double sPrice = 0.;

    protected double lDif = 0.;

    protected double sDif = 0.;

    protected int lTime = 0;

    protected int sTime = 0;

    protected boolean buyOpen(double price) {
        if (!lPos) {
            lPos = true;
            lPrice = price;
            lTime++;
            //System.out.println("\n[long  open]: " + price);
            return true;
        }
        return false;
    }

    protected boolean sellClose(double price) {
        if (lPos) {
            lPos = false;
            lDif += price - lPrice;
            //System.out.println("[long close]: " + price + ", delta: " + (price - lPrice) + ", profit: " + lDif);
            return true;
        }
        return false;
    }

    protected boolean sellOpen(double price) {
        if (!sPos) {
            sPos = true;
            sPrice = price;
            sTime++;
            //System.out.println("\n[short open]: " + price);
            return true;
        }
        return false;
    }

    protected boolean buyClose(double price) {
        if (sPos) {
            sPos = false;
            sDif += sPrice - price;
            //System.out.println("[short close]: " + price + ", delta: " + (sPrice - price) + ", profit: " + sDif);
            return true;
        }
        return false;
    }
}
