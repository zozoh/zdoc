package org.nutz.zdoc.impl.html;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback2;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.vfs.ZFile;
import org.nutz.vfs.ZIO;
import org.nutz.zdoc.RenderTo;
import org.nutz.zdoc.ZDocIndex;
import org.nutz.zdoc.ZDocNode;

public class RenderToHtml extends RenderTo {

    private static final Log log = Logs.get();

    public RenderToHtml(ZIO io) {
        super(io);
    }

    @Override
    public void render(final ZIO io) {
        index.walk(new Callback2<ZDocIndex, ZFile>() {
            public void invoke(ZDocIndex zi, ZFile zf) {
                // 忽略目录
                if (zf.isDir())
                    return;

                // 获得相对路径
                String rph = src.relative(zf);
                String oph = Files.renameSuffix(rph, ".html");

                log.info(" >> " + oph);

                // 根据 zDoc 文档将其转换成 HTML 字符串
                StringBuilder sb = new StringBuilder();
                joinDoc(sb, zi, zf);

                // 在目标目录创建对应文件
                ZFile destf = dest.createFileIfNoExists(oph);

                // 写入文件
                io.writeString(destf, Lang.inr(sb));
            }
        });
    }

    private void joinDoc(StringBuilder sb, ZDocIndex zi, ZFile zf) {
        ZDocNode root = zi.docRoot();
        ZDocNode2Html nd2html = new ZDocNode2Html();
        for (ZDocNode nd : root.children()) {
            nd2html.joinNode(sb, nd);
        }
    }

}