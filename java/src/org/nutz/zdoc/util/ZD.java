package org.nutz.zdoc.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Nums;
import org.nutz.zdoc.ZDocBlock;

public class ZD {

    /**
     * 将字符串拆分成一组字符串，给定的包裹字符之内的字符将被逃逸
     * 
     * @param str
     *            给定字符串
     * @param sep
     *            分隔字符串
     * @param bqs
     *            包裹字符开始列表
     * @param eqs
     *            包裹字符结束列表
     * @return 字符串列表，忽略空字符串
     */
    public static List<String> splitToListEscapeQuote(String str,
                                                      String sep,
                                                      char[] bqs,
                                                      char[] eqs) {
        char[] cs = str.toCharArray();
        char[] sepcs = sep.toCharArray();
        List<String> list = new LinkedList<String>();

        int off = 0;
        int i = 0;
        for (; i < cs.length; i++) {
            // 如果是包裹字符串，那么持续找到结束
            int pos = Nums.indexOf(bqs, cs[i]);
            if (pos >= 0) {
                for (i++; i < cs.length; i++) {
                    if (cs[i] == eqs[pos])
                        break;
                }
                continue;
            }

            // 如果匹配分隔字符串
            if (startsWith(cs, i, sepcs)) {
                // 看看是否需要加入到列表
                if (i > off) {
                    list.add(new String(cs, off, i - off));
                }
                // 偏移
                i += sepcs.length;
                off = i;
                i--;
            }
            // 继续下一个字符
        }
        // 最后看看是否需要加入最后一项
        if (i > off) {
            list.add(new String(cs, off, i - off));
        }

        return list;
    }

    public static boolean startsWith(char[] cs, int off, char[] cs2) {
        if (cs2.length > (cs.length - off))
            return false;
        for (int i = 0; i < cs2.length; i++) {
            if (cs[off + i] != cs2[i])
                return false;
        }
        return true;
    }

    /**
     * 将一组文档块合并成一个文档块
     * 
     * @param blocks
     *            块列表
     * @return 文档块，如果传入集合为空，返回 null
     */
    public static ZDocBlock merge(Collection<ZDocBlock> blocks) {
        if (null == blocks || blocks.isEmpty())
            return null;

        Iterator<ZDocBlock> it = blocks.iterator();
        // 第一块
        ZDocBlock b = it.next();

        // 融合之后的块
        while (it.hasNext()) {
            b.mergeWith(it.next());
        }

        // 返回
        return b;
    }

    private ZD() {}
}
