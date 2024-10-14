package com.gtp.jmysql.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileMode;
import com.gtp.jmysql.dict.DictTable;
import com.gtp.jmysql.dict.SystemDict;
import com.gtp.jmysql.page.FspHdrPage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.gtp.jmysql.core.PageUtil.PAGE_SIZE;

public class SpaceUtil {
    public static Path createUserTableSpace(String tableName) {
        Path path = Paths.get(tableName + ".ibd");
        if (!Files.exists(path)) {
            byte[] bytes = new byte[1* PAGE_SIZE];
            try {
                Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return path;
    }

    public static FspHdrPage getFspHdrPage(int spaceId) {
        Path path = getPathBySpaceId(spaceId);
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(PAGE_SIZE);
            fileChannel.position(0);
            fileChannel.read(byteBuffer);

            FspHdrPage page = new FspHdrPage();
            page.setPageByteBuffer(byteBuffer);
            return page;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getPathBySpaceId(int spaceId) {
        DictTable dictTable = SystemDict.getInstance().getSpaceIdTables().get(spaceId);
        return Paths.get(dictTable.getPath());
    }
}
