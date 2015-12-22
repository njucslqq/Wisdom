package tongtong.qiangqiang;

import cn.quanttech.quantera.common.data.TimeFrame;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-10.
 */
public class Preprocessing {

    public static void main(String[] args) {
        Preprocessing p = new Preprocessing();
        LocalDate date = LocalDate.now().minusDays(2);
        //p.barAvg("IF1512", TimeFrame.MIN_1, LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX), "FINANCIAL_FUTURES", 6, "derivative");
    }


}
