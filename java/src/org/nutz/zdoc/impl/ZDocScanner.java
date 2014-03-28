package org.nutz.zdoc.impl;

import static org.nutz.zdoc.ZLineType.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZBlock;
import org.nutz.zdoc.ZLine;
import org.nutz.zdoc.ZLineType;

public class ZDocScanner extends AbstractScanner {

    protected void scanWithException(Parsing ing) throws Exception {
        String str;
        // 读取文档元数据
        while (null != (str = _read_line(ing))) {
            // 不是元数据描述行，则退出
            if (!str.startsWith("#")) {
                break;
            }
            // 如果是元数据，则分析一下
            int pos = str.indexOf(':');
            // 有值
            if (pos > 0) {
                String key = Strings.trim(str.substring(1, pos));
                String val = Strings.trim(str.substring(pos + 1));
                // 如果值被 '[' 和 ']' 包裹，也是 List
                if (Strings.isQuoteBy(val, '[', ']')) {
                    String vs = val.substring(1, val.length() - 1);
                    String[] ss = Strings.splitIgnoreBlank(vs);
                    for (String s : ss) {
                        ing.root.attrs().add(key, s);
                    }
                }
                // 对于 author ，一定是 list
                else if (("author".equals(key) || "verifier".equals(key))) {
                    ing.root.attrs().add(key, val);
                }
                // 设置普通值
                else {
                    ing.root.attrs().set(key, val);
                }
            }
            // 没值
            else {
                ing.root.attrs().set(Strings.trim(str.substring(1)), true);
            }
        }
        // 到了文档结尾
        if (null == str)
            return;

        // 开始循环读取行
        ZBlock block = new ZBlock();
        ZLine line = evalLineType(new ZLine(str).evalIndent());
        while (null != str) {
            // ...........................................
            // 加入之前的块
            if (!block.isEmpty()) {
                ing.blocks.add(block.fixLines());
                block = new ZBlock();
            }
            // ...........................................
            // 表格
            if (ZLineType.TABLE == line.type) {
                block.type = line.type;
                block.indent = line.indent;
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = evalLineType(new ZLine(str).evalIndent());
                    if (ZLineType.TABLE != line.type)
                        break;
                    block._add(line);
                }
                if (ZLineType.TABLE != line.type)
                    continue;
            }
            // ...........................................
            // 代码
            else if (ZLineType.CODE == line.type) {
                block.type = line.type;
                block.indent = line.indent;
                int pBegin = line.trimLower().indexOf('<');
                if (pBegin > 0) {
                    int pEnd = line.trimLower().indexOf('>', pBegin);
                    if (pEnd > 0) {
                        block.codeType = Strings.trim(line.trimLower()
                                                          .substring(pBegin + 1,
                                                                     pEnd));
                    }
                }
                while (null != (str = _read_line(ing))) {
                    line = new ZLine(str).evalIndent()
                                         .type(ZLineType.CODE)
                                         .compensateSpace();
                    if (line.trimmed().equals("}}}"))
                        break;
                    block._add(line.alignText(block.indent));
                }
            }
            // ...........................................
            // 注释
            else if (ZLineType.COMMENT == line.type) {
                block.type = line.type;
                block.indent = line.indent;
                String txt = line.text();
                line.text(txt.substring("<!--".length()));
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = evalLineType(new ZLine(str).evalIndent()
                                                      .type(COMMENT)
                                                      .alignText(block.indent));
                    // 结束行
                    int pos = line.text().lastIndexOf("-->");
                    if (pos >= 0) {
                        line.text(txt.substring(0, pos));
                        block._add(line);
                        break;
                    }
                    // 同志们，那么就直接加吧
                    block._add(line);
                }
            }
            // ...........................................
            // 列表
            else if (ZLineType.UL == line.type || ZLineType.OL == line.type) {
                block.type = line.type;
                block.indent = line.indent;
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = evalLineType(new ZLine(str).evalIndent());
                    if (ZLineType.BLANK == line.type
                        || (line.type == block.type && line.indent == block.indent)) {
                        block._add(line);
                    } else {
                        break;
                    }
                }
                continue;
            }
            // ...........................................
            // 引用
            else if (ZLineType.BLOCKQUOTE == line.type) {
                block.type = line.type;
                block.indent = line.indent;
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = evalLineType(new ZLine(str).evalIndent());
                    if (line.type == block.type
                        || line.type == ZLineType.PARAGRAPH) {
                        block._add(line);
                    } else {
                        break;
                    }
                }
                continue;
            }
            // ...........................................
            // HR
            else if (ZLineType.HR == line.type) {
                block.type = line.type;
                block.indent = line.indent;
                block._add(line);
            }
            // ...........................................
            // HTML
            else if (ZLineType.HTML == line.type) {
                throw Lang.impossible();
            }
            // ...........................................
            // 普通段落
            else if (ZLineType.PARAGRAPH == line.type) {
                block.type = line.type;
                block.indent = line.indent;
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = evalLineType(new ZLine(str).evalIndent());
                    if (line.type == block.type && line.indent == block.indent) {
                        block._add(line);
                    } else {
                        break;
                    }
                }
                continue;
            }
            // ...........................................
            // 空行
            else if (ZLineType.BLANK == line.type) {
                // 啥都不做，继续读下一行
            }

            // 再读一行
            str = this._read_line(ing);
            line = evalLineType(new ZLine(str).evalIndent());
        }

        // 最后一块
        if (!block.isEmpty())
            ing.blocks.add(block.fixLines());
    }

    private ZLine evalLineType(ZLine line) {
        return evalLineType(null, line);
    }

    @Override
    protected ZLine evalLineType(Parsing ing, ZLine line) {

        String tmd = line.trimmed();
        // BLANK
        if (Strings.isEmpty(tmd)) {
            line.text("");
            line.type = BLANK;
        }
        // UL
        else if (tmd.startsWith("* ")) {
            line.type = UL;
            line.text(tmd.substring(2));
        }
        // OL
        else if (tmd.startsWith("# ")) {
            line.itype = '#';
            line.type = OL;
            line.text(tmd.substring(2));
        }
        // OL，用数字或者字母作为标识
        else if (tmd.matches("^[0-9a-zA-Z]+[.][ ].+$")) {
            line.itype = tmd.charAt(0);
            line.type = OL;
            line.text(tmd.substring(tmd.indexOf('.') + 2));
        }
        // CODE
        else if (tmd.startsWith("{{{")) {
            line.type = CODE;
        }
        // HR
        else if (tmd.matches("^[=-]{4,}$")) {
            line.type = HR;
            line.text(tmd);
        }
        // TABLE
        else if (Strings.isQuoteBy(tmd, "||", "||")) {
            line.type = TABLE;
        }
        // COMMENT
        else if (tmd.startsWith("<!--")) {
            line.type = COMMENT;
        }
        // HTML
        else if (tmd.equalsIgnoreCase("<html>")) {
            line.type = HTML;
        }
        // BLOCKQUOTE
        else if (tmd.startsWith("> ")) {
            line.type = BLOCKQUOTE;
            Matcher m = Pattern.compile("^((>[ \t]+)+)(.*)$").matcher(tmd);
            if (m.find()) {
                line.blockLevel = m.group(1).replaceAll("[ \t]", "").length();
                line.text(m.group(3));
            } else {
                throw Lang.impossible();
            }
        }
        // PARAGRAPH
        else {
            line.type = PARAGRAPH;
            line.compensateSpace();
        }

        // 返回输入以便链式赋值
        return line;
    }

}
