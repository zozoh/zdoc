package org.nutz.zdoc.am;

import org.nutz.am.Am;
import org.nutz.am.AmFactory;
import org.nutz.am.AmStatus;
import org.nutz.lang.Lang;
import org.nutz.zdoc.ZDocBaseTest;
import org.nutz.zdoc.ZDocEle;

public abstract class AmTest extends ZDocBaseTest {

    protected AmFactory fa;

    protected String rootAmName;

    protected ZDocEle _parse(String str) {
        char[] cs = str.toCharArray();
        Am<ZDocEle> am = fa.getAm(ZDocParallelAm.class, rootAmName);
        ZDocAmStack stack = new ZDocAmStack(10);
        stack.pushObj(stack.bornObj());
        if (am.enter(stack, cs[0]) != AmStatus.CONTINUE) {
            throw Lang.impossible();
        }
        for (int i = 1; i < cs.length; i++) {
            char c = cs[i];
            AmStatus st = stack.eat(c);
            if (AmStatus.CONTINUE != st)
                throw Lang.impossible();
        }
        return stack.close();
    }

}
