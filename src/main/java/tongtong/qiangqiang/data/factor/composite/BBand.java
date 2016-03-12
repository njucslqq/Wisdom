package tongtong.qiangqiang.data.factor.composite;

import cn.quanttech.quantera.common.data.BarInfo;
import tongtong.qiangqiang.data.factor.WIN;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-03.
 */
public class BBand extends WIN<BarInfo> {
    
    public BBand(int cacheCapacity) {
        super(cacheCapacity);
    }

    @Override
    public double update(BarInfo input) {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public SingleIndicator<?> getPrimary() {
        return null;
    }
}
