package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.lang.Lang;
import org.nutz.zdoc.ZDocEle;

/**
 * 这个自动机为了容忍 link 文本和内容之间的可选择性的空格
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class MdLinkSepAm extends ZDocAm {

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        if (' ' == c) {
            return AmStatus.DONE;
        }

        if ('[' == c || '(' == c) {
            return AmStatus.DONE_BACK;
        }

        return AmStatus.DROP;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        // 它不能再进食了
        throw Lang.impossible();
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        // 啥都不用做
    }

}
