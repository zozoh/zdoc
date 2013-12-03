package org.nutz.zdoc.impl;

import java.io.IOException;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZLine;
import org.nutz.zdoc.ZLineType;

public abstract class AbstractScanner {

    protected abstract void scanWithException(Parsing ing) throws Exception;

    protected abstract ZLineType evalLineType(ZLine line);

    // 这里封装了行的连接逻辑
    protected String _read_line(Parsing ing) throws IOException {
        StringBuilder sb = new StringBuilder();
        String s = null;
        while (null != (s = ing.reader.readLine())) {
            // 记录到解析时的纯文字缓冲中，以备其他程序获取文档内容
            ing.raw.append(s).append('\n');
            // 逃逸的连接符号
            if (s.endsWith("\\\\"))
                return s.substring(0, s.length() - 1);
            // 要连接下一行
            if (s.endsWith("\\")) {
                sb.append(s.substring(0, s.length() - 1));
                continue;
            }
            // 简单的获取一行就退出
            else {
                sb.append(s);
                break;
            }
        }
        return sb.length() == 0 && s == null ? null : sb.toString();
    }

    public void scan(Parsing ing) {
        try {
            scanWithException(ing);
        }
        catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(ing.reader);
        }
    }

}
