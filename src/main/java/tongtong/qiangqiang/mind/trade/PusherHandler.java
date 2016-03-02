package tongtong.qiangqiang.mind.trade;

import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.TickInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import tongtong.qiangqiang.func.GeneralUtilizer;
import tongtong.qiangqiang.mind.Algorithm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.time.LocalDate.now;
import static java.time.LocalDateTime.of;
import static java.time.LocalTime.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static tongtong.qiangqiang.func.GeneralUtilizer.combine;
import static tongtong.qiangqiang.func.GeneralUtilizer.sendString;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/2/26.
 */
public class PusherHandler extends SimpleChannelInboundHandler<HttpObject> {

    public static final DateTimeFormatter FMT_TICK = ofPattern("HH:mm:ss.S");

    public static final DateTimeFormatter FMT_PRINT = ofPattern("yyyy-MM-dd HH:mm");

    public final Map<String, List<Algorithm>> algorithms;

    public final Map<Algorithm, BarInfo> currentBar;

    public PusherHandler(Map<String, List<Algorithm>> algorithms, Map<Algorithm, BarInfo> currentBar) {
        this.algorithms = algorithms;
        this.currentBar = currentBar;
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
        sendString(ctx, "denied", BAD_REQUEST);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            Map<String, List<String>> para = new QueryStringDecoder(request.getUri()).parameters();
            TickInfo tick = new TickInfo(get(para, "code"), null, of(now(), parse(get(para, "time"), FMT_TICK)), null,
                    parseDouble(get(para, "last")), parseDouble(get(para, "ask")), parseDouble(get(para, "bid")),
                    parseInt(get(para, "volume")), parseDouble(get(para, "up")), parseDouble(get(para, "down"))
            );
            sendString(ctx, "accepted", OK);
            for (Algorithm algo : algorithms.get(tick.secuCode)) {
                BarInfo newBar = combine(currentBar.get(algo), tick, algo.getResolution());
                if (newBar != null) {
                    if (currentBar.get(algo) != null) {
                        System.out.println("\nBar Time = " + currentBar.get(algo).tradingTime.format(FMT_PRINT));
                        algo.onData(currentBar.get(algo));
                    }
                    currentBar.remove(algo);
                    currentBar.put(algo, newBar);
                }
            }
        }
    }

    String get(Map<String, List<String>> uri, String key) {
        return uri.get(key).get(0);
    }
}
