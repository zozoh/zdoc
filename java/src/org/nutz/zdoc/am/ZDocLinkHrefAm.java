package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.lang.Strings;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;

public class ZDocLinkHrefAm extends ZDocAm {

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        if (']' == c) {
            return AmStatus.DONE_BACK;
        }
        if (Character.isWhitespace(c)) {
            return as.buffer.isEmpty() ? AmStatus.CONTINUE : AmStatus.DONE;
        }
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        String str = as.buffer.toTrimmed();
        ZDocEle obj = as.peekObj();
        if (str.startsWith("^")) {
            obj.attr("target", "_blank");
            str = Strings.trim(str.substring(1));
        }
        obj.type(ZDocEleType.INLINE).href(str);
        as.buffer.clear();
    }

}
