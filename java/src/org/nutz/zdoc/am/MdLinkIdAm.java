package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.lang.Strings;
import org.nutz.zdoc.ZDocEle;

public class MdLinkIdAm extends ZDocAm {

    private String attName;

    public MdLinkIdAm(String attName) {
        this.attName = attName;
    }

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        if ('[' == c) {
            return AmStatus.CONTINUE;
        }
        return AmStatus.DROP;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        if (']' == c)
            return AmStatus.DONE_BACK;
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        String str = as.buffer.toTrimmed();

        // 获取栈头对象
        ZDocEle o = as.peekObj();

        // 设定了对应链接属性的 ID
        if (Strings.isEmpty(str)) {
            o.attr(attName, "$" + o.text().trim().toLowerCase());
        } else {
            o.attr(attName, "$" + str.toLowerCase());
        }

        // 清除缓冲
        as.buffer.clear();
    }

}
