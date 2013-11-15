package org.nutz.zdoc;

import org.nutz.vfs.ZFile;

public class ZDocHtmlCacheItem {

    private ZFile file;

    private String html;

    public ZFile file() {
        return file;
    }

    public ZDocHtmlCacheItem file(ZFile file) {
        this.file = file;
        return this;
    }

    public String html() {
        return html;
    }

    public ZDocHtmlCacheItem html(String html) {
        this.html = html;
        return this;
    }

}
