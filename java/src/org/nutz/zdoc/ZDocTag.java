package org.nutz.zdoc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;

public class ZDocTag {

    private String key;

    private String text;

    private int count;

    private List<ZDocIndex> zDocIndexes;

    private List<NutMap> items;

    public ZDocTag() {
        zDocIndexes = new LinkedList<ZDocIndex>();
    }

    public ZDocTag(String text) {
        this();
        this.text = text;
        this.genKey();
    }

    public String toString() {
        return key + ":" + text;
    }

    public String getKey() {
        return key;
    }

    public ZDocTag setKey(String key) {
        this.key = key;
        return this;
    }

    public ZDocTag genKey() {
        if (null != text) {
            key = Lang.md5(text);
        }
        return this;
    }

    public String getText() {
        return text;
    }

    public ZDocTag setText(String text) {
        this.text = text;
        return this;
    }

    public ZDocTag increaseCount() {
        count++;
        return this;
    }

    public int getCount() {
        return count;
    }

    public ZDocTag setCount(int count) {
        this.count = count;
        return this;
    }

    public List<ZDocIndex> getzDocIndexes() {
        return zDocIndexes;
    }

    public ZDocTag addzDocIndex(ZDocIndex zi) {
        this.zDocIndexes.add(zi);
        return this;
    }

    public List<NutMap> getItems() {
        return items;
    }

    public ZDocTag genItems() {
        if (null == items) {
            items = new ArrayList<NutMap>(zDocIndexes.size());
            for (ZDocIndex zi : zDocIndexes)
                items.add(zi.toMap());
        }
        return this;
    }

}
