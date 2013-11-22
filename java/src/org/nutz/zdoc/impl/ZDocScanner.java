package org.nutz.zdoc.impl;

import java.io.IOException;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZDocBlock;
import org.nutz.zdoc.ZDocLine;

public class ZDocScanner {

    private void scanWithException(Parsing ing) throws Exception {
        ZDocBlock block;
        ZDocLine line;
        String str;
        // 读第一行
        if (null != (str = _read_line(ing))) {
            line = new ZDocLine(str);
            block = new ZDocBlock();
            block.appendLine(line);
        }
        // 一行都木有，退出吧
        else {
            return;
        }
        // 读取之后的各行
        while (null != (str = _read_line(ing))) {
            ZDocLine current = new ZDocLine(str);
            // 不能加入当前块，那么生成一个新块吧
            if (!block.appendLine(current)) {
                ing.blocks.add(block.fixLines());
                block = new ZDocBlock();
                block.appendLine(current);
            }
        }
        // 最后一块
        if (!block.isEmpty())
            ing.blocks.add(block.fixLines());
    }

    // 这里封装了行的连接逻辑
    private String _read_line(Parsing ing) throws IOException {
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
