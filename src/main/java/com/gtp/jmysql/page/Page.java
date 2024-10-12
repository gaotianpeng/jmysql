package com.gtp.jmysql.page;


import lombok.Data;

import java.nio.ByteBuffer;

import static com.gtp.jmysql.core.ByteBufferUtil.*;

@Data
public class Page {
    public final static int FIL_PAGE_SPACE_OR_CHKSUM = 0;
    public final static int FIL_PAGE_OFFSET = 4; // 当前页的页号，从第4个字节开始
    public final static int FIL_PAGE_PRIV = 8; // 上一页的页号，占4个字节
    public final static int FIL_PAGE_NEXT = 12; // 下一页的页号，占4个字节
    public final static int FIL_PAGE_LSN = 16;
    public final static int FIL_PAGE_TYPE = 24; // 页的类型，从24字节开始，占2字节

    public final static int FIL_PAGE_FILE_FLUSH_LSN = 26;

    public final static int FIL_PAGE_ARCH_LOG_NO_OR_SPACE_ID = 34; // 属于哪个表空间

    public ByteBuffer pageByteBuffer;

    public void init_file_header(int spaceId, int pageNo) {
        fil_page_set_space_id(spaceId);
        fil_page_set_page_offset(pageNo);
    }

    public void fil_page_set_page_offset(int pageNo) {
        mach_write_to_4(pageByteBuffer, FIL_PAGE_OFFSET, pageNo);
    }

    public int fil_page_get_page_offset() {
        return mach_read_from_4(pageByteBuffer, FIL_PAGE_OFFSET);
    }

    public void fil_page_set_space_id(int spaceId) {
        mach_write_to_4(pageByteBuffer, FIL_PAGE_ARCH_LOG_NO_OR_SPACE_ID, spaceId);
    }

    public int fil_page_get_space_id() {
        return mach_read_from_4(pageByteBuffer, FIL_PAGE_ARCH_LOG_NO_OR_SPACE_ID);
    }

    public void fil_page_set_type(int type) {
        mach_write_to_2(pageByteBuffer, FIL_PAGE_TYPE, type);
    }

    public int fil_page_get_type() {
        return mach_read_from_2(pageByteBuffer, FIL_PAGE_TYPE);
    }

}
