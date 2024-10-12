package com.gtp.jmysql.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileMode;

import java.io.IOException;
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
}
