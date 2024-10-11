package com.gtp.jmysql.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictIndex {
    private int indexId;
    private int tableId;
    private String indexName;
    private List<DictField> dictFieldList = new ArrayList<>();
}
