package tongtong.qiangqiang.func;

import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016-02-01.
 */
@FunctionalInterface
public interface ListReducer {

    Double reduce(List<Double> data);
}
