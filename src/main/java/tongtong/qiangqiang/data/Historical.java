package tongtong.qiangqiang.data;

import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.TickInfo;
import cn.quanttech.quantera.common.data.TimeFrame;

import java.time.LocalDate;
import java.util.List;

import static cn.quanttech.quantera.datacenter.DataCenter.getBarData;
import static cn.quanttech.quantera.datacenter.DataCenter.getTickData;
import static java.time.LocalDateTime.of;
import static java.time.LocalTime.MAX;
import static java.time.LocalTime.MIN;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-22.
 */
public class Historical {

    public static List<BarInfo> bars(String code, TimeFrame resolution, LocalDate start, LocalDate end){
        return getBarData(code, resolution, of(start, MIN), of(end, MAX));
    }

    public static List<BarInfo> bars(String code, TimeFrame resolution, LocalDate day){
        return getBarData(code, resolution, of(day, MIN), of(day, MAX));
    }

    public static List<TickInfo> ticks(String code, LocalDate start, LocalDate end){
        return getTickData(code, of(start, MIN), of(end, MAX));
    }

    public static List<TickInfo> ticks(String code, LocalDate day){
        return getTickData(code, of(day, MIN), of(day, MAX));
    }
}
