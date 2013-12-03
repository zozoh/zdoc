package org.nutz.zdoc;

import org.nutz.vfs.ZDir;
import org.nutz.vfs.ZIO;

/**
 * 这个接口的实例是初始化一次就抛弃的
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class RenderTo extends ZDocHome {

    public RenderTo(ZIO io) {
        super(io);
    }

    /**
     * 渲染整个 ZDoc 集合
     * 
     * @param io
     *            输出用的 IO
     */
    public abstract void render(ZIO io);

    protected ZDir dest;

    public ZDir dest() {
        return dest;
    }

    public RenderTo dest(ZDir dest) {
        this.dest = dest;
        return this;
    }

}
