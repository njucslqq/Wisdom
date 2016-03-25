package tongtong.qiangqiang.mind.order;

import tongtong.qiangqiang.mind.Algorithm;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/2/27.
 */
public abstract class BaseOrder implements IOrder {

    protected boolean lPos = false;

    protected boolean sPos = false;

    protected LocalDate lDate = null;

    protected LocalDate sDate = null;

    protected double lPrice = 0.;

    protected double sPrice = 0.;

    protected double lDif = 0.;

    protected double sDif = 0.;

    protected int lTime = 0;

    protected int sTime = 0;

    protected final LinkedList<Double> longProfit = new LinkedList<>();

    protected final LinkedList<Double> shortProfit = new LinkedList<>();

    protected final LinkedList<Double> totalProfit = new LinkedList<>();

    protected final double commision;

    public BaseOrder(double commision) {
        this.commision = commision;
    }

    protected boolean buyAction(double price) {
        if (!lPos) {
            lPos = true;
            lPrice = price;
            lTime++;
            lDate = tradingDay();
            return true;
        }
        return false;
    }

    protected boolean sellAction(double price) {
        if (lPos) {
            lPos = false;
            double delta = (price - lPrice) - commision;
            lDif += delta;
            longProfit.add(delta);
            totalProfit.add(lDif + sDif);
            return true;
        }
        return false;
    }

    protected boolean buyCloseAction(double price) {
        if (sPos) {
            sPos = false;
            double delta = (sPrice - price) - commision;
            sDif += delta;
            shortProfit.add(delta);
            totalProfit.add(lDif + sDif);
            return true;
        }
        return false;
    }

    protected boolean sellOpenAction(double price) {
        if (!sPos) {
            sPos = true;
            sPrice = price;
            sTime++;
            sDate = tradingDay();
            return true;
        }
        return false;
    }

    @Override
    public double floatLongProfit(double lastPrice){
        if (lPos){
            return lastPrice - lPrice;
        }
        return 0.0;
    }

    @Override
    public double floatShortProfit(double lastPrice){
        if (sPos){
            return sPrice - lastPrice;
        }
        return 0.0;
    }

    @Override
    public double longReturn() {
        return lDif;
    }

    @Override
    public double shortReturn() {
        return sDif;
    }

    @Override
    public double totalReturn() {
        return lDif + sDif;
    }

    @Override
    public List<Double> profit() {
        return totalProfit;
    }

    @Override
    public String conclude() {
        return "\n<========== Summary ==========>" +
                "\nLong  Profit: " + lDif +
                "\nShort Profit: " + sDif +
                "\nLong  Trading Time: " + lTime +
                "\nShort Trading Time: " + sTime +
                "\n<==========   End   ==========>\n";
    }

    protected LocalDate tradingDay(){
        LocalDate date = null;
        if (LocalTime.now().isAfter(Algorithm.PM_END)){
            if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.FRIDAY))
                date = LocalDate.now().plusDays(3);
            else
                date = LocalDate.now().plusDays(1);
        }else {
            if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY))
                date = LocalDate.now().plusDays(2);
            else
                date = LocalDate.now();
        }
        return date;
    }
}
