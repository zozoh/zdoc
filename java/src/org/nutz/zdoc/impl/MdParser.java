package org.nutz.zdoc.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Nums;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.zdoc.Parser;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZBlock;
import org.nutz.zdoc.ZDocNode;
import org.nutz.zdoc.ZDocNodeType;
import org.nutz.zdoc.ZLine;
import org.nutz.zdoc.ZLineType;
import org.nutz.zdoc.util.ZD;

public class MdParser implements Parser {

    private static final Log log = Logs.get();

    private MdScanner scanner;

    public MdParser() {
        scanner = new MdScanner();
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

    private void asParagraph(Parsing ing, ZBlock b) {
        ing.current.type(ZDocNodeType.PARAGRAPH);
        ing.fillCurrentEles(b.joinLines());
    }

    private void asBlockquote(Parsing ing, ZBlock b) {
        ing.current.type(ZDocNodeType.BLOCKQUOTE);

        ZDocNode blockquote = new ZDocNode().type(ZDocNodeType.BLOCKQUOTE);
        ZDocNode nd = blockquote;
        nd.attrs().set("$block-indent", 1); // 当前块应该的缩进值

        Iterator<ZLine> it = b.lines.iterator();
        ZLine line = it.hasNext() ? it.next() : null;
        ArrayList<ZLine> myLines = new ArrayList<ZLine>(b.lines.size());
        while (null != line) {
            // 寻找本行到下一行的边界
            myLines.add(line);
            ZLine nextLine = null;
            nextLine = it.hasNext() ? it.next() : null;
            while (null != nextLine) {
                if (nextLine.type == ZLineType.BLOCKQUOTE
                    && nextLine.blockLevel != line.blockLevel) {
                    break;
                }
                myLines.add(nextLine);
                nextLine = it.hasNext() ? it.next() : null;
            }
            // 生成节点
            ZDocNode myNode = blockquote;
            ing.fillEles(myNode, ZBlock.joinLines(line.indent, myLines));

            // 寻找父
            while (!nd.isTop()
                   && nd.attrs().getInt("$block-indent") > line.blockLevel) {
                nd = nd.parent();
            }
            myNode.parent(nd);

            // 下一行
            line = nextLine;
            nd = myNode;
        }

        // 加入到树中
        for (ZDocNode child : blockquote.children()) {
            child.parent(ing.current);
        }
    }

    private void asHr(Parsing ing, ZBlock b) {
        ing.current.type(ZDocNodeType.HR);
    }

    private void asComment(Parsing ing, ZBlock b) {
        String str = b.joinLines();
        // 是一个引用
        String trimed = str.trim();
        if (trimed.startsWith("@import ")) {
            ing.current.type(ZDocNodeType.LINK);
            ing.current.text(trimed.substring("@import ".length()).trim());
        }
        // 普通注释
        else {
            ing.current.type(ZDocNodeType.COMMENT);
            ing.current.text(str);
        }
    }

    private void asTable(Parsing ing, ZBlock b) {
        ZDocNode table = ing.current.type(ZDocNodeType.TABLE);

        char[] bqs = Nums.arrayC('\'', '"', '{');
        char[] eqs = Nums.arrayC('\'', '"', '}');
        ZDocNode tr = null;
        int colCount = 0;
        for (ZLine line : b.lines) {
            // 是一个分割行的话 ...
            if (line.text().matches("^[|: \t+-]{6,}$") && null != tr) {
                // 生成行列配置信息
                String[] ss = Strings.splitIgnoreBlank(line.text(), "[|]");
                for (int i = 0; i < ss.length; i++) {
                    String s = Strings.trim(ss[i]);
                    // 居中
                    if (Strings.isQuoteBy(s, ':', ':')) {
                        ss[i] = "center";
                    }
                    // 居左
                    else if (s.startsWith(":")) {
                        ss[i] = "left";
                    }
                    // 居右
                    else if (s.endsWith(":")) {
                        ss[i] = "right";
                    }
                    // 自动
                    else {
                        ss[i] = "auto";
                    }
                }
                table.attrs().set("$cols", ss);

                // 将之前所有的 TR 都变 TH
                Each<ZDocNode> callback = new Each<ZDocNode>() {
                    public void invoke(int index, ZDocNode row, int length) {
                        row.type(ZDocNodeType.THEAD);
                        for (ZDocNode col : row.children()) {
                            col.type(ZDocNodeType.TH);
                        }
                    }
                };
                callback.invoke(0, tr, -1);
                tr.prevEach(callback);
                // 继续循环 ...
                continue;
            }
            List<String> cells = ZD.splitToListEscapeQuote(line.text(),
                                                           "||",
                                                           bqs,
                                                           eqs);
            colCount = Math.max(colCount, cells.size());
            tr = new ZDocNode().type(ZDocNodeType.TR);
            tr.parent(table);
            for (String cell : cells) {
                ZDocNode td = new ZDocNode().type(ZDocNodeType.TD);
                td.parent(tr);
                ing.fillEles(td, cell);
            }
        }
        // 生成默认行列信息
    }

    private void asList(Parsing ing, ZBlock b) {
        ZDocNode listRootNode = new ZDocNode();
        ZDocNode nd = listRootNode;
        nd.type(b.type == ZLineType.UL ? ZDocNodeType.UL : ZDocNodeType.OL);

        Iterator<ZLine> it = b.lines.iterator();
        ZLine line = it.next();
        nd.attrs().set("$line-indent", line.indent);
        nd.attrs().set("$line-type", line.type);

        ArrayList<ZLine> myLines = new ArrayList<ZLine>(b.lines.size());
        ZDocNode li = null;
        while (null != line) {
            // 制作一个 LI
            li = new ZDocNode().type(ZDocNodeType.LI);
            // 一直搜索直到遇到下一个 LI
            myLines.clear();
            myLines.add(line);
            ZLine nextLine = it.hasNext() ? it.next() : null;
            while (null != nextLine && !nextLine.isForList()) {
                myLines.add(nextLine);
                nextLine = it.hasNext() ? it.next() : null;
            }
            // 得到字符串，并填充元素
            String str = ZBlock.joinLines(line.indent, myLines);
            ing.fillEles(li, str);

            // 加入列表
            li.parent(nd);

            // 下一行
            line = nextLine;
        }

        // 将列表块复制到当前节点
        ing.current.type(listRootNode.type());
        ing.current.attrs().putAll(listRootNode.attrs());
        ing.current.takeoverChildren(listRootNode);

        // 当前节点指向最后一个 li
        ing.current = li;
    }

    private void asCode(Parsing ing, ZBlock b) {
        ing.current.type(ZDocNodeType.CODE);
        ing.current.attrs().set("code-type", b.codeType);
        ing.current.text(b.joinLines());
    }

    private ZDocNode makeNode(Parsing ing, ZBlock b) {
        // 建立一个对应的节点
        ZDocNode nd = new ZDocNode();

        // 得到最近一个标题
        ZDocNode p = ing.current;
        while (!p.isTop() && ZDocNodeType.HEADER != p.type())
            p = p.parent();

        // 标题节点，则试图寻找一下层级
        if (ZLineType.HEADER == b.type) {
            while (p.depth() >= b.firstLine.blockLevel) {
                p = p.parent();
            }
        }

        // 将当前节点加入文档层级中
        nd.parent(p);

        // 标记节点为当前节点
        ing.current = nd;

        // 返回
        return ing.current;
    }

}
