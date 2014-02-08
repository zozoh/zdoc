package org.nutz.zdoc.impl.html;

import java.io.Writer;
import java.util.List;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Files;
import org.nutz.lang.LoopException;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Tag;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.vfs.ZDir;
import org.nutz.vfs.ZFile;
import org.nutz.vfs.ZIO;
import org.nutz.zdoc.RenderTo;
import org.nutz.zdoc.Rendering;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocIndex;
import org.nutz.zdoc.ZDocNode;
import org.nutz.zdoc.ZDocRule;
import org.nutz.zdoc.ZDocTemplate;

public class RenderToHtml extends RenderTo {

    private static final Log log = Logs.get();

    public RenderToHtml(ZIO io) {
        super(io);
    }

    @Override
    public void render(final Rendering ing) {
        // 转换文档
        log.info("walk docs ...");
        index.walk(new Callback<ZDocIndex>() {
            public void invoke(ZDocIndex zi) {
                ZFile zf = zi.file();
                // 获得相对路径
                String rph = src.relative(zf);

                // 如果是目录，则 copy 全部的资源文件
                if (zf.isDir()) {
                    ZDir zdir = (ZDir) zf;
                    List<ZFile> imgs = zdir.lsFile("[.](png|gif|jpg|jpeg)",
                                                   true);
                    if (!imgs.isEmpty()) {
                        log.infof("copy all %d images under : %s",
                                  imgs.size(),
                                  rph);

                        for (ZFile img : imgs) {
                            String imgrph = src.relative(img);
                            log.info(" ++ " + imgrph);
                            ZFile destImg = dest.createFileIfNoExists(imgrph);
                            img.copyTo(ing.io(), ing.io(), destImg);
                        }
                    }
                    return;
                }

                // 目标输出文件的相对路径
                String oph = Files.renameSuffix(rph, ".html");

                log.info(" RENDER >> " + oph);

                // 将文档内所有相对链接(.zdoc|.man|.md|.markdown)都改一下扩展名
                zi.docRoot().walk(new Callback<ZDocNode>() {
                    public void invoke(ZDocNode nd) {
                        for (ZDocEle ele : nd.eles()) {
                            String href = ele.href();
                            if (Strings.isBlank(href))
                                continue;
                            if (href.startsWith("http://")
                                || href.startsWith("https://"))
                                continue;
                            if (href.toLowerCase()
                                    .matches("^(.*)(.zdoc|.man|.md|.markdown)$")) {
                                href = Files.renameSuffix(href, ".html");
                                ele.href(href);
                            }
                        }
                    }
                });

                // 创建渲染上下文
                NutMap map = new NutMap();
                if (null != zi.docRoot()) {
                    for (String key : zi.docRoot().attrs().keys()) {
                        map.put(key, zi.docRoot().attrs().get(key));
                    }
                }
                map.put("author", zi.authors());
                map.put("verifier", zi.verifiers());
                map.put("title", zi.title());
                map.put("tags", zi.tags());
                map.put("lm", zi.lm());
                map.put("rpath", zi.rpath());
                map.put("bpath", zi.bpath());
                ing.context().put("doc", map);

                // 根据 zDoc 文档将其转换成 HTML 字符串
                StringBuilder sb = new StringBuilder();
                joinDoc(sb, zi.docRoot(), ing);

                map.put("content", sb.toString());

                // 得到文档模板对象
                ZDocRule rule = checkRule(rph);
                ZDocTemplate tmpl = ing.tfa().getTemplte("tmpl:" + rule.key());

                // 在目标目录创建对应文件
                ZFile destf = dest.createFileIfNoExists(oph);

                // 准备渲染
                Writer wr = Streams.buffw(ing.io().openWriter(destf));
                tmpl.outputTo(wr, ing.context());
                Streams.safeClose(wr);
            }
        });
        // 拷贝资源
        log.info("copy resources ...");
        for (ZFile rs : rss) {
            String rph = src.relative(rs);
            log.info(" copy ++ " + rph);
            ZFile zfDest = rs.isDir() ? dest.createDirIfNoExists(rph)
                                     : dest.createFileIfNoExists(rph);
            rs.copyTo(ing.io(), ing.io(), zfDest);
        }
        // 生成索引
        if (null != this.htmlIndexPath) {
            log.info("make index ...");

            Tag tag = Tag.tag("ol", ".zdoc-index-container");
            joinIndexTag(tag, index);

            ing.context().remove("doc");
            ing.context().put("docIndexes", tag.toString());

            // 得到文档模板对象
            ZDocRule rule = checkRule(htmlIndexPath);
            ZDocTemplate tmpl = ing.tfa().getTemplte("tmpl:" + rule.key());

            // 在目标目录创建对应文件
            ZFile destf = dest.createFileIfNoExists(htmlIndexPath);

            // 准备渲染
            Writer wr = Streams.buffw(ing.io().openWriter(destf));
            tmpl.outputTo(wr, ing.context());
            Streams.safeClose(wr);

        } else {
            log.info("! NO index");
        }
    }

    private void joinIndexTag(Tag p, ZDocIndex zi) {
        // 如果还有子节点
        if (zi.hasChild()) {
            Tag tag = Tag.tag("li", ".doc-index-node");
            // 生成名称
            Tag b = Tag.tag("b").setText(zi.title());
            // 遍历子节点
            final Tag sub = Tag.tag("ol", ".doc-index-sub");
            zi.eachChild(new Each<ZDocIndex>() {
                public void invoke(int index, ZDocIndex child, int length) {
                    joinIndexTag(sub, child);
                }
            });
            // 加入到树中
            tag.parent(p).add(b, sub);
        }
        // 叶子节点
        else {
            Tag tag = Tag.tag("li", ".doc-index-item");

            // 获得相对路径
            ZFile zf = zi.file();
            String rph = src.relative(zf);
            String oph = Files.renameSuffix(rph, ".html");

            // 生成链接标签
            Tag a = Tag.tag("a").attr("href", oph).setText(zi.title());

            // 加入到树中
            tag.parent(p).add(a);

        }
    }

    private void joinDoc(StringBuilder sb, ZDocNode root, Rendering ing) {
        ZDocNode2Html nd2html = new ZDocNode2Html();
        for (ZDocNode nd : root.children()) {
            nd2html.joinNode(sb, nd, ing);
        }
    }

}