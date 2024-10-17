package com.gtp.jmysql.core;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DTuple {
    private List<DField> fields = new ArrayList<>();
}
