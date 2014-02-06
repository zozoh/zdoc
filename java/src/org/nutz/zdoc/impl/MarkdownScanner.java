package org.nutz.zdoc.impl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZBlock;
import org.nutz.zdoc.ZLine;
import org.nutz.zdoc.ZLineType;

public class MarkdownScanner extends AbstractScanner {

    @Override
    protected void scanWithException(Parsing ing) throws Exception {
        // 开始循环读取行
        String str = _read_line(ing);
        ZBlock block = new ZBlock();
        while (null != str) {
            ZLine line = new ZLine(str);
            // ...........................................
            // 看看是否是元数据
            if (line.trimmed().equals("---")) {
                // 一直读取到 "---" 结束的行
                str = _read_line(ing);
                String trimed = Strings.trim(str);
                while (null != str) {
                    if ("---".equals(trimed))
                        break;

                    int pos = trimed.indexOf(':');
                    // 如果没有冒号，则增加一个布尔值
                    if (pos < 0) {
                        ing.root.attrs().add(trimed, true);
                    }
                    // 如果开始第一个字符就是冒号，忽略
                    else if (pos == 0) {}
                    // 如果以冒号结尾，则表示后面跟随一个列表
                    else if (pos == trimed.length() - 1) {
                        String nm = trimed.substring(0, pos);
                        List<String> list = new ArrayList<String>(10);
                        while (null != (str = _read_line(ing))) {
                            trimed = Strings.trim(str);
                            // 遇到是 "- " 开头的行，则加入列表
                            if (trimed.startsWith("- ")) {
                                list.add(Strings.trim(trimed.substring(2)));
                            }
                            // 否则就退出，因为必然开始下一个元数据了
                            else {
                                break;
                            }
                        }
                        // 加入到属性中
                        ing.root.attrs().add(nm, list);
                        continue;
                    }
                    // 否则用冒号作为分隔，存入元数据名值对
                    else {
                        String nm = Strings.trim(trimed.substring(0, pos));
                        String v = Strings.trim(trimed.substring(pos + 1));
                        ing.root.attrs().add(nm, v);
                    }

                    // 读取下一行
                    str = _read_line(ing);

                } /* ~end while in meta '---' */

                if (null == str)
                    break;
            }
            // ...........................................
            ZLineType lntp = evalLineType(line);
            // ...........................................
            // 加入之前的块
            if (!block.isEmpty()) {
                ing.blocks.add(block.fixLines());
                block = new ZBlock();
            }
            // ...........................................
            // 代码
            else if (ZLineType.CODE == lntp) {
                block.type = lntp;

                // 依靠缩进的代码块
                if (line.indent > 0) {
                    block._add(line.alignText(1));
                    while (null != (str = _read_line(ing))) {
                        line = new ZLine(str);
                        if (ZLineType.BLANK != line.type && line.indent > 0) {
                            block._add(line.alignText(1));
                        } else {
                            break;
                        }
                    }
                    continue;
                }
                // 依靠 ``` 来结束的代码块
                block.codeType = line.text;
                while (null != (str = _read_line(ing))) {
                    line = new ZLine(str);
                    if (line.trimmed().equals("```"))
                        break;
                    block._add(line.alignText(1));
                }
                str = _read_line(ing);
            }
            // ...........................................
            // 注释
            else if (ZLineType.COMMENT == lntp) {
                block.type = lntp;
                block.indent = line.indent;
                line.text = line.text.substring("<!--".length());
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = new ZLine(str);
                    if (line.trimmed().endsWith("-->")) {
                        line.text = line.text.substring(0, line.text.length()
                                                           - "-->".length());
                        block._add(line);
                        break;
                    }
                    block._add(line);
                }
            }
            // ...........................................
            // 列表
            else if (ZLineType.UL == lntp || ZLineType.OL == lntp) {
                block.type = lntp;
                block.indent = line.indent;
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = new ZLine(str);
                    lntp = evalLineType(line);
                    if (ZLineType.BLANK == lntp
                        || (lntp == block.type && line.indent == block.indent)) {
                        block._add(line);
                    } else {
                        break;
                    }
                }
            }
            // ...........................................
            // 引用
            else if (ZLineType.BLOCKQUOTE == lntp) {
                block.type = lntp;
                block.indent = line.indent;
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = new ZLine(str);
                    lntp = evalLineType(line);
                    if (lntp == block.type || lntp == ZLineType.PARAGRAPH) {
                        block._add(line);
                    } else {
                        break;
                    }
                }
            }
            // ...........................................
            // HR
            else if (ZLineType.HR == lntp) {
                block.type = lntp;
                block.indent = line.indent;
                block._add(line);
            }
            // ...........................................
            // HTML
            else if (ZLineType.HTML == lntp) {
                throw Lang.impossible();
            }
            // ...........................................
            // 普通段落
            else if (ZLineType.PARAGRAPH == lntp) {
                block.type = lntp;
                block.indent = line.indent;
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = new ZLine(str);
                    lntp = evalLineType(line);
                    if (lntp == block.type && line.indent == block.indent) {
                        block._add(line);
                    } else {
                        break;
                    }
                }
            }
            // ...........................................
            // 空行
            else if (ZLineType.BLANK == lntp) {
                // 啥都不做，继续读下一行
            }
            // 读取下一行
            str = _read_line(ing);

        } /* end of while */

        // 最后一块
        if (!block.isEmpty())
            ing.blocks.add(block.fixLines());
    }

    @Override
    protected ZLineType evalLineType(ZLine line) {
        if (line.indent > 0)
            return ZLineType.PARAGRAPH;

        String tmd = line.trimmed();
        if (tmd.startsWith("```")) {
            line.indent = 0;
            line.text = Strings.trim(tmd.substring(3));
            return ZLineType.CODE;
        }

        if (tmd.matches("[: \t|-]{3,}"))
            return ZLineType.TABLE;

        if (tmd.startsWith("<!--"))
            return ZLineType.COMMENT;

        if (line.trimLower().equals("<html>"))
            return ZLineType.HTML;

        return line.type;
    }

}
