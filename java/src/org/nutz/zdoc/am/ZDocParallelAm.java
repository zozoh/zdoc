package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.am.ParallelAm;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;

public class ZDocParallelAm extends ParallelAm<ZDocEle> {

    @Override
    protected AmStatus whenNoCandidate(AmStack<ZDocEle> as) {
        if (!as.raw.isEmpty()) {
            ZDocEle o = new ZDocEle().type(ZDocEleType.INLINE);
            String str = as.raw.toString();
            as.raw.clear();
            // 执行转义 ...
            o.text(str.replaceAll("(\\\\)(.)", "$2"));
            as.mergeHead(o);
        }
        return AmStatus.DONE;
    }
}
