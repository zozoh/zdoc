package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;

public class ZDocQuoteAm extends ZDocAm {

    private char theChar;

    public ZDocQuoteAm(char theChar) {
        this.theChar = theChar;
    }

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        if (theChar == c) {
            as.pushAm(this).pushObj(as.bornObj());
            return AmStatus.CONTINUE;
        }
        return AmStatus.DROP;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        if (theChar == c)
            return AmStatus.DONE;
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        ZDocEle o = as.popObj().type(ZDocEleType.QUOTE);
        o.text(as.buffer.toString());
        as.mergeHead(o);
        as.buffer.clear();
        as.popAm();
    }
}