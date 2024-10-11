package com.gtp.jmysql.dict;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileMode;
import lombok.Data;
import lombok.Getter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

@Data
public class SystemDict implements Serializable {
    private final static String SYSTEM_DICT_FILE = "ibdata";

    private SystemDict() {
    }

    @Getter
    private static SystemDict instance = new SystemDict();

    public HashMap<String, DictTable> nameTables = new HashMap<>(); // 表名映射DictTable
    public HashMap<Integer, DictTable> idTables = new HashMap<>(); // 表id映射DictTable
    public HashMap<Integer, DictTable> spaceIdTables = new HashMap<>(); // 表空间Id映射DictTable
    public HashMap<Integer, List<DictIndex>> tableIdIndexes = new HashMap<>(); // 表Id映射List<DictIndex>，表示一个表有哪些索引

    public int maxTableId;
    public int maxIndexId;
    public int maxSpaceId;

    public int addMaxTableId() {
        return ++maxTableId;
    }

    public int addMaxIndexId()  {
        return ++maxIndexId;
    }

    public int addMaxSpaceId() {
        return ++maxSpaceId;
    }

    public void serialize() {
        if (!FileUtil.exist(Paths.get(SYSTEM_DICT_FILE).toFile())) {
            FileUtil.createRandomAccessFile(Paths.get(SYSTEM_DICT_FILE), FileMode.rw);
        }

        try (ObjectOutputStream out =
                new ObjectOutputStream(FileUtil.getOutputStream(Paths.get(SYSTEM_DICT_FILE)))) {
            out.writeObject(this);
            out.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deserialize() {
        if (!FileUtil.exist(Paths.get(SYSTEM_DICT_FILE).toFile())) {
            FileUtil.createRandomAccessFile(Paths.get(SYSTEM_DICT_FILE), FileMode.rw);
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(FileUtil.getInputStream(Paths.get(SYSTEM_DICT_FILE)))) {
            instance = (SystemDict) in.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
