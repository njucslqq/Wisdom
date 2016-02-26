package tongtong.qiangqiang.mind;

import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import tongtong.qiangqiang.mind.order.MockOrder;
import tongtong.qiangqiang.mind.order.Order;
import tongtong.qiangqiang.mind.order.RealOrder;
import tongtong.qiangqiang.mind.trade.Trader;
import tongtong.qiangqiang.vis.TimeSeriesChart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cn.quanttech.quantera.common.data.TimeFrame.TICK;
import static tongtong.qiangqiang.data.Historical.bars;
import static tongtong.qiangqiang.data.Historical.ticks;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public abstract class MockBase {

    public enum Model{
        TEST, TRADE
    }

    public enum State{
        REAL, MOCK
    }

    public enum OrderType{
        MARKET, LIMIT
    }

    private String name;

    private String security;

    private TimeFrame resolution;

    private LocalDate start, end;

    private Model model = Model.TEST;

    private State state = State.MOCK;

    private OrderType type = OrderType.MARKET;

    private Trader trader;

    protected Order order;

    public MockBase(String name){
        this.name = name;
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

    protected void setModel(Model model){
        this.model = model;
    }

    protected void setState(State state){
        this.state = state;
    }

    protected void setType(OrderType type){
        this.type = type;
    }

    public void simulate() {
        if (model == Model.TEST)
            order = new MockOrder(name);
        else {
            trader = new Trader(this);
            if (state == State.MOCK)
                order = new MockOrder(name);
            else
                order = new RealOrder();
        }

        if (model == Model.TEST) {
            List<? extends BaseData> data = null;
            switch (resolution){
                case TICK:{
                    data = ticks(security, start, end);
                    data.remove(0);
                    break;
                }
                case MIN_1:case MIN_5: {
                    data = bars(security, resolution, start, end);
                    break;
                }
            }
            for (int i = 0; i < data.size(); i++) {
                onData(data.get(i), i);
            }
        }
        else {
            trader = new Trader(this);
            trader.run();
        }
    }

    public void onComplete(){
        order.conclude();
    }

    public abstract void init();

    public abstract void onData(BaseData data, int index);
}
