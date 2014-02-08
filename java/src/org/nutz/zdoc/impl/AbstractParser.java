package org.nutz.zdoc.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Nums;
import org.nutz.lang.Strings;
import org.nutz.zdoc.Parser;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZBlock;
import org.nutz.zdoc.ZDocNode;
import org.nutz.zdoc.ZDocNodeType;
import org.nutz.zdoc.ZLine;
import org.nutz.zdoc.ZLineType;
import org.nutz.zdoc.util.ZD;

public abstract class AbstractParser implements Parser {

    protected String table_sep;

    protected void asTable(Parsing ing, ZBlock b) {
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
                                                           table_sep,
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

    protected void asBlockquote(Parsing ing, ZBlock b) {
        ing.current.type(ZDocNodeType.BLOCKQUOTE);
        ZDocNode bq = ing.current;
        ZDocNode p = new ZDocNode().type(ZDocNodeType.PARAGRAPH).parent(bq);
        // 当前快缩进的层级
        int lvl = 1;
        // 循环每一行
        for (ZLine line : b.lines) {
            // BLOCKQUOTE
            if (ZLineType.BLOCKQUOTE == line.type) {
                // 同级
                if (line.blockLevel == lvl) {
                    if (Strings.isBlank(line.text())) {
                        p = new ZDocNode().type(ZDocNodeType.PARAGRAPH)
                                          .parent(bq);
                    }
                    ing.fillEles(p, line.text());
                }
                // 向上
                else if (line.blockLevel < lvl) {
                    while (bq != ing.current && line.blockLevel < lvl) {
                        bq = bq.parent();
                        lvl--;
                    }
                    p = new ZDocNode().type(ZDocNodeType.PARAGRAPH).parent(bq);
                    ing.fillEles(p, line.text());
                }
                // 向下
                else {
                    while (line.blockLevel > lvl) {
                        ZDocNode sub = new ZDocNode().type(ZDocNodeType.BLOCKQUOTE)
                                                     .parent(bq);
                        lvl++;
                        bq = sub;
                    }
                    p = new ZDocNode().type(ZDocNodeType.PARAGRAPH).parent(bq);
                    ing.fillEles(p, line.text());
                }
            }
            // PARAGRAPH
            else if (ZLineType.PARAGRAPH == line.type) {
                ing.fillEles(p, line.text());
            }
            // BLANK
            else {}
            // 每行结尾都增加一个空格
            p.addEle(ZD.ele(" "));
        }
    }

    protected void asHr(Parsing ing, ZBlock b) {
        ing.current.type(ZDocNodeType.HR);
    }

    protected void asComment(Parsing ing, ZBlock b) {
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

    protected void asParagraph(Parsing ing, ZBlock b) {
        ing.current.type(ZDocNodeType.PARAGRAPH);
        ing.fillCurrentEles(b.joinLines());
    }

    protected void asCode(Parsing ing, ZBlock b) {
        ing.current.type(ZDocNodeType.CODE);
        ing.current.attrs().set("code-type", b.codeType);
        ing.current.text(b.joinLines());
    }

    protected void asList(Parsing ing, ZBlock b) {
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

}
