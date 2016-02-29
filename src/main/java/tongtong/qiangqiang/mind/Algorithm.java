package tongtong.qiangqiang.mind;

import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import tongtong.qiangqiang.mind.MindType.Model;
import tongtong.qiangqiang.mind.order.IOrder;
import tongtong.qiangqiang.mind.order.MockOrder;
import tongtong.qiangqiang.mind.order.RealOrder;
import tongtong.qiangqiang.mind.trade.Trader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static tongtong.qiangqiang.data.Historical.bars;
import static tongtong.qiangqiang.data.Historical.ticks;
import static tongtong.qiangqiang.mind.MindType.Model.TEST;
import static tongtong.qiangqiang.mind.MindType.Order;
import static tongtong.qiangqiang.mind.MindType.Order.MARKET;
import static tongtong.qiangqiang.mind.MindType.State;
import static tongtong.qiangqiang.mind.MindType.State.MOCK;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public abstract class Algorithm {

    private String security;

    private TimeFrame resolution;

    private LocalDate start;

    private LocalDate end;

    private Model model = TEST;

    private State state = MOCK;

    private Order type = MARKET;

    private boolean print = false;

    private final String name;

    private final int port;

    private final String tsAddress;

    private final int tsPort;

    private int index;

    private Trader trader;

    private IOrder order;

    public Algorithm(String name, int port, String tsAddress, int tsPort) {
        this.name = name;
        this.port = port;
        this.tsAddress = tsAddress;
        this.tsPort = tsPort;
    }

    public Algorithm(String name) {
        this(name, 8080, "localhost", 9090);
    }

    protected void setSecurity(String security) {
        this.security = security;
    }

    protected void setResolution(TimeFrame resolution) {
        this.resolution = resolution;
    }

    protected void setStart(LocalDate start) {
        this.start = start;
    }

    protected void setEnd(LocalDate end) {
        this.end = end;
    }

    protected void setModel(Model model) {
        this.model = model;
    }

    protected void setState(State state) {
        this.state = state;
    }

    protected void setType(Order type) {
        this.type = type;
    }

    protected void setVerbose(boolean print){
        this.print = print;
    }
    protected boolean buy(String id, int share, double price) {
        String rtn = order.buy(id, share, price);
        if (!rtn.isEmpty()) {
            if (print)
                System.out.println(rtn + " on the " + index + "th bar");
            return true;
        }
        return false;
    }

    protected boolean sell(String id, int share, double price) {
        String rtn = order.sell(id, share, price);
        if (!rtn.isEmpty()) {
            if (print)
                System.out.println(rtn + " on the " + index + "th bar");
            return true;
        }
        return false;
    }

    protected boolean buyClose(String id, int share, double price) {
        String rtn = order.buyClose(id, share, price);
        if (!rtn.isEmpty()) {
            if (print)
                System.out.println(rtn + " on the " + index + "th bar");
            return true;
        }
        return false;
    }

    protected boolean sellOpen(String id, int share, double price) {
        String rtn = order.sellOpen(id, share, price);
        if (!rtn.isEmpty()) {
            if (print)
                System.out.println(rtn + " on the " + index + "th bar");
            return true;
        }
        return false;
    }

    protected void conclude(){
        order.conclude();
    }

    private void configure() {
        switch (model) {
            case TEST:
                order = new MockOrder(name);
                break;
            case TRADE: {
                trader = new Trader(this, port);
                order = state.equals(MOCK) ? new MockOrder(name) : new RealOrder(name, tsAddress, tsPort);
                break;
            }
        }
    }

    private List<? extends BaseData> download() {
        List<? extends BaseData> data = new ArrayList<>();
        switch (resolution) {
            case TICK:
                data = ticks(security, start, end);
                data.remove(0);
                break;
            case MIN_1:
            case MIN_5:
                data = bars(security, resolution, start, end);
                break;
        }
        return data;
    }

    private void simulate() {
        System.out.println("Algorithm [" + name + "] start to run");
        if (model.equals(TEST)) {
            List<? extends BaseData> data = download();
            for (index = 0; index < data.size(); index++)
                onData(data.get(index), index);
        } else
            trader.run();
    }

    public void run() {
        init();
        configure();
        simulate();
        onComplete();
    }

    public String getName() {
        return name;
    }

    public double total(){
        return order.total();
    }

    public String getSecurity() {
        return security;
    }

    public TimeFrame getResolution() {
        return resolution;
    }

    public void onComplete() {
        order.conclude();
    }

    public abstract void init();

    public abstract void onData(BaseData data, int index);
}
