package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.am.ParallelAm;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;

public class ZDocParallelAm extends ParallelAm<ZDocEle> {

    @Override
    protected AmStatus whenNoCandidate(AmStack<ZDocEle> as) {
        ZDocEle o = new ZDocEle().type(ZDocEleType.INLINE);
        o.text(as.raw.toString());
        as.mergeHead(o);
        as.raw.clear();

        return AmStatus.DONE;
    }
}
