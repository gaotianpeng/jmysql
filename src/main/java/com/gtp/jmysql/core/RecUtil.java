package com.gtp.jmysql.core;

import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.util.ByteUtil;
import com.gtp.jmysql.page.IndexPage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.sf.jsqlparser.statement.create.table.Index;

import java.nio.ByteBuffer;

import static com.gtp.jmysql.core.ByteBufferUtil.*;

public class RecUtil {
    // 记录头包括2个字节heap_no、1个字节record_type、2个字节next_record
    public final static int REC_HEADER_SIZE = 5;
    // 用2个字节来存前一条记录的绝对地址
    public final static int REC_NEXT = 2 ;

    /*
        获取某条记录的下一条记录，返回值为下一条记录的中间位置
         * @param indexPage 某页
         * @param rec 某条记录的中间位置
         * @return 下一条记录的中间位置
     */
    public static int rec_get_next_offs(IndexPage indexPage, int rec) {
        return mach_read_from_2(indexPage.getPageByteBuffer(), rec - REC_NEXT);
    }

    /*
        用DTuple和rec执行的记录进行比较，要么比较id字段，要么比较id字段的后一个字段
             * @param indexPage 某页
             * @param tuple 某数据组
             * @param rec 指针，执行某条记录的中间
             * @return 大小关系，1表示DTuple大于rec
     */
    public static int cmp_dtuple_rec_with_match_low(IndexPage indexPage, DTuple tuple, int rec) {
        DField field = tuple.getFields().get(0);
        if ("id".equals(field.getDictColumn().getName())) {
            int filedId = (int)field.getData();
            int recId = mach_read_from_4(indexPage.pageByteBuffer, rec);
            return CompareUtil.compare(filedId, recId);
        } else {
            String filedName = (String)field.getData();
            String recName = mach_read_from_size_str(indexPage.pageByteBuffer, rec + 4,
                    field.getDictColumn().getLen());
            return CompareUtil.compare(filedName, recName);
        }
    }

    /*
        计算新数据占多少个字节
     */
    public static int rec_get_converted_size(DTuple dTuple) {
        int size = 0;
        for (DField field: dTuple.getFields()) {
            size += field.getDictColumn().getLen();
        }

        return size + REC_HEADER_SIZE;
    }

    /*
        把dTuple转成记录ByteBuffer
     */
    public static ByteBuffer rec_convert_dtuple_to_rec(IndexPage indexPage, DTuple dTuple) {
        int recSize = RecUtil.rec_get_converted_size(dTuple);
        ByteBuffer rec = ByteBuffer.allocate(recSize);

        int position = indexPage.pageByteBuffer.position();
        int pageNHeap = indexPage.get_n_heap() - 1;
        indexPage.pageByteBuffer.position(position);
        ByteBufferUtil.mach_write_to_2(rec, 0, pageNHeap);

        // 0表示普通记录
        ByteBufferUtil.mach_write_to_1(rec, 2, 0);
        // next_record后面再设置，先占用2个字节
        ByteBufferUtil.mach_write_to_2(rec, 3, 0);

        rec.position(REC_HEADER_SIZE);
        for (DField field: dTuple.getFields() ) {
            if (field.getDictColumn().isInt()) {
                rec.put(ByteUtil.intToBytes((Integer) field.getData()));
            } else {
                rec.put(((String)field.getData()).getBytes());
            }
        }

        // 指向记录的起始位
        rec.clear();
        return rec;
    }
}
