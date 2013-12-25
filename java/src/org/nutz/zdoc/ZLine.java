package org.nutz.zdoc;

import static org.nutz.zdoc.ZLineType.BLANK;
import static org.nutz.zdoc.ZLineType.BLOCKQUOTE;
import static org.nutz.zdoc.ZLineType.HR;
import static org.nutz.zdoc.ZLineType.OL;
import static org.nutz.zdoc.ZLineType.PARAGRAPH;
import static org.nutz.zdoc.ZLineType.UL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

public class ZLine {

    public String origin;

    public String text;

    public int indent;

    public int blockIndent;

    public ZLineType type;

    // OL 专用，可以是 '#','*',或者 0-9,A-Z,a-z
    public char itype;

    ZLine() {}

    public ZLine(String line) {
        origin = line;
        // 空白行，缩进无所谓
        if (Strings.isBlank(line)) {
            text = "";
            type = BLANK;
            return;
        }
        // 数数行的缩进
        int space = 0;
        int i = 0;
        for (; i < line.length(); i++) {
            char c = line.charAt(i);
            // 制表符
            if (c == '\t') {
                space += 4;
            }
            // 空白
            else if (Character.isWhitespace(c)) {
                space += 1;
            }
            // 其他
            else {
                break;
            }
        }
        indent = space / 4;
        // 得到文字
        text = line.substring(i);
        String trimText = this.trimmed();

        // 计算类型 ...
        // UL
        if (text.startsWith("* ")) {
            type = UL;
            text = text.substring(2);
        }
        // OL
        else if (text.startsWith("# ")) {
            itype = '#';
            type = OL;
            text = text.substring(2);
        }
        // OL，用数字或者字母作为标识
        else if (text.matches("^[0-9a-zA-Z]+[.][ ].+$")) {
            itype = text.charAt(0);
            type = OL;
            text = text.substring(text.indexOf('.') + 2);
        }
        // HR
        else if (trimText.matches("^[-]{4,}$")) {
            type = HR;
            text = null;
        }
        // BLOCKQUOTE
        else if (text.startsWith("> ")) {
            type = BLOCKQUOTE;
            Matcher m = Pattern.compile("^((>[ \t]+)+)(.*)$").matcher(text);
            if (m.find()) {
                blockIndent = m.group(1).replaceAll("[ \t]", "").length();
                text = m.group(3);
                _trim = null;
            } else {
                throw Lang.impossible();
            }
        }
        // PARAGRAPH
        else {
            type = PARAGRAPH;
        }

        // 如果是普通块，还原多余的缩进
        if (PARAGRAPH == type && indent * 4 != space) {
            text = Strings.dup(' ', space - indent * 4) + text;
        }

    }

    /**
     * 将本行的缩进值回退到给定缩进，同时减去或者补充对应的空格
     * 
     * @param indent
     *            期望的缩进
     * @return 自身
     */
    public ZLine alignText(int indent) {
        // 不需要调整
        if (this.indent == indent) {
            return this;
        }
        // 仅仅需要调整缩进值
        if (PARAGRAPH != this.type) {
            this.indent = indent;
            return this;
        }
        // 增加空格
        if (this.indent > indent) {
            this.text = Strings.dup(' ', (this.indent - indent) * 4) + text;
            this.indent = indent;
        }
        // 减少空格
        int n = (indent - this.indent) * 4;
        this.indent = indent;
        int i = 0;
        for (; i < text.length(); i++) {
            // 不需要减去空格了
            if (n <= 0)
                break;
            char c = text.charAt(i);
            // 制表符
            if (c == '\t') {
                n -= 4;
            }
            // 空白
            else if (Character.isWhitespace(c)) {
                n--;
            }
            // 其他
            else {
                break;
            }
        }
        text = text.substring(i);
        // 返回自身
        return this;
    }

    private String _trim;

    public String trimmed() {
        if (null == _trim) {
            _trim = Strings.sNull(Strings.trim(text), "");
        }
        return _trim;
    }

    private String _trim_lower;

    public String trimLower() {
        if (null == _trim_lower) {
            _trim_lower = trimmed().toLowerCase();
        }
        return _trim_lower;
    }

    public boolean isForList() {
        return UL == type || OL == type;
    }

    public String toString() {
        return String.format(">%d [%s]'%s'", indent, type, text);
    }
}
