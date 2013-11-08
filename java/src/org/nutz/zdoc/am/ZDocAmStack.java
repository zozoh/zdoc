package org.nutz.zdoc.am;

import org.nutz.am.Am;
import org.nutz.am.AmStack;
import org.nutz.zdoc.ZDocEle;

public class ZDocAmStack extends AmStack<ZDocEle> {

    @SuppressWarnings("unchecked")
    public ZDocAmStack(int maxDepth) {
        super(maxDepth);
        this.objs = new ZDocEle[maxDepth];
        this.ams = new Am[maxDepth];
    }

    @Override
    protected void merge(ZDocEle a, ZDocEle b) {
        b.parent(a);
    }

    @Override
    public AmStack<ZDocEle> born() {
        return new ZDocAmStack(maxDepth);
    }

    @Override
    public ZDocEle bornObj() {
        return new ZDocEle();
    }

    @Override
    protected String objBrief(ZDocEle o) {
        return o.toBrief();
    }

}
