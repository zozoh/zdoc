package org.nutz.zdoc.am;

import org.nutz.lang.Strings;
import org.nutz.zdoc.Am;
import org.nutz.zdoc.AmResult;
import org.nutz.zdoc.EachChar;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZDocChars;
import org.nutz.zdoc.ZDocEleType;

public class ZDocLinkAm implements Am {

    @Override
    public boolean test(Parsing ing, ZDocChars cs) {
        return cs.startsIs('[');
    }

    @Override
    public AmResult run(Parsing ing, ZDocChars cs) {
        // 如果当前元素还没有搜索链接和文字的间隔
        if (Strings.isBlank(ing.ele().href())) {
            int n = cs.each(new EachChar() {
                public boolean isBreak(int index, char c) {
                    return c == ' ' || c == '\t' || c == ']';
                }
            });
            // 标记搜索到的字符串
            cs.len(n);
            // 得到链接
            String href = cs.str(1, 0);
            AmResult re = AmResult.HUNGUP;
            if (cs.endsIs(']')) {
                re = AmResult.FINISHED;
                href = href.substring(0, href.length() - 1);
            }
            if (href.startsWith("^")) {
                ing.ele().attr("target", "_blank");
                href = href.substring(1);
            }
            ing.ele().type(ZDocEleType.INLINE).href(Strings.trim(href));

            // 如果是挂起，那么回退一个字符，以便让主逻辑吃掉这个字符
            if (AmResult.HUNGUP == re) {
                cs.next();
                cs.off--;
                cs.len++;
            }

            // 返回指令
            return re;
        }
        // 如果到了这个分支，表示本自动机又重新接管了解析逻辑，那么除非遇到 ']' 一直都是挂起
        if (cs.startsIs(']')) {
            cs.len = 1;
            return AmResult.FINISHED;
        }
        return AmResult.HUNGUP;
    }

}
