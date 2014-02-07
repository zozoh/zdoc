package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.lang.Lang;
import org.nutz.zdoc.ZDocEle;

/**
 * 仅仅会消费一个特殊字符的自动机，仅会被用到串行自动机内
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class OneCharAm extends ZDocAm {

    private char expectC;

    public OneCharAm(char expectC) {
        this.expectC = expectC;
    }

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        if (this.expectC == c)
            return AmStatus.DONE;
        return AmStatus.DROP;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        throw Lang.impossible();
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        // 啥也不用做
    }

}
