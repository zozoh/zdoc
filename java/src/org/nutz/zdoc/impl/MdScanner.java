package org.nutz.zdoc.impl;

import static org.nutz.zdoc.ZLineType.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZBlock;
import org.nutz.zdoc.ZLine;
import org.nutz.zdoc.ZLineType;

public class MdScanner extends AbstractScanner {

    @Override
    protected void scanWithException(Parsing ing) throws Exception {
        // 开始循环读取行
        String str = _read_line(ing);
        ZBlock block = new ZBlock();
        while (null != str) {
            // 评估当前行
            ZLine line = evalLineType(ing, new ZLine(str).evalIndent());
            ZLineType lntp = line.type;
            // ...........................................
            // 看看是否是元数据
            if (str.equals("---")) {
                if (null == parseMeta(ing))
                    break;
                str = _read_line(ing);
                continue;
            }
            // ...........................................
            // 加入之前的块
            if (!block.isEmpty()) {
                ing.blocks.add(block.fixLines());
                block = new ZBlock();
            }
            // ...........................................
            if (ZLineType.HEADER == lntp) {
                block.type = lntp;
                block._add(line);
            }
            // ...........................................
            // 代码
            else if (ZLineType.CODE == lntp) {
                block.type = lntp;
                // 依靠缩进的代码块
                if (line.indent > 0) {
                    line.indent--;
                    line.space -= 4;
                    block.indent = line.indent;
                    block._add(line);
                    while (null != (str = _read_line(ing))) {
                        line = new ZLine(str).evalIndent();
                        // 空行，直接退出
                        if(line.indent==0)
                            break;
                        line.indent--;
                        line.space -= 4;
                        if (Strings.isBlank(str) || line.indent >= block.indent) {
                            block._add(line.type(ZLineType.CODE)
                                           .compensateSpace());
                        } else {
                            evalLineType(ing, line).compensateSpace();
                            break;
                        }
                    }
                    continue;
                }
                // 依靠 ``` 来结束的代码块
                else {
                    block.indent = line.indent;
                    block.codeType = Strings.trim(line.text().substring(3));
                    while (null != (str = _read_line(ing))) {
                        line = new ZLine(str).evalIndent()
                                             .type(ZLineType.CODE)
                                             .compensateSpace();
                        if (line.trimmed().equals("```"))
                            break;
                        block._add(line.alignText(0));
                    }
                }
            }
            // ...........................................
            // 注释
            else if (ZLineType.COMMENT == lntp) {
                block.type = lntp;
                block.indent = line.indent;
                String txt = line.text();
                line.text(txt.substring("<!--".length()));
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = evalLineType(ing,
                                        new ZLine(str).evalIndent()
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
            else if (ZLineType.UL == lntp || ZLineType.OL == lntp) {
                block.type = lntp;
                block.indent = line.indent;
                ing.scanLevel = line.indent + 1;
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = evalLineType(ing, new ZLine(str).evalIndent());
                    lntp = line.type;
                    if (ZLineType.BLANK == lntp
                        || (lntp == block.type && line.indent == block.indent)) {
                        block._add(line);
                    } else {
                        break;
                    }
                }
                // 下面几种情况命中注定退出列表扫描，恢复代码库扫描级别
                if (ZLineType.PARAGRAPH == line.type) {
                    if (ZLineType.BLANK == block.lastLine.type) {
                        ing.scanLevel = 0;
                    }
                } else if (!line.isForList()) {
                    ing.scanLevel = 0;
                }
                continue;
            }
            // ...........................................
            // 引用
            else if (ZLineType.BLOCKQUOTE == lntp) {
                block.type = lntp;
                block.indent = line.indent;
                block._add(line);
                while (null != (str = _read_line(ing))) {
                    line = evalLineType(ing, new ZLine(str).evalIndent());
                    lntp = line.type;
                    // 引用块统统加入
                    if (ZLineType.BLOCKQUOTE == lntp) {
                        block._add(line);
                    }
                    // 如果是段落，那么仅当最后一行不是空行的情况才能加入
                    else if (ZLineType.PARAGRAPH == lntp) {
                        if (null != block.lastLine
                            && ZLineType.BLANK == block.lastLine.type)
                            break;
                        block._add(line);
                    }
                    // 空行也属于本块
                    else if (ZLineType.BLANK == lntp) {
                        block._add(line);
                    }
                    // 其他统统不属于本引用块
                    else {
                        break;
                    }
                }
                continue;
            }
            // ...........................................
            // HR
            else if (ZLineType.HR == lntp) {
                block.type = lntp;
                block.indent = 0;
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
                block.indent = 0;

                // 一直读到一个空行
                while (true) {
                    // 空行退出
                    if (BLANK == line.type) {
                        break;
                    }
                    // 标题行截断
                    else if (HEADER == line.type) {
                        if (!block.lines.isEmpty()) {
                            ing.blocks.add(block);
                            block = new ZBlock();
                        }
                        block.type = HEADER;
                        block._add(line);
                        ing.blocks.add(block);
                        block = new ZBlock();
                        break;
                    }
                    // HR 的话，仅将之前的一行作为标题
                    else if (HR == line.type) {
                        // 空块的话，则将变成标题块
                        if (block.lines.isEmpty()) {
                            block.type = HR;
                            block._add(line);
                            ing.blocks.add(block);
                            block = new ZBlock();
                        }
                        // 如果块只有一行，则整个块变成标题
                        else if (block.lines.size() == 1) {
                            block.type = HEADER;
                            block.firstLine.type = HEADER;
                            if (line.itype == '=')
                                block.firstLine.blockLevel = 1;
                            else
                                block.firstLine.blockLevel = 2;
                            ing.blocks.add(block);
                            block = new ZBlock();
                        }
                        // 如果有多行，则增加一个文字块和一个分割线
                        else {
                            ing.blocks.add(block);
                            block = new ZBlock();
                            block.type = HR;
                            block._add(line);
                            ing.blocks.add(block);
                            block = new ZBlock();
                        }

                        // 无论如何都会打破循环
                        break;
                    }
                    // 表格的话，整个块将被变成表格
                    else if (TABLE == line.type && block.lines.size() == 1) {
                        block.type = TABLE;
                        block._add(line);
                    }
                    // 引用的话，退出
                    else if (BLOCKQUOTE == line.type) {
                        break;
                    }
                    // 其他算是普通段落
                    else {
                        line = new ZLine(str).text(str)
                                             .type(ZLineType.PARAGRAPH);
                        block._add(line);
                    }

                    // 读取下一行
                    if (null == (str = _read_line(ing)))
                        break;
                    line = evalLineType(ing, new ZLine(str));

                } /* 普通段落 的 while 读取结束 */

                // 如果当前行是引用，不要读取下一行
                if (BLOCKQUOTE == line.type) {
                    continue;
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

    public String parseMeta(Parsing ing) throws IOException {
        // 一直读取到 "---" 结束的行
        String str = _read_line(ing);
        while (null != str) {
            String trimed = Strings.trim(str);
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
                String nm = Strings.trim(trimed.substring(0, pos));
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
                ing.root.attrs().set(nm, v);
            }

            // 读取下一行
            str = _read_line(ing);

        } /* ~end while in meta '---' */
        return str;
    }

    /**
     * @param line
     *            当前行
     * @param codeLevel
     *            当行缩进大于那个值才能算代码，默认为 0
     * @return 传入的行以便链式赋值
     */
    @Override
    protected ZLine evalLineType(Parsing ing, ZLine line) {
        // 判断是否是标题
        line.blockLevel = 0;
        for (; line.blockLevel < line.origin.length(); line.blockLevel++)
            if (line.origin.charAt(line.blockLevel) != '#')
                break;
        if (line.blockLevel > 0) {
            line.type = HEADER;
            line.text(Strings.trim(line.origin.substring(line.blockLevel)));
            return line;
        }

        String tmd = line.trimmed();
        // BLANK
        if (Strings.isEmpty(tmd)) {
            line.text("");
            line.type = BLANK;
        }
        // CODE
        else if (line.indent > ing.scanLevel || tmd.startsWith("```")) {
            line.type = CODE;
        }
        // UL
        else if (tmd.startsWith("* ")) {
            line.type = UL;
            line.text(tmd.substring(2));
        }
        // OL，用数字或者字母作为标识
        else if (tmd.matches("^[0-9a-zA-Z]+[.][ ].+$")) {
            line.itype = tmd.charAt(0);
            line.type = OL;
            line.text(tmd.substring(tmd.indexOf('.') + 2));
        }
        // HR
        else if (tmd.matches("^[=-]{4,}$")) {
            line.type = HR;
            line.itype = tmd.charAt(0);
        }
        // TABLE
        else if (tmd.matches("[: \t|-]{3,}")) {
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
