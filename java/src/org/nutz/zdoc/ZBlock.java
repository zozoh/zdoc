package org.nutz.zdoc;

import static org.nutz.zdoc.ZLineType.BLANK;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ZBlock {

    public List<ZLine> lines;

    public ZLine firstLine;

    public ZLine lastLine;

    public int indent;

    public ZLineType type;

    public String codeType;

    public ZBlock() {
        this.lines = new LinkedList<ZLine>();
    }

    public ZBlock(List<ZLine> lines) {
        this.lines = lines;
        if (!lines.isEmpty()) {
            firstLine = lines.get(0);
            lastLine = lines.get(lines.size() - 1);
            indent = firstLine.indent;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("BLOCK(%s):indent=%d", type, indent));
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
        return joinLines(indent, lines);
    }

    public static String joinLines(int indent, List<ZLine> lines) {
        if (lines == null || lines.isEmpty())
            return "";
        Iterator<ZLine> it = lines.iterator();
        StringBuilder sb = new StringBuilder();
        ZLine line = it.next();
        sb.append(line.text());
        while (it.hasNext()) {
            line = it.next();
            if (line.type == ZLineType.BLANK)
                break;
            line.alignText(indent);
            sb.append('\n').append(line.text());
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
    public List<ZLine> sublines(int l, int r) {
        int maxOff = Math.min(lines.size(), r > 0 ? r : lines.size() + r);
        int sz = maxOff - l;
        if (sz <= 0)
            return new ArrayList<ZLine>(3);
        Iterator<ZLine> it = lines.iterator();
        int i = 0;
        while (i < l && it.hasNext()) {
            it.next();
            i++;
        }
        List<ZLine> list = new ArrayList<ZLine>(sz);
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
    public ZBlock mergeWith(ZBlock block) {
        if (null != block) {
            for (ZLine line : block.lines) {
                _add(line);
                // 如果不是列表，则需要对齐 indent
                if (!line.isForList()) {
                    line.alignText(indent);
                }
            }
        }
        return this;
    }

    public ZBlock setText(String text) {
        ZLine line = new ZLine();
        line.indent = indent;
        line.type = ZLineType.PARAGRAPH;
        line.text(text);
        this.lines.clear();
        this.firstLine = line;
        this._add(line);
        return this;
    }

    public ZBlock fixLines() {
        ArrayList<ZLine> list = new ArrayList<ZLine>(lines.size());
        list.addAll(lines);
        lines = list;
        return this;
    }

    public boolean isEmpty() {
        for (ZLine line : lines)
            if (line.type != BLANK)
                return false;
        return true;
    }

    public void _add(ZLine line) {
        if (lines.isEmpty()) {
            firstLine = line;
        }
        lines.add(line.alignText(indent));
        lastLine = line;
    }
}
