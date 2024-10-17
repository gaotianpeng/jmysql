package com.gtp.jmysql.core;

import com.gtp.jmysql.dict.DictColumn;
import lombok.Data;

@Data
public class DField {
    private DictColumn dictColumn;
    private Object data;
}
