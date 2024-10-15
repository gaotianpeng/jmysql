package com.gtp.jmysql.page;

import com.gtp.jmysql.core.ByteBufferUtil;

import static com.gtp.jmysql.core.ByteBufferUtil.mach_read_from_2;
import static com.gtp.jmysql.core.ByteBufferUtil.mach_write_to_2;

public class IndexPage extends Page{
    // Page 头相关，共36个字节
    public final static int PAGE_HEADER = 38; // PageHeader从Page中的第38个字节开始
    public final static int PAGE_N_DIR_SLOTS = 0; // 前2个字节用来示Slot的数量
    public final static int PAGE_HEAP_TOP = 2; // page中第几个字节开始空闲的，可以用来存记录
    public final static int PAGE_N_HEAP = 4; // 当前Page总共有多少条记录

    public final static int PAGE_LEVEL = 26; // 当前page是B+树的第几层，0为叶子节点层

    public final static int FSEG_HEADER_SIZE = 10;

    //  UserRecord相关，用来存行记录的，PAGE_DATA表示用来存记录的起始位
    public final static int PAGE_DATA = (PAGE_HEADER + 36 + 2*FSEG_HEADER_SIZE);
    public final static int PAGE_NEW_INFIMUM = (PAGE_DATA + 5);
    public final static int PAGE_NEW_SUPREMUM  = (PAGE_DATA + 13 + 5);

    // 最大最小记录各自占用13个字节，共26个字节
    static byte[] infimum_supremum_compact = {
            /* the infimum record */
            0x01/*n_owned=1*/,
            0x00, 0x02/* heap_no=0, REC_STATUS_INFIMUM */,
            0x00, PAGE_NEW_SUPREMUM/* pointer to supremum , 本来是0x0d相当地址，改成绝对地址PAGE_DATA+13*/,
            'i', 'n', 'f', 'i', 'm', 'u', 'm', 0,
            /* the supremum record */
            0x01/*n_owned=1*/,
            0x00, 0x0b/* heap_no=1, REC_STATUS_SUPREMUM */,
            0x00, 0x00/* end of record list */,
            's', 'u', 'p', 'r', 'e', 'm', 'u', 'm'
    };

    public void init_page_header() {
        // 用PAGE_HEAP_TOP的第二个字节存下一条记录插入的位置，26表示默认有两条记录了，最大和最小记录
        pageByteBuffer.array()[PAGE_HEADER + PAGE_HEAP_TOP + 1] = PAGE_DATA + 26;
        // 用PAGE_N_HEAP的第一个字节存0x80，表示是compact页，用紧凑型的方式来存记录
        pageByteBuffer.array()[PAGE_HEADER + PAGE_N_HEAP] = (byte)0x80;
        // 用PAGE_N_HEAP的第二个字节存页面中有多少条记录，默认为2，因为默认会有最大和最小两条记录
        pageByteBuffer.array()[PAGE_HEADER + PAGE_N_HEAP + 1] = 2;

        // 把最大和最小记录存到UserRecord区域
        ByteBufferUtil.memcpy(pageByteBuffer, PAGE_DATA, infimum_supremum_compact);
    }

    public int get_heap_top() {
        return page_header_get_field(PAGE_HEAP_TOP);
    }

    public void set_heap_top(int heap_top) {
        page_header_set_field(PAGE_HEAP_TOP, heap_top);
    }

    public int get_n_heap() {
        return page_header_get_field(PAGE_N_HEAP) & 0x7fff;
    }

    public void set_n_heap(int n_heap) {
        page_header_set_field(PAGE_N_HEAP, n_heap | (0x8000 & page_header_get_field(PAGE_N_HEAP)));
    }

    private void page_header_set_field(int field, int val) {
        mach_write_to_2(pageByteBuffer, PAGE_HEADER + field, val);
    }

    private int page_header_get_field(int field) {
        return (mach_read_from_2(pageByteBuffer, PAGE_HEADER + field));
    }
}
