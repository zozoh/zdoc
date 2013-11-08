package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.lang.Strings;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;

public class ZDocImgAltAm extends ZDocAm {

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        if (c == '>')
            return AmStatus.DONE;
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        if (as.buffer.last() == '>')
            return AmStatus.DONE_BACK;
        if (c == '>')
            return AmStatus.DONE;

        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        String str = as.buffer.toTrimmed();

        ZDocEle obj = as.peekObj();
        if (!Strings.isBlank(str)) {
            obj.text(str);
        }
        obj.type(ZDocEleType.IMG);
        as.buffer.clear();
    }

}
