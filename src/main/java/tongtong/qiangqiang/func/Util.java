package tongtong.qiangqiang.func;

import cn.quanttech.quantera.common.data.BaseData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015/11/18.
 */
public class Util {

    public static double sma(List<Double> price) {
        if (price.isEmpty()) return 0.0;
        return price.stream().mapToDouble(d -> d).sum() / price.size();
    }

    public static double ema(final List<Double> price, final int number) {
        if (price.isEmpty()) return 0.0;
        double k = 2.0 / (number + 1.0);
        double ema = price.get(0);
        for (int i = 1; i < price.size(); i++)
            ema = ema * (1 - k) + price.get(i) * k;
        return ema;
    }

    public static double wma(final List<Double> price, final List<Double> weight) {
        double sum = 0.0;
        for (int i = 0; i < price.size(); i++)
            sum += price.get(i) * (i >= weight.size() ? weight.get(weight.size() - 1) : weight.get(i));
        return sum;
    }

    public static double max(final List<Double> price) {
        if (price.isEmpty()) return 0.0;
        return price.stream().max(Double::compareTo).get();
    }

    public static double min(final List<Double> price) {
        if (price.isEmpty()) return 0.0;
        return price.stream().min(Double::compareTo).get();
    }

    public static List<Double> extract(List<? extends BaseData> data, String field) {
        List<Double> res = new ArrayList<>();
        data.stream().forEach(b -> {
            Class c = b.getClass();
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields)
                if (f.getName().equalsIgnoreCase(field))
                    try {
                        res.add(f.getDouble(b));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
        });
        return res;
    }
}
