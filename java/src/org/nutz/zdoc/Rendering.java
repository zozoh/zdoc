package org.nutz.zdoc;

import org.nutz.lang.util.NutMap;
import org.nutz.vfs.ZIO;

public class Rendering {

    public int charCount;

    public int limit;

    public String currentBasePath;

    public boolean isOutOfLimit() {
        if (limit <= 0)
            return false;
        return charCount > limit;
    }

    public boolean hasLimit() {
        return limit > 0;
    }

    /**
     * 读写接口
     */
    private ZIO io;

    /**
     * 模板工厂
     */
    private ZDocTemplateFactory tfa;

    /**
     * 当前正在工作的上下文
     */
    private NutMap context;

    public Rendering(ZIO io, ZDocTemplateFactory tfa) {
        this.io = io;
        this.tfa = tfa;
        this.context = new NutMap();
    }

    public ZIO io() {
        return io;
    }

    public ZDocTemplateFactory tfa() {
        return tfa;
    }

    public NutMap context() {
        return context;
    }

}
