package org.nutz.zdoc.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.nutz.lang.Lang;
import org.nutz.lang.Nums;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.zdoc.Parser;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZDocBlock;
import org.nutz.zdoc.ZDocLine;
import org.nutz.zdoc.ZDocLineType;
import org.nutz.zdoc.ZDocNode;
import org.nutz.zdoc.ZDocNodeType;
import org.nutz.zdoc.util.ZD;

public class ZDocParser implements Parser {

    private static final Log log = Logs.get();

    private ZDocScanner scanner;

    public ZDocParser() {
        scanner = new ZDocScanner();
    }

    @Override
    public void build(Parsing ing) {
        scanner.scan(ing);
        Streams.safeClose(ing.reader);

        // 依次循环块，为每个块制作一个节点
        ListIterator<ZDocBlock> it = ing.blocks.listIterator();
        while (it.hasNext()) {
            // 得到块
            ZDocBlock b = it.next();

            try {
                // 空块无视
                if (b.isEmpty())
                    continue;

                // 建立对应节点
                makeNode(ing, b);

                // 根据块的不同类型，进行解析
                // <HTML> 的行要一直寻找到 </HTML> 行
                if (b.firstLine.trimLower().equals("<html>")) {
                    asHTML(ing, it, b);
                }
                // {{{ 开始的行，要一直寻找到 }}} 结束的行
                else if (b.firstLine.trimmed().equals("{{{")) {
                    asCode(ing, it, b);
                }
                // UL | OL
                else if (ZDocLineType.UL == b.type || ZDocLineType.OL == b.type) {
                    asList(ing, it, b);
                }
                // TABLE
                else if (ZDocLineType.TABLE == b.type) {
                    asTable(ing, b);
                }
                // COMMENT
                else if (ZDocLineType.COMMENT == b.type) {
                    asComment(ing, b);
                }
                // HR
                else if (ZDocLineType.HR == b.type) {
                    asHr(ing, b);
                }
                // BLOCKQUOTE
                else if (ZDocLineType.BLOCKQUOTE == b.type) {
                    asBlockquote(ing, b);
                }
                // 普通段落
                else if (ZDocLineType.PARAGRAPH == b.type) {
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

        ing.root.normalize();
    }

    private void asBlockquote(Parsing ing, ZDocBlock b) {
        ing.current.type(ZDocNodeType.BLOCKQUOTE);
        ing.current.attrs().set(ZD.ATT_BLOCK_INDENT, 0);

        int bIndent = 0;

        List<ZDocLine> blines = new ArrayList<ZDocLine>(b.lines.size());

        for (ZDocLine line : b.lines) {
            // 不同的块，要建立对应的节点
            if (line.blockIndent != bIndent) {
                // 如果已经记录了一些节点
                _lines_to_blockquote(ing, bIndent, blines);
                bIndent = line.blockIndent;
            }
            // 否则加入到行
            else {
                blines.add(line);
            }
        }

        // 扫尾最后一段
        _lines_to_blockquote(ing, bIndent, blines);

        // 将 current 回退到最顶级的 BLOCKQUOTE
        while (ing.current.parent().isBlockquote())
            ing.current = ing.current.parent();

    }

    private void _lines_to_blockquote(Parsing ing,
                                      int bIndent,
                                      List<ZDocLine> blines) {
        if (!blines.isEmpty()) {
            ZDocNode nd = new ZDocNode().type(ZDocNodeType.BLOCKQUOTE);
            nd.attrs().set(ZD.ATT_BLOCK_INDENT, bIndent);

            // 寻找父节点
            while (ing.current.parent().isBlockquote()
                   && ing.current.attrs().getInt(ZD.ATT_BLOCK_INDENT) >= bIndent) {
                ing.current = ing.current.parent();
            }

            // 加入
            nd.parent(ing.current);

            // 作为最后一个可能的父
            ing.current = nd;

            // 解析
            ZDocBlock bb = new ZDocBlock(blines);
            ing.fillCurrentEles(bb.joinLines());

            blines.clear();

        }
    }

    private void asHr(Parsing ing, ZDocBlock b) {
        ing.current.type(ZDocNodeType.HR);
    }

    private void asComment(Parsing ing, ZDocBlock b) {
        // 是个 LINK
        if (b.firstLine == b.lastLine
            && b.firstLine.trimLower().startsWith("@import ")) {
            ing.current.type(ZDocNodeType.LINK);
            String link = Strings.trim(b.firstLine.text.substring("@import ".length()));
            ing.current.text(link);
        }
        // 普通的注释
        else {
            ing.current.text(b.joinLines());
        }
    }

    private void asTable(Parsing ing, ZDocBlock b) {
        ZDocNode table = ing.current.type(ZDocNodeType.TABLE);
        // 每一个 ZDocLine 就是一个 TR 或者 THEAD
        for (ZDocLine line : b.lines) {
            // 表头分隔行，类似
            //
            // || --- ||:---:|| ---:||
            // 或
            // | --- |:---:|---:|
            // 或
            // --- |:---:|---:
            //
            if (line.type == ZDocLineType.TSEP) {
                String[] ss = Strings.splitIgnoreBlank(line.text, "[|]");
                for (int x = 0; x < ss.length; x++) {
                    String s = ss[x];
                    boolean l = s.startsWith(":");
                    boolean r = s.endsWith(":");
                    if (l && r) {
                        ss[x] = "justify";
                    } else if (l) {
                        ss[x] = "left";
                    } else if (r) {
                        ss[x] = "right";
                    } else {
                        ss[x] = null;
                    }
                    table.attrs().set("cols", ss);
                }
                continue;
            }
            // 可能是 TR 或者 THEAD
            ZDocNode row = new ZDocNode();
            ZDocNodeType cellType;
            // TR
            if (line.type == ZDocLineType.TR) {
                row.type(ZDocNodeType.TR);
                cellType = ZDocNodeType.TD;
            }
            // THEAD
            else if (line.type == ZDocLineType.THEAD) {
                row.type(ZDocNodeType.THEAD);
                cellType = ZDocNodeType.TH;
            }
            // ! 不可能
            else {
                throw Lang.impossible();
            }
            // 将一行拆分成一组 ZDocBlock
            char[] b0 = Nums.arrayC('`', '"', '\'', '{', '<', '[', '(');
            char[] b1 = Nums.arrayC('`', '"', '\'', '}', '>', ']', ')');
            List<String> ss = ZD.splitToListEscapeQuote(line.text, "|", b0, b1);
            // 依次加入当前行
            for (String s : ss) {
                ZDocBlock block = new ZDocBlock().setText(Strings.trim(s));
                ing.current = new ZDocNode().type(cellType);
                ing.fillCurrentEles(block.joinLines());
                ing.current.parent(row);
            }
            // 将当前行加入表格
            row.parent(table);
        }
        // 恢复解析时的当前节点
        ing.current = table;
    }

    private void asParagraph(Parsing ing, ZDocBlock b) {
        ing.current.type(ZDocNodeType.PARAGRAPH);
        ing.fillCurrentEles(b.joinLines());
    }

    private void asList(Parsing ing, ListIterator<ZDocBlock> it, ZDocBlock b) {
        ing.current.type(ZDocLineType.UL == b.type ? ZDocNodeType.UL
                                                  : ZDocNodeType.OL);
        while (it.hasNext()) {
            ZDocBlock b2 = it.next();
            // indent 比自己小的 BLOCK 统统算自己的块
            if (ZDocLineType.PARAGRAPH == b2.type && b2.indent < b.indent) {
                b.mergeWith(b2);
            }
            // indent 等于自己的块，如果类型一致也合并
            else if (b2.type == b.type && b2.indent == b.indent) {
                b.mergeWith(b2);
            }
            // 退回一个块
            else {
                it.previous();
                break;
            }
        }
    }

    private void asCode(Parsing ing, ListIterator<ZDocBlock> it, ZDocBlock b) {
        ing.current.type(ZDocNodeType.CODE);
        // 本段没有结尾，继续寻找后面的块
        if (!b.lastLine.trimmed().equals("}}}")) {
            while (it.hasNext()) {
                ZDocBlock b2 = it.next();
                b.mergeWith(b2);
                if (b2.lastLine.trimmed().equals("}}}"))
                    break;
            }
        }
        b.lines = b.sublines(1, -1);
        ing.current.text(b.joinLines());
    }

    private void asHTML(Parsing ing, ListIterator<ZDocBlock> it, ZDocBlock b) {
        ing.current.type(ZDocNodeType.PARAGRAPH);
        ing.current.attrs().set("zhtml", true);
        while (it.hasNext()) {
            ZDocBlock b2 = it.next();
            b.mergeWith(b2);
            if (b2.lastLine.trimLower().equals("</html>"))
                break;
        }
        b.lines = b.sublines(1, -1);
        // TODO 执行解析 ... Ams.fillEles(ams_html, ing, b);
    }

    private void makeNode(Parsing ing, ZDocBlock b) {
        // 建立一个对应的节点
        ZDocNode nd = new ZDocNode();
        nd.depth(b.indent + 1);

        // 将节点加入树
        ZDocNode p = ing.current;
        while (!p.isTop()) {
            if (p.depth() < nd.depth()
                && (p.type() == ZDocNodeType.PARAGRAPH || p.type() == ZDocNodeType.HEADER)) {
                break;
            }
            p = p.parent();
        }
        nd.parent(p);

        // 那么父节点就是一定是标题了
        if (!p.isTop() && !p.isHeader())
            p.type(ZDocNodeType.HEADER);

        // 作为当前节点
        ing.current = nd;
    }

}
