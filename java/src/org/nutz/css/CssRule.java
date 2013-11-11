package org.nutz.css;

import java.util.HashMap;

import org.nutz.lang.Strings;

public class CssRule extends HashMap<String, String> {

    public CssRule set(String name, String value) {
        put(name, value);
        return this;
    }

    public String get(String name, String dft) {
        return Strings.sNull(get(name), dft);
    }

}
