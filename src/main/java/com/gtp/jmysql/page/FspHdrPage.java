package com.gtp.jmysql.page;

import com.gtp.jmysql.core.ByteBufferUtil;

public class FspHdrPage extends Page {
    /* FSP_HEADER从Page中的第38个字节开始，表示文件头占38个字节，
        FSP_HEADER实际占112个字节 */
    public final static int FSP_HEADER = 38;

    public final static int FSP_SPACE_ID = 0;
    public final static int FSP_NOT_USED = 4;
    public final static int FSP_SIZE = 8; // 当前表空间占用的页面数，占4个字节

    public int get_fsp_size() {
        return ByteBufferUtil.mach_read_from_4(pageByteBuffer, FSP_HEADER + FSP_SIZE);
    }

    public void set_fsp_size(int size) {
        ByteBufferUtil.mach_write_to_4(pageByteBuffer, FSP_HEADER + FSP_SIZE, size);
    }
}
