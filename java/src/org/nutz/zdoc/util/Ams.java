package org.nutz.zdoc.util;

import org.nutz.lang.Lang;
import org.nutz.zdoc.Am;
import org.nutz.zdoc.AmResult;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZDocBlock;
import org.nutz.zdoc.ZDocChars;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;
import org.nutz.zdoc.ZDocLine;
import org.nutz.zdoc.ZDocLineType;

/**
 * 创建自动机列表的帮主函数
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public final class Ams {

    public static void fillEles(Am[] amList, Parsing ing, ZDocBlock block) {
        // 准备解析用字符集
        ZDocChars cs = new ZDocChars();

        // 逐行
        for (ZDocLine line : block.lines) {
            // 忽略空行
            if (line.type == ZDocLineType.BLANK)
                continue;

            // 准备遍历本行
            cs.set(line.text);

            // 每次移动一个字符，看看是不是有自动机可以从这个字符解析
            while (cs.len > 0) {
                // 执行自动机
                AmResult re = null;

                // 准备开始一个新的自动机
                // 寻找自动机，如果没有找到， re==null 就是标志
                for (Am a : amList) {
                    // 如果找到了自动机
                    if (a.test(ing, cs)) {
                        // 如果之前有内容，添加一个普通文字元素
                        addTxtEle(ing, cs);

                        // 准备自动机堆栈
                        ing.ams.addLast(a);
                        ing.eles.add(new ZDocEle());

                        // 退出寻找自动机的循环
                        break;
                    }
                }
                // 执行自动机
                if (!ing.ams.isEmpty()) {
                    re = ing.am().run(ing, cs);
                    // 如果自动机完成，那么添入元素
                    if (AmResult.FINISHED == re) {
                        // 处理余下的内容
                        addTxtEle(ing, cs);

                        // 弹出自动机也其处理的元素
                        ing.ams.pollLast();
                        ZDocEle ele = ing.eles.pollLast();

                        // 将元素加入文档块
                        ing.current.addEle(ele);

                        // 指向下一个元素的开始
                        cs.next();
                        cs.left = cs.off;
                    }
                    // 当前自动机挂起，由其他自动机接管
                    else if (AmResult.HUNGUP == re) {
                        cs.len--;
                        cs.off++;
                    }
                    // 当前的自动机一定还没有喂完，退出循环，进入下一行
                    else if (AmResult.NEED_MORE == re) {
                        break;
                    }
                    // 不可能
                    else {
                        throw Lang.impossible();
                    }
                }
                // 如果没找到自动机，偏移一个字符
                else {
                    cs.len--;
                    cs.off++;
                }

            } // ~ end of while {

            // 全部循环完一行，如果没有自动机在栈里，判断扫清剩余的文字的文字
            if (ing.ams.isEmpty())
                addTxtEle(ing, cs);

        } // ~ end of for each lines

        // 如果还有元素
        if (!ing.eles.isEmpty() && ZDocEleType.NEW != ing.ele().type()) {
            ing.current.addEle(ing.ele());
        }

        // 清除自动机堆栈
        ing.eles.clear();
        ing.ams.clear();

    }

    private static void addTxtEle(Parsing ing, ZDocChars cs) {
        if (cs.left < cs.off) {
            int sz = cs.off - cs.left;
            ZDocEle ele = new ZDocEle();
            ele.type(ZDocEleType.INLINE)
               .text(new String(cs.chars, cs.left, sz));

            // 修改 left
            cs.left = cs.off;

            // 如果有元素，加入到当前元素中
            if (!ing.eles.isEmpty()) {
                ele.parent(ing.ele());
            }
            // 否则直接加入当前块中
            else {
                ing.current.addEle(ele);
            }
        }
    }

    // 禁止实例化
    private Ams() {}
}
