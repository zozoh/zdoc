package org.nutz.zdoc;

import static org.nutz.zdoc.ZLineType.*;

import org.nutz.lang.Strings;

public class ZLine {

    public String origin;

    private String text;

    public int indent;

    public int blockLevel;

    public ZLineType type;

    public int space;

    // OL 专用，可以是 '#','*',或者 0-9,A-Z,a-z
    public char itype;

    ZLine() {}

    public ZLine(String str) {
        origin = str;
        text = str;
    }

    public ZLine type(ZLineType type) {
        this.type = type;
        return this;
    }

    public ZLine text(String str) {
        this.text = str;
        this._trim = null;
        this._trim_lower = null;
        return this;
    }

    public String text() {
        return this.text;
    }

    public ZLine compensateSpace() {
        if (indent * 4 != space) {
            text = Strings.dup(' ', space - indent * 4) + text;
        }
        return this;
    }

    public ZLine evalIndent() {
        // 非空白行才要计算缩进
        if (!Strings.isBlank(origin)) {
            // 数数行的缩进
            space = 0;
            int i = 0;
            for (; i < origin.length(); i++) {
                char c = origin.charAt(i);
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
            text = origin.substring(i);
        }
        // 空白行，缩进无所谓
        else {
            text = "";
        }

        return this;
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
