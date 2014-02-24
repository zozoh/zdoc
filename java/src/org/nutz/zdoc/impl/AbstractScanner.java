package org.nutz.zdoc.impl;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZLine;
import org.nutz.zdoc.ZLinkInfo;

public abstract class AbstractScanner {

    protected abstract void scanWithException(Parsing ing) throws Exception;

    protected abstract ZLine evalLineType(Parsing ing, ZLine line);

    // 这里封装了行的连接逻辑
    protected String _read_line(Parsing ing) throws IOException {
        StringBuilder sb = new StringBuilder();
        String s = null;
        while (null != (s = ing.reader.readLine())) {
            // 记录到解析时的纯文字缓冲中，以备其他程序获取文档内容
            ing.raw.append(s).append('\n');
            // 逃逸的连接符号
            if (s.endsWith("\\\\")) {
                sb.append(s.substring(0, s.length() - 1));
            }
            // 要连接下一行
            else if (s.endsWith("\\")) {
                sb.append(s.substring(0, s.length() - 1));
                continue;
            }
            // 简单的获取一行就退出
            else {
                sb.append(s);
            }

            // ...........................................
            // 看看是否是链接定义
            Matcher m = Pattern.compile("^(\\[)([0-9a-zA-Z_. -]+)(\\]:[\t ]?)(.*)$")
                               .matcher(sb);
            if (m.find()) {
                String key = m.group(2);
                String sInfo = m.group(4);
                ZLinkInfo lInfo = new ZLinkInfo().parse(sInfo);
                ing.root.links().put(key, lInfo);

                sb = new StringBuilder();
                continue;
            }

            // 退出循环
            break;
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
