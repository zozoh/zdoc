package org.nutz.zdoc.am;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;

public class ZDocImgSrcAm extends ZDocAm {

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        if ('>' == c) {
            return AmStatus.DONE_BACK;
        }
        if (Character.isWhitespace(c)) {
            return as.buffer.isEmpty() ? AmStatus.CONTINUE : AmStatus.DONE;
        }
        as.buffer.push(c);
        return AmStatus.CONTINUE;
    }

    // Matcher m = P.matcher("50x30:abc.png");
    // ----------------------------------------
    // 0: 50x30:abc.png
    // 1: 50x30:
    // 2: 50
    // 3: x30
    // 4: x
    // 5: 30
    // 6: :
    // 7: abc.png
    private static String regex = "^(([0-9]+)?(([xX])([0-9]+))?(:))?(.*)$";
    private static Pattern P = Pattern.compile(regex);

    @Override
    public void done(AmStack<ZDocEle> as) {
        String str = as.buffer.toTrimmed();
        Matcher m = P.matcher(str);
        if (!m.find()) {
            throw Lang.impossible();
        }
        // 获取栈头对象
        ZDocEle obj = as.peekObj();

        // 指明了宽度
        if (null != m.group(2)) {
            obj.width(Integer.parseInt(m.group(2)));
        }
        // 指明了高度
        if (null != m.group(5)) {
            obj.height(Integer.parseInt(m.group(5)));
        }
        // 设定了 src
        obj.type(ZDocEleType.IMG).src(Strings.trim(m.group(7)));

        // 清除缓冲
        as.buffer.clear();
    }
}
