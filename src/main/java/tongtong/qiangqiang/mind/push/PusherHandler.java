package tongtong.qiangqiang.mind.push;

import cn.quanttech.quantera.common.type.data.BarInfo;
import cn.quanttech.quantera.common.type.data.TickInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import tongtong.qiangqiang.mind.Algorithm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.of;
import static java.time.LocalTime.now;
import static java.time.LocalTime.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static tongtong.qiangqiang.func.GeneralUtilizer.combine;
import static tongtong.qiangqiang.func.GeneralUtilizer.sendString;
import static tongtong.qiangqiang.mind.Algorithm.*;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/2/26.
 */
public class PusherHandler extends SimpleChannelInboundHandler<HttpObject> {

    public static final DateTimeFormatter FMT_TICK = ofPattern("HH:mm:ss.S");

    public static final DateTimeFormatter FMT_PRINT = ofPattern("yyyy-MM-dif_dif HH:mm");

    public final Map<String, List<Algorithm>> algorithms;

    public final Map<Algorithm, BarInfo> currentBar;

    public PusherHandler(Map<String, List<Algorithm>> algorithms, Map<Algorithm, BarInfo> currentBar) {
        this.algorithms = algorithms;
        this.currentBar = currentBar;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("[Recieve Tick, Processing]");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("[Finished, Close Connection]\n");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        sendString(ctx, "======> Catch Exception in Handler, Abort", OK);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            Map<String, List<String>> para = new QueryStringDecoder(request.getUri()).parameters();
            if (isTradingTimeAll()) {
                TickInfo tick = parseTick(para);
                if (tick != null) {
                    if (algorithms.containsKey(tick.secuCode)) {
                        algorithms.get(tick.secuCode).parallelStream().forEach(a -> {
                            a.getPanel().writer.append(".");
                            BarInfo newBar = combine(currentBar.get(a), tick, a.getResolution());
                            if (newBar != null) {
                                if (currentBar.get(a) != null) {
                                    a.getPanel().writer.append("\nBar Time = " + currentBar.get(a).tradingTime.format(FMT_PRINT) + "\n");
                                    a.onData(currentBar.get(a));
                                }
                                currentBar.remove(a);
                                currentBar.put(a, newBar);
                            }
                        });
                    }
                }
            }
        }
        sendString(ctx, "accepted", OK);
    }

    private String get(Map<String, List<String>> uri, String key) {
        return uri.get(key).get(0);
    }

    private boolean isTradingTimeDay() {
        return (!now().isBefore(AM_START) && !now().isAfter(AM_END)) || (!now().isBefore(PM_START) && !now().isAfter(PM_END));
    }

    private boolean isTradingTimeAll() {
        return !((now().isAfter(NIGHT_END) && now().isBefore(AM_START)) ||
                (now().isAfter(AM_END) && now().isBefore(PM_START)) ||
                (now().isAfter(PM_END) && now().isBefore(NIGHT_START)));
    }

    private TickInfo parseTick(Map<String, List<String>> para) {
        try {
            return new TickInfo(get(para, "code"), null, of(LocalDate.now(), parse(get(para, "time"), FMT_TICK)), null,
                    parseDouble(get(para, "last")), parseDouble(get(para, "ask")), parseDouble(get(para, "bid")),
                    parseInt(get(para, "volume")), parseDouble(get(para, "up")), parseDouble(get(para, "down"))
            );
        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }
}
