package tongtong.qiangqiang.hunt;

import tongtong.qiangqiang.func.ListReducer;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-23.
 */
public class Window {

    public static List<Double> win(List<Double> value, int size, ListReducer handler) {
        List<Double> res = new LinkedList<>();
        for (int i = 0; i < value.size(); i += size) {
            int j = Math.min(value.size(), i + size);
            res.add(handler.reduce(value.subList(i, j)));
        }
        return res;
    }
}
