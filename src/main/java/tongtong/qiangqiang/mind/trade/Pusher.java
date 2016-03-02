package tongtong.qiangqiang.mind.trade;

import cn.quanttech.quantera.common.data.BarInfo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import tongtong.qiangqiang.mind.Algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.buffer.PooledByteBufAllocator.DEFAULT;
import static io.netty.channel.ChannelOption.*;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class Pusher {

    private final Map<String, List<Algorithm>> algorithms;

    public final Map<Algorithm, BarInfo> currentBar;

    private final int port;

    public Pusher(int port) {
        this.algorithms = new HashMap<>();
        this.currentBar = new HashMap<>();
        this.port = port;
    }

    public synchronized void register(Algorithm algorithm) {
        currentBar.put(algorithm, null);
        if (algorithms.containsKey(algorithm.getSecurity()))
            algorithms.get(algorithm.getSecurity()).add(algorithm);
        else {
            List<Algorithm> lst = new ArrayList<>();
            lst.add(algorithm);
            algorithms.put(algorithm.getSecurity(), lst);
        }
    }

    public int getPort() {
        return port;
    }

    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NioEventLoopGroup boss = new NioEventLoopGroup(1);
                NioEventLoopGroup worker = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(boss, worker);
                    b.channel(NioServerSocketChannel.class);
                    b.option(SO_KEEPALIVE, false);
                    b.childHandler(new PusherInitializer(algorithms, currentBar));
                    b.childOption(ALLOCATOR, DEFAULT);

                    Channel ch = b.bind(8080).sync().channel();
                    System.out.println("\ntrader is now ready to accept connections on port 8080 \n");

                    ch.closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    boss.shutdownGracefully();
                    worker.shutdownGracefully();
                }
            }
        }).start();
    }
}
