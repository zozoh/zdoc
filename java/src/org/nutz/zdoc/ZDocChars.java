package org.nutz.zdoc;

import java.util.regex.Pattern;

public class ZDocChars {

    public char[] chars;

    public int left;

    public int off;

    public int len;

    /**
     * 设置自己代理的字符串数组
     * 
     * @param cs
     *            字符串数组
     * @param off
     *            偏移
     * @param len
     *            0 表从 off 开始的示全部，<0 表示从右侧减去多少字符
     * @return 自身
     */
    public ZDocChars set(char[] cs, int off, int len) {
        this.chars = cs;
        this.off = Math.max(0, off);
        return this.len(len);
    }

    public ZDocChars set(char[] cs) {
        return set(cs, 0, 0);
    }

    public ZDocChars set(String str) {
        return set(str.toCharArray());
    }

    /**
     * 修改长度
     * 
     * @param len
     *            0 表从 off 开始的示全部，<0 表示从右侧减去多少字符
     * @return 自身
     */
    public ZDocChars len(int len) {
        int maxLen = Math.max(0, chars.length - this.off);
        if (maxLen > 0) {
            if (len == 0) {
                this.len = maxLen;
            } else if (len > 0) {
                this.len = Math.min(len, maxLen);
            } else {
                this.len = Math.max(1, maxLen + len);
            }
        } else {
            this.len = 0;
        }
        return this;
    }

    /**
     * 让自己表示的字符范围转移到下一个
     * 
     * @return 自身，如果没有更多字符了返回 null
     */
    public ZDocChars next() {
        if (set(chars, off + len, 0).len == 0) {
            return null;
        }
        return this;
    }

    /**
     * @param callback
     *            回调
     * @return 一共遍历了多少个字符
     */
    public int each(EachChar callback) {
        int i = 0;
        for (; i < len; i++) {
            if (callback.isBreak(i, chars[off + i])) {
                break;
            }
        }
        return i + 1;
    }

    public boolean startsIs(char c) {
        return len > 0 && chars[off] == c;
    }

    public boolean endsIs(char c) {
        return len > 0 && chars[off + len - 1] == c;
    }

    public boolean startsWith(char... cs) {
        if (cs.length > len)
            return false;
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] != chars[off + i])
                return false;
        }
        return true;
    }

    /**
     * @return 是否与给定字符数组一致
     */
    public boolean matchIgnoreCase(char... cs) {
        if (cs.length != len)
            return false;
        for (int i = 0; i < len; i++) {
            if (Character.toUpperCase(cs[i]) != Character.toUpperCase(chars[off
                                                                            + i]))
                return false;
        }
        return true;
    }

    /**
     * @return 是否与给定字符数组一致
     */
    public boolean match(char... cs) {
        if (cs.length != len)
            return false;
        for (int i = 0; i < len; i++) {
            if (cs[i] != chars[off + i])
                return false;
        }
        return true;
    }

    public boolean match(Pattern p) {
        return p.matcher(str()).find();
    }

    public String str() {
        return str(0, 0);
    }

    /**
     * 切取字符串
     * 
     * @param l
     *            必须 >=0，表示左侧下标的位移
     * @param r
     *            必须 <=0，表示右侧下标的位移
     * @return 切去后的字符串
     */
    public String str(int l, int r) {
        return new String(chars, off + l, (len + r - l));
    }

}
