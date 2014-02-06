package org.nutz.zdoc.am;

import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;

/**
 * 因为需要解析诸如:
 * 
 * <pre>
 * **abc** 或者 _xyz_
 * </pre>
 * 
 * 之类的字符串，所以在 enter 方法，遵循如下策略
 * 
 * <pre>
 * 在 enter 时，如果给定字符匹配 cs[0] 则表示可以入，
 *      自动机变为 eat 状态
 *      
 * 在 eat 状态，
 *      如果自动机填充 buffer 超过了 cs 的长度，
 *          则检查 buffer 是否为 cs 结尾
 * 
 * </pre>
 * 
 * 
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class MdEmphasisAm extends ZDocAm {

    private char[] cs;

    public MdEmphasisAm(String str) {
        cs = str.toCharArray();
    }

    @Override
    public AmStatus enter(AmStack<ZDocEle> as, char c) {
        if (c == cs[0]) {
            as.pushAm(this).pushObj(as.bornObj());
            as.buffer.push(c);
            return AmStatus.CONTINUE;
        }
        return AmStatus.DROP;
    }

    @Override
    public AmStatus eat(AmStack<ZDocEle> as, char c) {
        // 未填充完起始字符，则还是有可能 DROP 的
        int bfsz = as.buffer.size();
        if (bfsz < cs.length) {
            if (cs[bfsz] == c) {
                as.buffer.push(c);
                return AmStatus.CONTINUE;
            }
            return AmStatus.DROP;
        }
        // 填充一下字符，看看是否以 cs 结尾，来标志是否是结束
        as.buffer.push(c);

        if (as.buffer.endsWith(cs)) {
            // 有内容，就 DONE
            if (bfsz > cs.length)
                return AmStatus.DONE;

            // 这个分支表示缓冲木有实际内容，DROP 咯
            return AmStatus.DROP;
        }

        // 人家还要嘛 ~~~
        return AmStatus.CONTINUE;
    }

    @Override
    public void done(AmStack<ZDocEle> as) {
        // 移除最后的字符
        as.buffer.popFirst(cs.length).popLast(cs.length);

        // 弹出对象进行组合
        ZDocEle o = as.popObj().type(ZDocEleType.INLINE);
        o.text(as.buffer.toTrimmed());
        // 斜体
        if (cs.length == 1) {
            o.style("font-style", "italic");
        }
        // 粗体
        else {
            o.style("font-weight", "bold");
        }

        as.mergeHead(o);
        as.buffer.clear();
        as.popAm();

    }

}
