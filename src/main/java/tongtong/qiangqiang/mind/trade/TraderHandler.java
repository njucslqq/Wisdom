package tongtong.qiangqiang.mind.trade;

import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.TickInfo;
import cn.quanttech.quantera.datacenter.DataCenter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import tongtong.qiangqiang.func.GeneralUtilizer;
import tongtong.qiangqiang.mind.Algorithm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/2/26.
 */
public class TraderHandler extends SimpleChannelInboundHandler<HttpObject> {

    public final Algorithm algorithm;

    public static BarInfo bar = null;

    public int index = 0;

    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss.S");

    public TraderHandler(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //System.out.println("tick coming");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //System.out.println("closed");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        GeneralUtilizer.sendString(ctx, "denied", BAD_REQUEST);
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
                    LocalDateTime.of(LocalDate.now(), LocalTime.parse(get(u, "time"), FMT)),
                    null,
                    Double.parseDouble(get(u, "last")),
                    Double.parseDouble(get(u, "ask")),
                    Double.parseDouble(get(u, "bid")),
                    Integer.parseInt(get(u, "volume")),
                    Double.parseDouble(get(u, "up")),
                    Double.parseDouble(get(u, "down"))
            );
            if (algorithm.getSecurity().equals(tick.secuCode)) {
                //System.out.println(tick.secuCode + " : last = " + tick.lastPrice + ", volume = " + tick.volume);
                BarInfo newBar = GeneralUtilizer.combine(bar, tick, algorithm.getResolution());
                if (newBar != null) {
                    if (bar != null) {
                        System.out.println("======> Generate a New Bar <======");
                        System.out.println("   Time = " + bar.tradingTime);
                        System.out.println("================><================");
                        algorithm.onData(bar, index++);
                    }
                    bar = newBar;
                }
            }
            GeneralUtilizer.sendString(ctx, "accepted", OK);
        }
    }

    String get(Map<String, List<String>> uri, String key) {
        return uri.get(key).get(0);
    }
}
