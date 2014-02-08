package org.nutz.zdoc.impl;

import java.util.ListIterator;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZBlock;
import org.nutz.zdoc.ZDocNode;
import org.nutz.zdoc.ZDocNodeType;
import org.nutz.zdoc.ZLineType;

public class ZDocParser extends AbstractParser {

    private static final Log log = Logs.get();

    private ZDocScanner scanner;

    public ZDocParser() {
        scanner = new ZDocScanner();
        table_sep = "||";
    }

    @Override
    public void build(Parsing ing) {
        scanner.scan(ing);
        Streams.safeClose(ing.reader);

        // 依次循环块，为每个块制作一个节点
        ListIterator<ZBlock> it = ing.blocks.listIterator();
        while (it.hasNext()) {
            // 得到块
            ZBlock b = it.next();

            try {
                // 空块无视
                if (b.isEmpty())
                    continue;

                // 建立对应节点
                makeNode(ing, b);

                // 根据块的不同类型，进行解析
                // <HTML> 的行要一直寻找到 </HTML> 行
                if (ZLineType.HTML == b.type) {
                    throw Lang.impossible();
                }
                // 代码
                else if (ZLineType.CODE == b.type) {
                    asCode(ing, b);
                }
                // UL | OL
                else if (ZLineType.UL == b.type || ZLineType.OL == b.type) {
                    asList(ing, b);
                }
                // TABLE
                else if (ZLineType.TABLE == b.type) {
                    asTable(ing, b);
                }
                // COMMENT
                else if (ZLineType.COMMENT == b.type) {
                    asComment(ing, b);
                }
                // HR
                else if (ZLineType.HR == b.type) {
                    asHr(ing, b);
                }
                // BLOCKQUOTE
                else if (ZLineType.BLOCKQUOTE == b.type) {
                    asBlockquote(ing, b);
                }
                // 普通段落
                else if (ZLineType.PARAGRAPH == b.type) {
                    asParagraph(ing, b);
                }
                // 肯定有啥错
                else {
                    throw Lang.impossible();
                }
            }
            catch (Exception e) {
                log.warn(String.format("Fail to parse block : \n%s \n",
                                       b.toString()),
                         e);
            }
        }

        ing.root.normalizeChildren();
    }

    private ZDocNode makeNode(Parsing ing, ZBlock b) {
        return makeNode(ing, b.indent + 1);
    }

    private ZDocNode makeNode(Parsing ing, int depth) {
        // 建立一个对应的节点
        ZDocNode nd = new ZDocNode();
        nd.depth(depth);

        joinTo(ing.current, nd);

        // 作为当前节点
        ing.current = nd;

        // 返回
        return ing.current;
    }

    private void joinTo(ZDocNode p, ZDocNode nd) {
        // 将节点加入树
        while (!p.isTop()) {
            if (p.getLogicDepth() < nd.depth()
                && (p.is(ZDocNodeType.PARAGRAPH,
                         ZDocNodeType.HEADER,
                         ZDocNodeType.LI))) {
                break;
            }
            p = p.parent();
        }
        nd.parent(p);

        // 那么如果父节点是普通段落，就一定是标题（这话说的有点绕 -_-!)
        if (!p.isTop() && p.is(ZDocNodeType.PARAGRAPH))
            p.type(ZDocNodeType.HEADER);
    }
}
