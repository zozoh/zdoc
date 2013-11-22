package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.lang.Strings;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;

public class ZDocEmphasisAm extends ZDocAm {

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        if ('{' == c) {
            as.pushAm(this).pushObj(as.bornObj());
            return AmStatus.CONTINUE;
        }
        return AmStatus.DROP;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        if ('}' == c)
            return AmStatus.DONE;
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        ZDocEle o = as.popObj().type(ZDocEleType.INLINE);
        String str = as.buffer.toTrimmed();

        int pos;
        // 寻找头部的文字内容描述
        for (pos = 0; pos < str.length(); pos++) {
            char c = str.charAt(pos);
            // 粗体 {*some text}
            if ('*' == c) {
                o.style("font-weight", "bold");
                continue;
            }
            // 斜体 {/some text}
            else if ('/' == c) {
                o.style("font-style", "italic");
                continue;
            }
            // 下划线 {_some text}
            else if ('_' == c) {
                o.style("text-decoration", "underline");
                continue;
            }
            // 穿越线 {~some text}
            else if ('~' == c) {
                o.style("text-decoratioin", "line-through");
                continue;
            }
            // 颜色 {#FF0;some text}
            else if ('#' == c) {
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                int maxPos = Math.min(pos + 10, str.length());
                for (pos++; pos < maxPos; pos++) {
                    c = str.charAt(pos);
                    if (c == ';' || c == ' ')
                        break;
                    sb.append(c);
                }
                o.style("color", sb.toString());
                continue;
            }
            // 标注 {^sup}
            else if ('^' == c) {
                o.type(ZDocEleType.SUP);
                continue;
            }
            // 底注 {,sub}
            else if (',' == c) {
                o.type(ZDocEleType.SUB);
                continue;
            }
            // 都不是，那么退出 ...
            else {
                break;
            }
        }

        try {
            String text = pos > 0 ? Strings.trim(str.substring(pos)) : str;
            o.text(text);
            as.mergeHead(o);
            as.buffer.clear();
            as.popAm();
        }
        catch (RuntimeException e) {
            throw e;
        }
    }
}
