package com.gtp.jmysql.core;

import com.gtp.jmysql.page.Page;

import java.nio.file.Path;

public class PageUtil {
    public static final int PAGE_SIZE = 16 * 1024; // 16KB

    public static int createPage(int spaceId) {
        return 0;
    }

    public static int flushPages(Page page) {
        int spaceId = page.fil_page_get_space_id();
        int pageNo = page.fil_page_get_page_offset();

        return 0;
    }
}
