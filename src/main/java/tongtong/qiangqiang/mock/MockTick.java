package tongtong.qiangqiang.mock;

import biz.source_code.dsp.filter.IirFilter;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TickInfo;
import tongtong.qiangqiang.research.FileEcho;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static biz.source_code.dsp.filter.FilterCharacteristicsType.butterworth;
import static cn.quanttech.quantera.common.data.TimeFrame.TICK;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.H.ticks;
import static tongtong.qiangqiang.func.Util.*;
import static tongtong.qiangqiang.research.Filter.warm;
import static tongtong.qiangqiang.research.Research.BASE;
import static tongtong.qiangqiang.research.Research.getLowPassFilter;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public class MockTick extends MockBase {

    String code = "IF1601";
    LocalDate warmDay = of(2015, 12, 14);
    List<Double> warmData = extract(ticks(code, warmDay), "lastPrice");
    List<Double> tmp = new ArrayList<>();
    double bond = 0.09;
    int size = 127;

    IirFilter iir = null;

    List<Double> win = new LinkedList<>();
    List<Double> smooth = new LinkedList<>();

    boolean LONG = false;
    boolean SHORT = false;
    double longPrice = 0;
    double shortPrice = 0;
    double longDiff = 0;
    double shortDiff = 0;

    FileEcho echo = new FileEcho(BASE+"openclose.csv");

    @Override
    void init() {
        setSecurity(code);
        setResolution(TICK);
        setStart(of(2015, 12, 15));
        setEnd(of(2015, 12, 15));

        iir = getLowPassFilter(butterworth, bond);
        warm(window(warmData, size), iir);
    }

    @Override
    void onData(BaseData data, int index) {
        TickInfo tick = (TickInfo) data;
        if (tick.lastPrice <= 0.01)
            return;

        echo.write(tick.lastPrice + ",");

        tmp.add(tick.lastPrice);
        if (tmp.size() == size) {
            double avg = wma(tmp, defaultWeights(size)); //tmp.stream().mapToDouble(d -> d).sum() / size;
            win.add(0, avg);
            smooth.add(0, iir.step(avg));
            tmp.clear();

            if (smooth.size() == 1) {
                echo.write("\r\n");
                return;
            }

            if (smooth.get(0) > smooth.get(1)){
                if(LONG == false) {
                    LONG = true;
                    longPrice = tick.lastPrice;
                    echo.write((tick.lastPrice + 100)+",");
                }
                else
                    echo.write(",");

                if(SHORT == true){
                    SHORT = false;
                    shortDiff += (shortPrice-tick.lastPrice);
                    echo.write((tick.lastPrice + 100));
                }
            } else if (smooth.get(0) < smooth.get(1)) {
                if (LONG == true) {
                    LONG = false;
                    longDiff += (tick.lastPrice - longPrice);
                    echo.write((tick.lastPrice + 20)+",");
                }
                else
                    echo.write(",");

                if(SHORT == false){
                    SHORT = true;
                    shortPrice = tick.lastPrice;
                    echo.write((tick.lastPrice + 20));
                }
            }
        }

        echo.write("\r\n");
    }

    @Override
    void onComplete() {
        System.out.println("多头盈利：" + longDiff);
        System.out.println("空头盈利：" + shortDiff);
    }

    public static void main(String[] args) {
        MockTick m = new MockTick();
        m.init();
        m.simulate();
        m.onComplete();
    }
}
