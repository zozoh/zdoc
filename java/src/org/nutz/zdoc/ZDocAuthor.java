package org.nutz.zdoc;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class ZDocAuthor {

    /**
     * @param str
     *            格式诸如 'zozoh(zozohtnt@gmail.com)'
     */
    public ZDocAuthor(String str) {
        int pos = str.indexOf('(');
        if (pos > 0) {
            name = Strings.trim(str.substring(0, pos));
            email = Strings.trim(str.substring(pos + 1, str.length() - 1));
        } else {
            name = Strings.trim(str);
            email = null;
        }
    }

    public ZDocAuthor() {}

    private String name;

    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String toString() {
        return String.format("%s(%s)", name, email);
    }

    public NutMap toMap() {
        NutMap map = new NutMap();
        map.setv("name", Strings.sBlank(name, "anonymity"));
        if (!Strings.isBlank(email)) {
            map.setv("email", email);
        }
        return map;
    }
}
