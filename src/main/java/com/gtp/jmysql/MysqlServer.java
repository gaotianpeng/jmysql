package com.gtp.jmysql;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;


public class MysqlServer {
    private final int port;

    public MysqlServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        /*
            设置了两个事件循环组：
                bossGroup 用于接受连接
                workerGroup 用于处理已经接受的连接
         */
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ConnectionHandlerPerThread());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            System.out.println("Server is listening on port " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 18080;
        new MysqlServer(port).run();
    }
}
