package tongtong.qiangqiang.mind.trade;

import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.TickInfo;
import cn.quanttech.quantera.datacenter.DataCenter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import tongtong.qiangqiang.mind.MockBase;

import java.util.List;
import java.util.Map;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/2/26.
 */
public class TraderHandler extends SimpleChannelInboundHandler<HttpObject>{

    public final MockBase algorithm;

    public BarInfo bar = null;

    public int index = 0;

    public TraderHandler(MockBase algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("tick coming");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("closed");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
            Map<String, List<String>> u = decoderQuery.parameters();
            TickInfo tick = new TickInfo(
                    get(u, "code"),
                    null,
                    null,
                    null,
                    Double.parseDouble(get(u,"last")),
                    Double.parseDouble(get(u,"ask")),
                    Double.parseDouble(get(u,"bid")),
                    Integer.parseInt(get(u,"volume")),
                    Double.parseDouble(get(u,"up")),
                    Double.parseDouble(get(u,"down"))
            );
            if (bar==null)
                bar =  new BarInfo(tick.secuCode, tick.exchangeID, null, tick.tradingTime, null);
            else {
                BarInfo newBar = DataCenter.updateBarByTick(tick, bar);
                if (newBar!=null){
                    algorithm.onData(bar, index++);
                    bar = newBar;
                }
            }
        }
    }

    String get(Map<String, List<String>> uri, String key){
        return uri.get(key).get(0);
    }
}
