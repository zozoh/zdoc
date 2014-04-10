package org.nutz.zdoc.impl.html;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Tag;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.vfs.ZFile;
import org.nutz.vfs.ZIO;
import org.nutz.zdoc.RenderTo;
import org.nutz.zdoc.Rendering;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;
import org.nutz.zdoc.ZDocIndex;
import org.nutz.zdoc.ZDocNode;
import org.nutz.zdoc.ZDocRule;
import org.nutz.zdoc.ZDocTag;
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
            public void invoke(final ZDocIndex zi) {
                final ZFile zf = zi.file();
                int depth = zi.depth();

                // 获得相对路径
                final String rph = zi.rpath();

                // 如果是目录，则忽略
                if (zf.isDir()) {
                    // 生成索引
                    if (depth == htmlIndexDepth) {
                        genIndexPage(ing, zi);
                    }

                    // TODO 这里是一个生成 README 的好地方
                    return;
                }

                // 目标输出文件的相对路径
                String oph = Files.renameSuffix(rph, ".html");
                String aph = Files.getParent(oph);
                if (aph.equals("/"))
                    aph = "";
                final String _aph = aph;

                log.info(" RENDER >> " + oph);

                // 将文档内所有相对链接(.zdoc|.man|.md|.markdown)都改一下扩展名
                // 并对 IMG 和 LINK 都给一个本文档的相对与根的路径
                // 这样，在渲染的时候，js 端比较容易获得这个路径，以便采用 ajax.load
                // 的方式组织页面的跳转
                if (null != zi.docRoot()) {
                    _normalize_links(ing, zi, zf, _aph);
                }

                // 创建页面全局上下文
                NutMap page = new NutMap();
                page.setv("bpath", zi.bpath());
                page.setv("title", zi.title());
                page.setv("depth", depth);
                page.setv("treeName", htmlIndexPath);
                page.setv("treeDepth", htmlIndexDepth);

                // 生成文档摘要
                ing.currentBasePath = "../";
                StringBuilder sb = new StringBuilder();
                // zDoc || markdown
                if (null != zi.docRoot()) {
                    joinBrief(sb, zi.docRoot(), ing);
                }
                // HTML
                else if (null != zi.rawTex()) {
                    sb.append(zi.rawTex()
                                .substring(0, ing.limit)
                                .replace("<", "&lt;")
                                .replace(">", "&gt;"));
                }
                // unknown
                else {
                    sb.append("???unknown content???");
                }
                zi.briefHtml(sb.toString());

                // 创建渲染上下文
                NutMap doc = zi.toMap();
                if (null != zi.docRoot()) {
                    for (String key : zi.docRoot().attrs().keys()) {
                        // tags 标签需要跳过，因为之前已经被处理成 ZDocTag 对象了
                        if ("tags".equals(key))
                            continue;
                        doc.put(key, zi.docRoot().attrs().get(key));
                    }
                }

                // 根据 zDoc 文档将其转换成 HTML 字符串
                ing.currentBasePath = zi.bpath();
                sb = new StringBuilder();
                // zDoc || markdown
                if (null != zi.docRoot()) {
                    joinDoc(sb, zi.docRoot(), ing);
                }
                // HTML
                else if (null != zi.rawTex()) {
                    sb.append(zi.rawTex());
                }
                // unknown
                else {
                    sb.append("???unknown content???");
                }
                doc.put("content", sb.toString());

                // 添加到上下文中
                ing.context().setv("doc", doc).setv("page", page);

                // 得到文档模板对象
                ZDocRule rule = checkRule(rph);
                ZDocTemplate tmpl = ing.tfa().getTemplte(rule.key());

                // 在目标目录创建对应文件
                ZFile destf = dest.createFileIfNoExists(oph);

                // 准备渲染
                Writer wr = Streams.utf8w(ing.io().openOutputStream(destf));
                tmpl.outputTo(wr, ing.context());
                Streams.safeClose(wr);
            }
        });
        // 拷贝资源
        copyResources(ing);
        // 生成标签页面
        genTagPages(ing);

    }

    @SuppressWarnings("unchecked")
    private void genTagPages(Rendering ing) {
        if (!Strings.isBlank(tagPath)) {
            log.infof("make tags >> %s/*", tagPath);
            // 得到文档模板对象
            ZDocRule rule = checkRule(tagPath);
            ZDocTemplate tmpl = ing.tfa().getTemplte(rule.key());

            NutMap page = new NutMap();
            page.setv("bpath", "../");
            ing.context().setv("page", page);
            Map<String, ZDocTag> tags = ing.context().getAs("tags", Map.class);
            for (ZDocTag tag : tags.values()) {
                _gen_tag_page(ing, tmpl, page, tag);
            }
            // 生成 others Tag
            _gen_tag_page(ing, tmpl, page, othersTag);
        } else {
            log.info("! Ignore tags");
        }
    }

    private void _gen_tag_page(Rendering ing,
                               ZDocTemplate tmpl,
                               NutMap page,
                               ZDocTag tag) {
        page.setv("title", tag.getText());
        ing.context().setv("tag", tag.genItems());

        // 在目标目录创建对应文件
        ZFile destf = dest.createFileIfNoExists(tagPath
                                                + "/"
                                                + tag.getKey()
                                                + ".html");
        // 准备渲染
        Writer wr = Streams.utf8w(ing.io().openOutputStream(destf));
        tmpl.outputTo(wr, ing.context());
        Streams.safeClose(wr);
    }

    private void copyResources(final Rendering ing) {
        log.info("copy resources ...");
        for (ZFile rs : rss) {
            String rph = src.relative(rs);
            log.info(" copy ++ " + rph);
            ZFile zfDest = rs.isDir() ? dest.createDirIfNoExists(rph)
                                     : dest.createFileIfNoExists(rph);
            rs.copyTo(ing.io(), ing.io(), zfDest);
        }
        log.info("copy images ...");
        for (Map.Entry<String, ZFile> en : ing.medias.entrySet()) {
            String destPath = en.getKey();
            ZFile imgf = en.getValue();
            log.info(" ++ " + destPath);
            ZFile zfDest = dest.createFileIfNoExists(destPath);
            imgf.copyTo(ing.io(), ing.io(), zfDest);
        }
    }

    private void genIndexPage(final Rendering ing, final ZDocIndex base) {
        if (!Strings.isBlank(this.htmlIndexPath)) {
            String oph = base.rpath() + "/" + htmlIndexPath;

            log.infof("++++++++++++++++ make index : %s", oph);

            final Tag tag = Tag.tag("ol", ".zdoc-index-container");
            base.eachChild(new Each<ZDocIndex>() {
                public void invoke(int index, ZDocIndex zi, int length) {
                    if (zi.file().name().startsWith("README."))
                        return;
                    joinIndexTag(tag, base, zi);
                }
            });

            ing.context().remove("doc");
            ing.context().put("siteIndexes", tag.toString());

            // 创建页面
            NutMap page = new NutMap();
            page.setv("bpath", base.bpath());
            page.setv("title", base.title());
            page.setv("depth", base.depth());
            ing.context().setv("page", page);

            // 得到文档模板对象
            ZDocRule rule = checkRule(htmlIndexPath);
            ZDocTemplate tmpl = ing.tfa().getTemplte(rule.key());

            // 在目标目录创建对应文件
            ZFile destf = dest.createFileIfNoExists(oph);

            // 准备渲染
            Writer wr = Streams.utf8w(ing.io().openOutputStream(destf));
            tmpl.outputTo(wr, ing.context());
            Streams.safeClose(wr);

        } else {
            log.info("! Ignore index");
        }
    }

    private void joinIndexTag(Tag p, final ZDocIndex base, ZDocIndex zi) {
        // 如果还有子节点
        if (zi.hasChild()) {
            Tag tag = Tag.tag("li", ".zdoc-index-node");
            // 生成名称
            Tag b = Tag.tag("b").setText(zi.title());
            // 遍历子节点
            final Tag sub = Tag.tag("ol", ".zdoc-index-wrapper");
            zi.eachChild(new Each<ZDocIndex>() {
                public void invoke(int index, ZDocIndex child, int length) {
                    if (child.file().name().startsWith("README."))
                        return;
                    joinIndexTag(sub, base, child);
                }
            });
            // 加入到树中
            tag.parent(p).add(b, sub);
        }
        // 叶子节点
        else {
            Tag tag = Tag.tag("li", ".doc-index-item");

            // 获得相对路径
            String rph = zi.rpath(base);
            String oph = Files.renameSuffix(rph, ".html");

            // 生成链接标签
            Tag a = Tag.tag("a").attr("href", oph).setText(zi.title());

            // 加入到树中
            tag.parent(p).add(a);

        }
    }

    private void joinBrief(StringBuilder sb, ZDocNode root, Rendering ing) {
        ing.charCount = 0;
        ing.limit = briefLimit;
        List<ZDocNode> children = root.children();
        if (null == children || children.isEmpty()) {
            sb.append(root.text());
        } else {
            ZDocNode2Html nd2html = new ZDocNode2Html();
            for (ZDocNode nd : root.children()) {
                nd2html.joinNode(sb, nd, ing);
                if (ing.isOutOfLimit())
                    break;
            }
        }
    }

    private void joinDoc(StringBuilder sb, ZDocNode root, Rendering ing) {
        ing.charCount = 0;
        ing.limit = 0;
        ZDocNode2Html nd2html = new ZDocNode2Html();
        for (ZDocNode nd : root.children()) {
            nd2html.joinNode(sb, nd, ing);
            if (ing.isOutOfLimit())
                break;
        }
    }

    void _normalize_links(final Rendering ing,
                          final ZDocIndex zi,
                          final ZFile zf,
                          final String _aph) {
        zi.docRoot().walk(new Callback<ZDocNode>() {
            public void invoke(ZDocNode nd) {
                for (ZDocEle ele : nd.eles()) {
                    // IMG
                    if (ZDocEleType.IMG == ele.type()) {
                        ele.attr("apath", _aph);
                        String src = ele.linkInfoString("src");
                        if (!src.startsWith("http://")) {
                            ZFile imgf = zf.parent().get(src);
                            if (null == imgf) {
                                log.warnf("  !!! img no found '%s'", src);
                            } else {
                                String oph = _aph + "/" + src;
                                ing.medias.put(oph, imgf);
                            }
                        }
                    }
                    // LINK
                    else {
                        String href = ele.href();
                        if (Strings.isBlank(href))
                            continue;

                        href = href.toLowerCase();
                        if (href.matches("^[a-z]+://.*$"))
                            continue;

                        ele.attr("apath", _aph);
                        if (href.matches("^(.*)(.zdoc|.man|.md|.markdown)$")) {
                            href = Files.renameSuffix(href, ".html");
                            ele.href(href);
                        }
                    }
                }
            }
        });
    }

}