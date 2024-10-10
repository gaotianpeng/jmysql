package com.gtp.jmysql;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

// 连接器: 负责与客户端建立网络连接，并接收网络请求
public class ConnectionHandlerPerThread extends ChannelInboundHandlerAdapter{

    private SqlParser sqlParser = new SqlParser();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        String sql = in.toString(CharsetUtil.UTF_8);
        System.out.println("接收到sql: " + sql);

        // 调用解析器解析sql
        // 调用优化器优化sql
        // 调用执行器执行sql,得到最终结果
        String result = sqlParser.mysql_parse(sql);
        ByteBuf responseBuffer = Unpooled.copiedBuffer(result, CharsetUtil.UTF_8);
        ctx.write(responseBuffer);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
