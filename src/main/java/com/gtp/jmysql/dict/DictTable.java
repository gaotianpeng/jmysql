package com.gtp.jmysql.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictTable {
    private int tableId;
    private int spaceId;
    private String tableName;
    private String path;
    private List<DictColumn> dictColumnList;
}
