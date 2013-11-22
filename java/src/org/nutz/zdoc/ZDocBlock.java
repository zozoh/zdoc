package org.nutz.zdoc;

import static org.nutz.zdoc.ZDocLineType.BLANK;
import static org.nutz.zdoc.ZDocLineType.BLOCKQUOTE;
import static org.nutz.zdoc.ZDocLineType.COMMENT;
import static org.nutz.zdoc.ZDocLineType.COMMENT_BEGIN;
import static org.nutz.zdoc.ZDocLineType.COMMENT_END;
import static org.nutz.zdoc.ZDocLineType.HR;
import static org.nutz.zdoc.ZDocLineType.PARAGRAPH;
import static org.nutz.zdoc.ZDocLineType.TABLE;
import static org.nutz.zdoc.ZDocLineType.THEAD;
import static org.nutz.zdoc.ZDocLineType.TR;
import static org.nutz.zdoc.ZDocLineType.TSEP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ZDocBlock {

    public List<ZDocLine> lines;

    public ZDocLine firstLine;

    public ZDocLine lastLine;

    public int indent;

    public ZDocLineType type;

    public ZDocBlock() {
        this.lines = new LinkedList<ZDocLine>();
    }

    public ZDocBlock(List<ZDocLine> lines) {
        this.lines = lines;
        if (!lines.isEmpty()) {
            firstLine = lines.get(0);
            lastLine = lines.get(lines.size() - 1);
            indent = firstLine.indent;
            type = firstLine.type;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(">%d [%s]", indent, type.toString()));
        for (int i = 0; i < lines.size(); i++) {
            sb.append('\n')
              .append(i)
              .append(". ")
              .append(lines.get(i).toString());
        }
        return sb.append('\n').toString();
    }

    /**
     * @return 所有的行文本合并，'\n' 分隔
     */
    public String joinLines() {
        if (lines == null || lines.isEmpty())
            return "";
        Iterator<ZDocLine> it = lines.iterator();
        StringBuilder sb = new StringBuilder();
        ZDocLine line = it.next();
        sb.append(line.text);
        while (it.hasNext()) {
            line = it.next();
            if (line.type == ZDocLineType.BLANK)
                break;
            sb.append('\n').append(line.text);
        }
        return sb.toString();
    }

    /**
     * 截取一部分行
     * 
     * @param l
     *            左下标，0 表示从第一行截取
     * @param r
     *            右下标，0 表示截取到最后一行<br>
     *            如果小于 0，比如 -1 表示截取到倒数第二行<br>
     *            如果大于 0 表示行下标（不包含）
     * @return 行列表
     */
    public List<ZDocLine> sublines(int l, int r) {
        int maxOff = Math.min(lines.size(), r > 0 ? r : lines.size() + r);
        int sz = maxOff - l;
        if (sz <= 0)
            return new ArrayList<ZDocLine>(3);
        Iterator<ZDocLine> it = lines.iterator();
        int i = 0;
        while (i < l && it.hasNext()) {
            it.next();
            i++;
        }
        List<ZDocLine> list = new ArrayList<ZDocLine>(sz);
        while (it.hasNext() && i < maxOff) {
            list.add(it.next());
            i++;
        }
        return list;
    }

    /**
     * 将自身与另外一个文档块融合
     * 
     * @param block
     *            另外一个文档块
     * @return 自身
     */
    public ZDocBlock mergeWith(ZDocBlock block) {
        if (null != block) {
            for (ZDocLine line : block.lines) {
                _add(line);
                // 如果不是列表，则需要对齐 indent
                if (!line.isForList()) {
                    line.alignText(indent);
                }
            }
        }
        return this;
    }

    public ZDocBlock setText(String text) {
        ZDocLine line = new ZDocLine();
        line.indent = indent;
        line.type = ZDocLineType.PARAGRAPH;
        line.text = text;
        this.lines.clear();
        this.firstLine = line;
        this._add(line);
        return this;
    }

    /**
     * @param line
     *            行对象
     * @return 是否附加成功，如果不成功，调用者应该创建一个新块
     */
    public boolean appendLine(ZDocLine line) {
        // 空块一定可以添加
        if (lines.isEmpty() || type == BLANK) {
            firstLine = line;
            indent = line.indent;
            type = line.isForTable() ? TABLE : line.type;

            lines.clear();
            _add(line);

            // 如果是一个注释结尾块，无意义，变 block
            if (type == COMMENT_END) {
                type = PARAGRAPH;
                lastLine.type = PARAGRAPH;
                lastLine.text = lastLine.origin;
            }
            // 如果行是个注释开始，那么块变注释
            else if (type == COMMENT_BEGIN) {
                type = COMMENT;
            }
            return true;
        }
        // 如果本块是表格，那么无需考虑缩进，并且所加入的行均设为 TR
        // 直到遇到一个空行
        if (TABLE == type) {
            if (BLANK != line.type) {
                line.type = TR;
                _add(line);
                return true;
            } else {
                return false;
            }
        }
        // 如果当前行是空行，一定可以加入
        if (BLANK == line.type) {
            _add(line);
            return true;
        }
        // HR 一定不可以添加
        if (HR == type) {
            return false;
        }
        // 如果行是 TSEP 那么本块将变成表格，当前所有行均为 THEAD
        if (TSEP == line.type) {
            type = TABLE;
            for (ZDocLine zl : lines)
                zl.type = THEAD;
            _add(line);
            return true;
        }
        // 如果本身就是注释
        if (COMMENT == type) {
            // 首行就是完整注释，则不能再继续添加了
            if (COMMENT == firstLine.type)
                return false;
            // 那么首行必然是注释的开始，那么会一直添加到拥有了注释结尾
            if (COMMENT_END != lastLine.type) {
                _add(line.alignText(indent));
                return true;
            }
            return false;
        }
        // 如果最后一个是空行，那么就不能被添加
        if (BLANK == lastLine.type) {
            return false;
        }
        // 如果是 BLOCKQUOTE，那么也可以接受 PARAGRAPH
        if (BLOCKQUOTE == type) {
            if (type == line.type || line.type == PARAGRAPH) {
                _add(line);
                return true;
            }
            return false;
        }
        // 其他的，校验一下缩进和类型
        if (type == line.type && indent == line.indent) {
            _add(line);
            return true;
        }
        // 拒绝之 ~~~
        return false;
    }

    public ZDocBlock fixLines() {
        ArrayList<ZDocLine> list = new ArrayList<ZDocLine>(lines.size());
        list.addAll(lines);
        lines = list;
        return this;
    }

    public boolean isEmpty() {
        return lines.isEmpty() || BLANK == type;
    }

    private void _add(ZDocLine line) {
        lines.add(line.alignText(indent));
        lastLine = line;
    }
}
