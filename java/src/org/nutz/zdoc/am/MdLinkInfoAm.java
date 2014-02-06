package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZLinkInfo;

public class MdLinkInfoAm extends ZDocAm {

    private String attName;

    public MdLinkInfoAm(String attName) {
        this.attName = attName;
    }

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        if ('(' == c) {
            return AmStatus.CONTINUE;
        }
        return AmStatus.DROP;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        if (')' == c)
            return AmStatus.DONE_BACK;
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        String str = as.buffer.toTrimmed();

        // 获取栈头对象
        ZDocEle o = as.peekObj();

        // 设置属性
        ZLinkInfo zi = new ZLinkInfo().parse(str);
        if (zi.hasTitle()) {
            o.title(zi.title());
        }
        if (zi.hasLink()) {
            o.attr(attName, zi.link());
        }

        // 清除
        as.buffer.clear();
    }

}
