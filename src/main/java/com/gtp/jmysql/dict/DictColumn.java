package com.gtp.jmysql.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictColumn implements Serializable {
    private int tableId;
    private String name;
    private String type;
    private int colNo;
    private int len;
}
