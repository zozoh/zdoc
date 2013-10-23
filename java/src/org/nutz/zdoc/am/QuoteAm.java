package org.nutz.zdoc.am;

import org.nutz.zdoc.Am;
import org.nutz.zdoc.AmResult;
import org.nutz.zdoc.EachChar;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZDocChars;
import org.nutz.zdoc.ZDocEleType;

public class QuoteAm implements Am {

    private char qc;

    public QuoteAm(char quoteBy) {
        this.qc = quoteBy;
    }

    @Override
    public boolean test(Parsing ing, ZDocChars cs) {
        return cs.startsIs(qc);
    }

    @Override
    public AmResult run(Parsing ing, ZDocChars cs) {
        int n = cs.each(new EachChar() {
            public boolean isBreak(int index, char c) {
                return qc == c;
            }
        });
        // 设置文字对象的内容
        ing.ele().type(ZDocEleType.INLINE).quote(qc).text(cs.len(n).str(1, -1));

        // 确保已经完成
        return AmResult.FINISHED;
    }
}
