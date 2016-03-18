package tongtong.qiangqiang.mind.push;

import cn.quanttech.quantera.common.type.data.BarInfo;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import tongtong.qiangqiang.mind.Algorithm;

import java.util.List;
import java.util.Map;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/2/26.
 */
public class PusherInitializer extends ChannelInitializer<SocketChannel> {

    public final Map<String, List<Algorithm>> algorithms;

    public final Map<Algorithm, BarInfo> currentBar;

    public PusherInitializer(Map<String, List<Algorithm>> algorithms, Map<Algorithm, BarInfo> currentBar) {
        this.algorithms = algorithms;
        this.currentBar = currentBar;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpResponseEncoder());
        p.addLast(new PusherHandler(algorithms, currentBar));
    }
}
