package org.nutz.zdoc.am;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.zdoc.Am;
import org.nutz.zdoc.AmResult;
import org.nutz.zdoc.EachChar;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZDocChars;
import org.nutz.zdoc.ZDocEleType;

public class ZDocImageAm implements Am {

    static String REGEX = "^(<)"
                          + "(([0-9]+)([xX]?)([0-9]*)([ \t]*[:][ \t]*))?"
                          + "([^ ]+)"
                          + "(([ \t]+)(.*))?"
                          + "(>)$";

    private static final Pattern P = Pattern.compile(REGEX);

    @Override
    public boolean test(Parsing ing, ZDocChars cs) {
        final boolean[] re = new boolean[]{cs.startsIs('<')};

        if (re[0]) {
            cs.each(new EachChar() {
                public boolean isBreak(int index, char c) {
                    // 遇到了不能接受的字符
                    if ('"' == c
                        || '\'' == c
                        || '/' == c
                        || '=' == c
                        || Character.isWhitespace(c)) {
                        re[0] = false;
                        return true;
                    }
                    return '>' == c;
                }
            });
            // TODO 这里判断一下是否是 HTML 的标签
        }

        return re[0];
    }

    @Override
    public AmResult run(Parsing ing, ZDocChars cs) {

        final AmResult[] re = Lang.array(AmResult.FINISHED);

        // 遍历
        int n = cs.each(new EachChar() {
            public boolean isBreak(int index, char c) {
                // 遇到 '>' 就结束
                return '>' == c;
            }
        });

        // 分析
        // 3 : width
        // 5 : height
        // 7 : src
        // 10 : alt
        String s = cs.len(n).str();
        Matcher m = P.matcher(s);
        if (m.find()) {
            if (!Strings.isBlank(m.group(3))) {
                ing.ele().width(Integer.parseInt(m.group(3)));
            }
            if (!Strings.isBlank(m.group(5))) {
                ing.ele().height(Integer.parseInt(m.group(5)));
            }
            ing.ele().type(ZDocEleType.IMG);
            ing.ele().src(m.group(7));
            ing.ele().text(m.group(10));
        } else {
            ing.ele().src(s);
        }

        // 返回
        return re[0];
    }
}
