package org.nutz.zdoc;

import java.util.regex.Pattern;

public class ZDocRule {

    private Pattern regex;

    private String key;

    public Pattern regex() {
        return regex;
    }

    public ZDocRule regex(Pattern regex) {
        this.regex = regex;
        return this;
    }

    public ZDocRule regex(String regex) {
        this.regex = Pattern.compile(regex);
        return this;
    }

    public boolean match(String rph) {
        return regex.matcher(rph).find();
    }

    public String key() {
        return key;
    }

    public ZDocRule key(String key) {
        this.key = key;
        return this;
    }

}
