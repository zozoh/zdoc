package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.lang.Lang;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;

public class MdImgDoneAm extends ZDocAm {

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        if (']' == c || ')' == c) {
            return AmStatus.DONE;
        }
        throw Lang.impossible();
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        throw Lang.impossible();
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        ZDocEle o = as.popObj();
        o.type(ZDocEleType.IMG);

        as.mergeHead(o);
    }
}
