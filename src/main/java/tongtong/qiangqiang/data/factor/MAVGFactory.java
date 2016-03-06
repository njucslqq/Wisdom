package tongtong.qiangqiang.data.factor;

import tongtong.qiangqiang.data.factor.composite.DEMA;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;
import tongtong.qiangqiang.data.factor.single.indicators.WMA;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/3/5.
 */
public class MAVGFactory {

    public static MAVG create(Class<?> clazz, int period){
        if (clazz.equals(SMA.class))
            return new SMA(period);
        if (clazz.equals(EMA.class))
            return new EMA(period);
        if (clazz.equals(WMA.class))
            return new WMA(period);
        if (clazz.equals(DEMA.class))
            return new DEMA(period);
        return new SMA(period);
    }
}
