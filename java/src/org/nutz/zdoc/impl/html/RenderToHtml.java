package org.nutz.zdoc.impl.html;

import org.nutz.vfs.ZDir;
import org.nutz.vfs.ZIO;
import org.nutz.zdoc.RenderTo;

public class RenderToHtml extends RenderTo {

    @Override
    public void render(ZIO io) {}

    public RenderToHtml(ZIO io) {
        super(io);
    }

    private ZDir dest;

    public ZDir dest() {
        return dest;
    }

    public RenderToHtml dest(ZDir dest) {
        this.dest = dest;
        return this;
    }
}