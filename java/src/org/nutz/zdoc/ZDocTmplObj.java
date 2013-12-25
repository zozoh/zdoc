package org.nutz.zdoc;

import org.nutz.vfs.ZFile;

public class ZDocTmplObj {

    private ZFile file;

    private String rawText;

    public ZFile file() {
        return file;
    }

    public ZDocTmplObj file(ZFile file) {
        this.file = file;
        return this;
    }

    public String raw() {
        return rawText;
    }

    public ZDocTmplObj raw(String str) {
        this.rawText = str;
        return this;
    }

}
