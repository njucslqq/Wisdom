package tongtong.qiangqiang.mock;

import biz.source_code.dsp.filter.IirFilter;
import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TickInfo;
import cn.quanttech.quantera.datacenter.DataCenterUtil;
import tongtong.qiangqiang.research.FileEcho;
import tongtong.qiangqiang.research.Filter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static cn.quanttech.quantera.common.data.TimeFrame.TICK;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.H.ticks;
import static tongtong.qiangqiang.func.Util.*;
import static tongtong.qiangqiang.research.Research.BASE;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public class MockTick extends MockBase {

    public static final String BASE = "./signal/mock/";

    String code = "IF1601";
    LocalDate warmDay = of(2015, 12, 14);
    //List<Double> warmData = extract(ticks(code, warmDay), "lastPrice");
    List<Double> tmp = new ArrayList<>();
    double bond = 0.09;
    int windowSize = 1;
    int filterSize = 71;
    int number = 23;

    IirFilter iir = null;

    List<Double> win = new LinkedList<>();
    List<Double> smooth = new LinkedList<>();

    boolean LONG = false;
    boolean SHORT = false;
    double longPrice = 0;
    double shortPrice = 0;
    double longDiff = 0;
    double shortDiff = 0;

    FileEcho echo = new FileEcho(BASE + "openclose.csv");

    @Override
    void init() {
        setSecurity(code);
        setResolution(TICK);
        setStart(of(2015, 12, 22));
        setEnd(of(2015, 12, 22));

        //iir = getLowPassFilter(butterworth, bond);
        //warm(window(warmData, windowSize), iir);
    }

    @Override
    void onData(BaseData data, int index) {
        TickInfo tick = (TickInfo) data;
        if (tick.lastPrice <= 0.001)
            return;

        tmp.add(tick.lastPrice);
        if (tmp.size() == windowSize) {
            double avg = wma(tmp, defaultWeights(windowSize));
            win.add(0, avg);

            if (win.size() >= filterSize) {
                List<Double> list = win.subList(0, win.size());
                List<Double> reverse = new ArrayList<>();
                for (Double d : list)
                    reverse.add(0, d);
                Filter.fourierNumber(reverse, number, BASE + code + "[" + win.size() + "].csv");
            }

            tmp.clear();
        }

        //echo.write("\r\n");
    }

    @Override
    void onComplete() {
        System.out.println("多头盈利：" + longDiff);
        System.out.println("空头盈利：" + shortDiff);
    }

    public static void main(String[] args) {
        DataCenterUtil.setNetDomain(CONST.INTRA_QUANDIS_URL);
        MockTick m = new MockTick();
        m.init();
        m.simulate();
        m.onComplete();
    }
}
