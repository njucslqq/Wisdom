package tongtong.qiangqiang.data.factor.single.indicators;

import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;

import static java.lang.Integer.MAX_VALUE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-02.
 */
public class NVI extends SingleIndicator<BarInfo> {

    public NVI() {
        this(MAX_VALUE);
    }

    public NVI(int valueCapacity) {
        super(valueCapacity, 1);
    }

    @Override
    public double update(BarInfo input) {
        if (cache.isEmpty()){
            cache.push(input);
            value.push(100.0);
        }
        else {
            if (input.volume < cache.tail().volume)
                value.push(value.tail()*(1.0 + (input.closePrice-cache.tail().closePrice)/cache.tail().closePrice));
            else
                value.push(value.tail());
            cache.push(input);
        }
        return value.tail();
    }

    @Override
    public String getName() {
        return "NVI";
    }
}
