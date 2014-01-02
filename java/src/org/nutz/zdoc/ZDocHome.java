package org.nutz.zdoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nutz.am.AmFactory;
import org.nutz.cache.ZCache;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.MultiLineProperties;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.vfs.ZDir;
import org.nutz.vfs.ZFWalker;
import org.nutz.vfs.ZFile;
import org.nutz.vfs.ZIO;
import org.nutz.zdoc.impl.ZDocParser;
import org.nutz.zdoc.util.ZD;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ZDocHome {

    private static final Log log = Logs.get();

    private ZIO io;

    protected ZDir src;

    protected ZCache<ZDocTmplObj> libs;

    protected ZCache<ZDocTmplObj> tmpl;

    protected List<ZFile> rss;

    protected Context vars;

    protected List<ZDocRule> rules;

    protected ZDocIndex index;

    protected String htmlIndexPath;

    // 顶层目录都有哪些文件和目录不要扫描的
    private Set<String> topIgnores;

    public ZDocHome(ZIO io) {
        this.io = io;
        this.libs = new ZCache<ZDocTmplObj>();
        this.tmpl = new ZCache<ZDocTmplObj>();
        this.rss = new ArrayList<ZFile>();
        this.topIgnores = new HashSet<String>();
        this.vars = Lang.context();
        this.rules = new ArrayList<ZDocRule>();
        this.index = new ZDocIndex();
    }

    public ZDocHome clear() {
        libs.clear();
        tmpl.clear();
        vars.clear();
        rules.clear();
        index.clear();
        topIgnores.clear();
        return this;
    }

    public ZDir src() {
        return src;
    }

    public ZDocHome src(ZDir dir) {
        if (null != dir) {
            src = dir;
            log.infof("home @ %s", src.path());
            init(src.getFile("zdoc.conf"));
        }
        return this;
    }

    public ZDocHome init(ZFile fconf) {
        // 清除
        log.info("clear caches");
        clear();

        // 如果有配置就分析一下 ...
        if (null != fconf) {
            log.infof("read conf : %s", fconf.name());

            topIgnores.add(fconf.name());

            // 读取配置文件
            MultiLineProperties pp = _read_zdoc_conf(fconf);

            // 开始分析 ...
            _set_cache_item(tmpl, pp.get("zdoc-tmpl"));
            _set_cache_item(libs, pp.get("zdoc-libs"));
            _read_rules(pp);
            _read_rss(pp);
            _read_vars(pp);

            // 读取其他配置字段
            this.htmlIndexPath = Strings.sBlank(pp.get("zdoc-html-index-path"),
                                                null);

        }
        // 没有配置文件，则试图给个默认值
        else {
            _set_cache_item(tmpl, "_tmpl");
            _set_cache_item(libs, "_libs");
            for (String nm : "imgs,js,css".split(",")) {
                if (src.existsDir(nm)) {
                    rss.add(src.getDir(nm));
                    topIgnores.add(nm);
                }
            }
        }

        // 解析索引
        ZFile indexml = src.getFile("index.xml");
        // 根据原生目录结构
        if (null == indexml) {
            _read_index_by_native();
        }
        // 根据给定的 XML 文件
        else {
            _read_index_by_xml(indexml);
        }
        // 开始逐个分析文档
        log.info("walking docs ...");

        // 准备解析器
        final Parser paZDoc = new ZDocParser();
        final AmFactory fa_zdoc = new AmFactory("org/nutz/zdoc/am/zdoc.js");

        // 开始遍历，解析每个文件
        index.walk(new Callback<ZDocIndex>() {
            @SuppressWarnings("unchecked")
            public void invoke(ZDocIndex zi) {
                ZFile zf = zi.file();
                if (!zf.isFile())
                    return;
                String rph = src.relative(zf);

                zi.rpath(rph);
                zi.bpath(Strings.dup("../", zi.depth() - 1));
                zi.lm(Times.D(zf.lastModified()));

                // ZDoc
                if (zf.matchType("^zdoc|man$")) {
                    log.infof("zdoc: %s", rph);
                    Parsing ing = new Parsing(io.readString(zf));
                    ing.fa = fa_zdoc;
                    paZDoc.build(ing);
                    ing.root.normalizeChildren();
                    zi.docRoot(ing.root).rawTex(ing.raw);
                }
                // Markdown
                else if (zf.matchType("^md|markdown$")) {
                    log.infof("md: %s", rph);
                    throw Lang.noImplement();
                }
                // HTML
                else if (zf.matchType("^html?$")) {
                    log.infof("html: %s", rph);
                    String html = Streams.readAndClose(io.readString(zf));
                    zi.rawTex(html);
                }

                // 填充索引的关键属性
                if (null != zi.docRoot()) {
                    ZDocAttrs attrs = zi.docRoot().attrs();
                    zi.updateAuthors(attrs.getList(ZDocAuthor.class, "author"));
                    zi.updateVerifier(attrs.getList(ZDocAuthor.class,
                                                    "verifier"));
                    zi.title(attrs.getString("title", zi.title()));
                    Object tags = attrs.get("tags");
                    if (null != tags) {
                        if (tags instanceof List) {
                            zi.tags((List<String>) tags);
                        } else {
                            throw Lang.impossible();
                        }
                    }
                    zi.lm(attrs.getAs(Date.class, "lm", zi.lm()));
                }
            }
        });

        // 返回自身
        return this;
    }

    public ZCache<ZDocTmplObj> libs() {
        return libs;
    }

    public ZCache<ZDocTmplObj> tmpl() {
        return tmpl;
    }

    public Context vars() {
        return vars;
    }

    public List<ZDocRule> rules() {
        return rules;
    }

    public ZDocRule checkRule(String rph) {
        for (ZDocRule rule : rules)
            if (rule.match(rph))
                return rule;
        throw Lang.makeThrow("Fail to found rule for '%s'", rph);
    }

    public ZDocIndex index() {
        return index;
    }

    private void _read_index_by_native() {
        for (ZFile topf : src.ls(null, true)) {
            // 忽略第一层特殊的目录
            if (topIgnores.contains(topf.name()))
                continue;
            // 目录或者特殊的文件类型会被纳入索引
            if (topf.isDir() || topf.matchType("^zdoc|man|md|markdown|html?$")) {
                ZDocIndex topzi = new ZDocIndex().parent(index);
                _read_index_by_native(topf, topzi);
            }
        }
        _read_index_by_native(src, index);
    }

    private void _read_index_by_native(ZFile zf, ZDocIndex zi) {
        zi.file(zf);
        if (zf.isDir()) {
            for (ZFile subf : ((ZDir) zf).ls(null, true)) {
                // 目录或者特殊的文件类型会被纳入索引
                if (subf.isDir()
                    || subf.matchType("^zdoc|man|md|markdown|html?$")) {
                    ZDocIndex subzi = new ZDocIndex().parent(zi);
                    _read_index_by_native(subf, subzi);
                }
            }
        }
    }

    private void _read_index_by_xml(ZFile indexml) {
        try {
            Document doc = Lang.xmls().parse(io.read(indexml));
            Element root = doc.getDocumentElement();
            _read_index_by_XmlElement(src, root, index);
        }
        catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
    }

    private void _read_index_by_XmlElement(ZFile zf, Element ele, ZDocIndex zi) {
        // 设置自身的值
        zi.file(zf);
        zi.author(Xmls.getAttr(ele, "author"));
        zi.title(Xmls.getAttr(ele, "title"));

        // 判断是否有子
        List<Element> subeles = Xmls.children(ele, "doc");
        if (!subeles.isEmpty() && !zf.isDir()) {
            throw Lang.makeThrow("'%s' should be a DIR!", zf.path());
        }

        // 循环子节点
        for (Element subele : subeles) {
            ZDocIndex subzi = new ZDocIndex();
            subzi.parent(zi);
            subzi.path(Xmls.getAttr(subele, "path"));
            ZFile subf = ((ZDir) zf).check(subzi.path());
            _read_index_by_XmlElement(subf, subele, subzi);
        }
    }

    private void _read_rss(MultiLineProperties pp) {
        String[] ss = Strings.splitIgnoreBlank(pp.get("zdoc-rs"), ",\n");
        for (String s : ss) {
            ZFile d = src.get(s);
            if (null != d) {
                topIgnores.add(s);
                rss.add(d);
            }
        }
    }

    private void _read_vars(MultiLineProperties pp) {
        String[] ss = Strings.splitIgnoreBlank(pp.get("zdoc-vars"), "\n");
        for (String s : ss) {
            int pos = s.indexOf('=');
            String varName = s.substring(0, pos).trim();
            String valValue = s.substring(pos + 1).trim();
            vars.set(varName, valValue);
        }
    }

    private void _read_rules(MultiLineProperties pp) {
        String[] ss = Strings.splitIgnoreBlank(pp.get("zdoc-rules"), "\n");
        for (String s : ss) {
            int pos = s.lastIndexOf(':');
            String regex = s.substring(0, pos).trim();
            String key = s.substring(pos + 1).trim();
            ZDocRule rule = new ZDocRule();
            rules.add(rule.key(key).regex(regex));
        }
    }

    private MultiLineProperties _read_zdoc_conf(ZFile fconf) {
        BufferedReader br = Streams.buffr(io.readString(fconf));
        MultiLineProperties pp = new MultiLineProperties();
        try {
            pp.load(br);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(br);
        }
        return pp;
    }

    private void _set_cache_item(final ZCache<ZDocTmplObj> zc, String fname) {
        if (!Strings.isBlank(fname)) {
            topIgnores.add(fname);
            final ZDir d = src.getDir(fname);
            if (null != d) {
                d.walk(true, new ZFWalker() {
                    public boolean invoke(int i, ZFile f) {
                        if (f.isDir())
                            return true;
                        if (f.isHidden())
                            return false;
                        ZDocTmplObj ci = new ZDocTmplObj();
                        ci.file(f).raw(Streams.readAndClose(io.readString(f)));
                        String rph = d.relative(f);
                        String key = ZD.rph2key(rph);
                        zc.set(key, ci);
                        return true;
                    }
                });
            }
        }
    }
}
