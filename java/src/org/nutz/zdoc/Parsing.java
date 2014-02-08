package org.nutz.zdoc;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.nutz.am.Am;
import org.nutz.am.AmFactory;
import org.nutz.am.AmStack;
import org.nutz.am.AmStatus;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.zdoc.am.ZDocAmStack;
import org.nutz.zdoc.am.ZDocParallelAm;

public class Parsing {

    public BufferedReader reader;

    public List<ZBlock> blocks;

    public int depth;

    public ZDocNode root;

    public ZDocNode current;

    public AmFactory fa;

    public String rootAmName;

    public ZDocAmStack stack;

    public StringBuilder raw;

    /**
     * 供扫描器使用的，说明现在文档正在处于的层别，默认为 0
     * <p>
     * 比如对于 Markdown 之类的依靠缩进来判断代码段
     */
    public int scanLevel;

    public Parsing(Reader reader) {
        this.reader = Streams.buffr(reader);
        this.root = new ZDocNode();
        this.current = root;
        this.blocks = new ArrayList<ZBlock>();
        this.stack = new ZDocAmStack(10);
        this.raw = new StringBuilder();
    }

    /**
     * 根据一段字符串填充当前的节点
     * 
     * @param str
     *            字符串
     * @return 自身
     * 
     * @see #fillEles(ZDocNode, String)
     */
    public ZDocNode fillCurrentEles(String str) {
        return fillEles(current, str);
    }

    /**
     * 根据一段字符串填充节点
     * 
     * @param nd
     *            节点
     * @param str
     *            字符串
     * @return 自身
     */
    public ZDocNode fillEles(ZDocNode nd, String str) {
        ZDocEle ele = parseString(str);
        // 这种情况需要仅仅加入所有的子 ...
        if (ele.isWrapper()) {
            nd.addEles(ele.children());
        }
        // 加入自己就成
        else {
            nd.addEle(ele);
        }
        return nd;
    }

    /**
     * 从一个字符串中解析出一个 ZDocEle 对象
     * 
     * @param str
     *            字符串对象
     * @return 节点内容元素对象
     */
    public ZDocEle parseString(String str) {
        char[] cs = str.toCharArray();
        Am<ZDocEle> am = fa.getAm(ZDocParallelAm.class, rootAmName);
        // 准备堆栈
        AmStack<ZDocEle> stack = this.stack.born();
        stack.pushObj(stack.bornObj());
        // 用第一个字符测试...
        if (am.enter(stack, cs[0]) != AmStatus.CONTINUE) {
            throw Lang.impossible();
        }
        // 循环每个字符
        for (int i = 1; i < cs.length; i++) {
            char c = cs[i];
            AmStatus st = stack.eat(c);
            if (AmStatus.CONTINUE != st)
                throw Lang.makeThrow("Fail to parse :\n%s", str);
        }
        // 关闭堆栈得到对象
        return stack.close();
    }

}
