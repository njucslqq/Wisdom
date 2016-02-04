package tongtong.qiangqiang.data.indicator.advance;

import org.apache.commons.lang3.tuple.Pair;
import tongtong.qiangqiang.data.indicator.SuperIndicator;
import tongtong.qiangqiang.data.indicator.basic.BasicIndicator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-02-01.
 */
public abstract class AdvanceIndicator implements SuperIndicator {

    @Override
    public List<Pair<String, BasicIndicator>> fields(String prefix) {
        List<Pair<String, BasicIndicator>> bi = new ArrayList<>();
        Class<?> c = this.getClass();
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields)
            if (SuperIndicator.class.isAssignableFrom(f.getType())) {
                try {
                    SuperIndicator ind = (SuperIndicator) f.get(this);
                    bi.addAll(ind.fields(prefix + "-" + name() + "-" + f.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        return bi;
    }
}
