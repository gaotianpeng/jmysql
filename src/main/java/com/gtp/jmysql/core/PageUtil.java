package com.gtp.jmysql.core;

import com.gtp.jmysql.page.Page;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PageUtil {
    public static final int PAGE_SIZE = 16 * 1024; // 16KB

    public static int createPage(int spaceId) {
        return 0;
    }

    public static int flushPages(Page page) {
        int spaceId = page.fil_page_get_space_id();
        int pageNo = page.fil_page_get_page_offset();
        Path path = SpaceUtil.getPathBySpaceId(spaceId);
        ByteBuffer byteBuffer = page.getPageByteBuffer();

        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            page.pageByteBuffer.clear();
            fileChannel.position((long)pageNo*PAGE_SIZE);
            return fileChannel.write(byteBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
