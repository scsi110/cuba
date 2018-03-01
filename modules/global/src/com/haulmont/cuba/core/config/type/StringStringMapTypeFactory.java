package com.haulmont.cuba.core.config.type;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class StringStringMapTypeFactory extends TypeFactory {

    @Override
    public Object build(String string) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotEmpty(string)) {
            String[] elements = string.split(",");
            for (int i = 0; i < elements.length / 2; i++) {
                map.put(elements[i * 2].trim(), elements[i * 2 + 1].trim());
            }
        }
        return map;
    }
}
