package tongtong.qiangqiang.mock;

import biz.source_code.dsp.filter.IirFilter;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import tongtong.qiangqiang.research.FileEcho;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static biz.source_code.dsp.filter.FilterCharacteristicsType.butterworth;
import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static tongtong.qiangqiang.data.H.bars;
import static tongtong.qiangqiang.func.Util.*;
import static tongtong.qiangqiang.research.Filter.warm;
import static tongtong.qiangqiang.research.Research.BASE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public class MockMinute extends MockBase {

    String code = "zn1601";
    LocalDate warmDay = LocalDate.of(2015, 11, 25);
    List<Double> warmData = extract(bars(code, MIN_1, warmDay), "lastPrice");
    List<Double> tmp = new ArrayList<>();
    double bond = 0.09;
    int size = 23;

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
        setResolution(MIN_1);
        setStart(LocalDate.of(2015, 11, 26));
        setEnd(LocalDate.of(2015, 12, 4));

        iir = null;
        warm(warmData, iir);
    }

    @Override
    void onData(BaseData data, int index) {
        BarInfo bar = (BarInfo) data;
        if (bar.closePrice <= 0.01)
            return;

        echo.write(bar.closePrice + ",");

        tmp.add(bar.closePrice);
        if (tmp.size() == size) {
            double avg = wma(tmp, defaultWeights(size)); //tmp.stream().mapToDouble(d -> d).sum() / windowSize;
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
                    longPrice = bar.closePrice;
                    echo.write((bar.closePrice + 100)+",");
                }
                else
                    echo.write(",");

                if(SHORT == true){
                    SHORT = false;
                    shortDiff += (shortPrice-bar.closePrice);
                    echo.write((bar.closePrice + 100));
                }
            } else if (smooth.get(0) < smooth.get(1)) {
                if (LONG == true) {
                    LONG = false;
                    longDiff += (bar.closePrice - longPrice);
                    echo.write((bar.closePrice + 20)+",");
                }
                else
                    echo.write(",");

                if(SHORT == false){
                    SHORT = true;
                    shortPrice = bar.closePrice;
                    echo.write((bar.closePrice + 20));
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
        MockMinute m = new MockMinute();
        m.init();
        m.simulate();
        m.onComplete();
    }
}
