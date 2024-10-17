package com.gtp.jmysql.core;

import com.gtp.jmysql.dict.SystemDict;
import com.gtp.jmysql.page.IndexPage;
import com.gtp.jmysql.page.Page;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.gtp.jmysql.core.RecUtil.REC_HEADER_SIZE;
import static com.gtp.jmysql.core.RecUtil.cmp_dtuple_rec_with_match_low;
import static com.gtp.jmysql.page.IndexPage.PAGE_NEW_INFIMUM;
import static com.gtp.jmysql.page.IndexPage.PAGE_NEW_SUPREMUM;

public class PageUtil {
    public static final int PAGE_SIZE = 16 * 1024; // 16KB

    public static IndexPage readPage(int spaceId, int pageNo) {
        try (FileChannel fileChannel = FileChannel.open(SpaceUtil.getPathBySpaceId(spaceId),
                StandardOpenOption.READ)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(PAGE_SIZE);
            fileChannel.position((long)pageNo * PAGE_SIZE);
            fileChannel.read(byteBuffer);

            IndexPage page = new IndexPage();
            page.setPageByteBuffer(byteBuffer);
            return page;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

    public static int insert_row(IndexPage indexPage, DTuple dTuple) {
        int rec = PageUtil.page_cur_search_with_match_bytes(indexPage, dTuple);
        return PageUtil.btr_cur_optimistic_insert(indexPage, dTuple, rec);
    }

    /*
        在indexPage中查找等于tuple或小于tuple的最后一条记录
            - 如果某条记录等于tuple，那么返回这条记录
            - 如果某条记录大于tuple，则返回这条记录的前一条记录
     */
    public static int page_cur_search_with_match_bytes(IndexPage indexPage, DTuple tuple) {
        int currentRec = PAGE_NEW_INFIMUM;
        int nextRec = RecUtil.rec_get_next_offs(indexPage, currentRec);
        while (nextRec != PAGE_NEW_SUPREMUM) {
            int cmp = cmp_dtuple_rec_with_match_low(indexPage, tuple, nextRec);
            // 如果tuple为插入记录，则比较下一条记录是否比tuple大，如果大，则返回当前记录，tuple将插在当前记录的后面
            // 如果tuple为查询记录，则比较下一条记录记录是否等于tuple，如果等于，则返回下一条记录

            // cmp < 0, 表示nextRec > tuple
            if (cmp < 0) {
                // 插入的时候走这个分支
                return currentRec;
            } else if (cmp == 0) {
                // 遇到id相等的了，插入的时候应该要报错，但是select、update、delete的时候却正好是需要的
                return nextRec;
            } else {
                // 取下一条
                currentRec = nextRec;
                nextRec = RecUtil.rec_get_next_offs(indexPage, nextRec);
            }
        }
        return currentRec;
    }

    /*
     * @param dTuple     需要插入的新数据
     * @param indexPage  新数据需要插入的页
     * @param currentRec 新数据需要插入在currentRec之后
     * @return 返回新数据在页中的地址，地址为记录的中间位置
     */
    public static int btr_cur_optimistic_insert(IndexPage indexPage, DTuple dTuple, int currentRec) {
        // 计算插入数据占多少字节
        int recSize = RecUtil.rec_get_converted_size(dTuple);

        // 给该记录分配页内空闲空间，并且会将position定位在空闲的起始位置
        page_mem_alloc_heap(indexPage, recSize);

        // 先保存新记录的中间记录，再插入数据，因为插入数据会修改position
        int insertRec = indexPage.pageByteBuffer.position() + REC_HEADER_SIZE;

        // 把插入数据转成字节数组
        // 注意一行记录不止有记录体，还有记录头
        // 如果一个表有变长字段或NULL值字段，则会用单独的区域保存变长字段长度和NULL值字段是否为NULL
        // 记录头包括2个字节heap_no、1个字节record_type、2个字节next_record
        ByteBuffer rec = RecUtil.rec_convert_dtuple_to_rec(indexPage, dTuple);

        // 插入新记录
        indexPage.pageByteBuffer.put(rec);

        // 修改指针
        // insertRec是记录的中间位置，nextRec和insertRec也是记录的中间位置
        // 也就是存在next_offset处的都是记录的中间位置
        int nextRec = RecUtil.rec_get_next_offs(indexPage, currentRec);
        indexPage.rec_set_next_offs_new(insertRec, nextRec);
        indexPage.rec_set_next_offs_new(currentRec, insertRec);

        flushPages(indexPage);
        return insertRec;
    }

    /**
     * 从页中分配需要的空间，并定位在空闲空间的起始位
     *
     * @param indexPage 指定页
     * @param need      需要分配的字节数
     * @return 当前页
     */
    public static IndexPage page_mem_alloc_heap(IndexPage indexPage, int need) {
        // 正常需要判断可用空间是否足够

        // 先获取当前PAGE_HEAP_TOP的值，然后增加need个字节，然后修改PAGE_HEAP_TOP的值
        int pageHeapTop = indexPage.get_heap_top();
        int newPageHeapTop = pageHeapTop + need;
        indexPage.set_heap_top(newPageHeapTop);

        // 获取新增行对应的heapNo，然后对heapNo进行+1，然后修改PAGE_N_HEAP
        int pageNHeap = indexPage.get_n_heap();
        int newPaeNHeap = pageHeapTop + 1;
        indexPage.set_n_heap(newPaeNHeap);

        // 定位在数据插入的起始伴
        indexPage.pageByteBuffer.position(pageHeapTop);
        return indexPage;
    }

    public static void main(String[] args) {
        SystemDict.getInstance().deserialize();;
        createPage(1);
    }
}
