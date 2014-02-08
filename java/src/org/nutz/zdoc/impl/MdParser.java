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

public class MdParser extends AbstractParser {

    private static final Log log = Logs.get();

    private MdScanner scanner;

    public MdParser() {
        scanner = new MdScanner();
        table_sep = "|";
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
                // 普通段落
                if (ZLineType.PARAGRAPH == b.type) {
                    asParagraph(ing, b);
                }
                // 标题
                else if (ZLineType.HEADER == b.type) {
                    asHeader(ing, b);
                }
                // 代码
                else if (ZLineType.CODE == b.type) {
                    asCode(ing, b);
                }
                // UL | OL
                else if (ZLineType.UL == b.type || ZLineType.OL == b.type) {
                    asList(ing, b);
                }
                // <HTML> 的行要一直寻找到 </HTML> 行
                else if (ZLineType.HTML == b.type) {
                    throw Lang.impossible();
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

    private void asHeader(Parsing ing, ZBlock b) {
        ing.current.type(ZDocNodeType.HEADER);
        ing.current.attrs().set("tagName", "h" + b.firstLine.blockLevel);
        ing.fillCurrentEles(b.joinLines());
    }

    private ZDocNode makeNode(Parsing ing, ZBlock b) {
        // 建立一个对应的节点
        ZDocNode nd = new ZDocNode();

        // 根据当前节点得到一个正确的 parent
        ZDocNode p = ing.current;

        // 当前块是标题节点，那么试图从当前节点找到最接近它的标题
        if (ZLineType.HEADER == b.type) {
            while (!p.isTop()
                   && (ZDocNodeType.HEADER != p.type() || p.depth() >= b.firstLine.blockLevel)) {
                p = p.parent();
            }
        }
        // 对于列表项目，有自己的搜索逻辑:
        // 只要块的 indent 比自己所在列表的 $line-indent 小，就属于自己
        else if (ZDocNodeType.LI == p.type()) {
            while (true) {
                int indent = p.parent().attrs().getInt("$line-indent");
                if (b.indent <= indent) {
                    p = p.parent().parent();
                    // 已经不再是列表了，那么一定是标题，否则就见鬼了
                    if (ZDocNodeType.LI != p.type()) {
                        if (ZDocNodeType.HEADER != p.type()) {
                            throw Lang.impossible();
                        }
                        break;
                    }
                }
                // 这个块肯定属于这个 LI，退出把
                else {
                    break;
                }
            }
        }
        // 其他节点，得到最近一个标题
        else {
            while (!p.isTop() && ZDocNodeType.HEADER != p.type())
                p = p.parent();
        }

        // 将当前节点加入文档层级中
        nd.parent(p);

        // 标记节点为当前节点
        ing.current = nd;

        // 返回
        return ing.current;
    }

}
