package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.zdoc.ZDocEle;

/**
 * 本自动机一定在 link 的串联自动机里，并且在 enter 的时候，'[' 一定已经被吃掉了
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class MdLinkTextAm extends ZDocAm {

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        if (']' == c)
            return AmStatus.DONE;
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        String str = as.buffer.toTrimmed();

        // 获取栈头对象
        ZDocEle obj = as.peekObj();

        // 设定了 text
        obj.text(str);

        // 清除缓冲
        as.buffer.clear();
    }

}
