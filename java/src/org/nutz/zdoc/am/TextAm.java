package org.nutz.zdoc.am;

import static org.nutz.am.AmStatus.CONTINUE;
import static org.nutz.am.AmStatus.DONE_BACK;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.lang.Nums;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;

public class TextAm extends ZDocAm {

    private char[] stopcs;

    public TextAm(String stopcs) {
        this.stopcs = stopcs.toCharArray();
    }

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        if (Nums.isin(stopcs, c))
            return AmStatus.DROP;
        as.pushAm(this).pushObj(as.bornObj()).buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        // 如果 buffer 最后一个字符为转义字符 '\'
        if (as.buffer.last() == '\\') {
            as.buffer.push(c);
            return CONTINUE;
        }
        if (Nums.isin(stopcs, c))
            return DONE_BACK;
        as.buffer.push(c);
        return CONTINUE;
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        ZDocEle o = as.popObj().type(ZDocEleType.INLINE);
        String str = as.buffer.toString();
        // 执行转义 ...
        o.text(str.replaceAll("(\\\\)(.)", "$2"));
        as.mergeHead(o);
        as.buffer.clear();
        as.popAm();
    }

}
