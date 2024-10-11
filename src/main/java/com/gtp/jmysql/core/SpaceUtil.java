package com.gtp.jmysql.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileMode;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SpaceUtil {
    public static Path createUserTableSpace(String tableName) {
        Path path = Paths.get(tableName + ".ibd");
        FileUtil.createRandomAccessFile(path, FileMode.rw);
        return path;
    }
}
