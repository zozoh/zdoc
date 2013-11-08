package org.nutz.am;

import org.nutz.lang.Strings;

public abstract class AbstractAm<T> implements Am<T> {

    private String name;

    @Override
    public Am<T> name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String name() {
        return Strings.sNull(name, "@" + getClass().getSimpleName());
    }
}
