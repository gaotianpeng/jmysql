package com.gtp.jmysql.core;

import com.gtp.jmysql.dict.SystemDict;
import com.gtp.jmysql.page.IndexPage;
import com.gtp.jmysql.page.Page;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PageUtil {
    public static final int PAGE_SIZE = 16 * 1024; // 16KB

    public static int createPage(int spaceId) {
        int pageNo = SpaceUtil.getNextPageNo(spaceId);

        ByteBuffer byteBuffer = ByteBuffer.allocate(PAGE_SIZE);
        IndexPage page = new IndexPage();
        page.setPageByteBuffer(byteBuffer);
        page.fil_page_set_space_id(spaceId);
        page.fil_page_set_page_offset(pageNo);
        page.fil_page_set_type(17855); // 源码中17855表示INDEX页
        page.init_file_header(spaceId, pageNo);
        page.init_page_header();

        return flushPages(page);
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

    public static void main(String[] args) {
        SystemDict.getInstance().deserialize();;
        createPage(0);
    }
}
