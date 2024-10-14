package com.gtp.jmysql;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MysqlClient {
    private final String host;
    private final int port;

    public MysqlClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void sendMessage(String message) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object result) throws Exception {
                                    System.out.println(result);
                                }
                            });
                        }
                    });

            Channel channel = bootstrap.connect().sync().channel();
            ChannelFuture future = channel.writeAndFlush(message + "\r\n");
            future.sync(); // 等待写操作完成

            // 等待服务器的响应
            ChannelFuture closeFuture = channel.closeFuture();
            closeFuture.sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MysqlClient client = new MysqlClient("localhost", 18080);

        String createSql = """
                CREATE TABLE t1 (
                  id int,
                  a varchar(10),
                  b varchar(10),
                  c varchar(10),
                  d int,
                  PRIMARY KEY (id)
                );
                """;
        client.sendMessage(createSql);
    }
}
