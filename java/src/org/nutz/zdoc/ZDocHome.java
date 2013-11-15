package org.nutz.zdoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nutz.cache.ZCache;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.MultiLineProperties;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.vfs.ZDir;
import org.nutz.vfs.ZFWalker;
import org.nutz.vfs.ZFile;
import org.nutz.vfs.ZIO;
import org.nutz.zdoc.impl.ZDocParser;

public class ZDocHome {

    private static final Log log = Logs.get();

    private ZIO io;

    private ZDir src;

    private ZCache<ZDocHtmlCacheItem> libs;

    private ZCache<ZDocHtmlCacheItem> tmpl;

    private ZCache<ZDocNode> docs;

    private ZCache<String> htmls;

    private List<ZDir> rss;

    private Context vars;

    private List<ZDocRule> rules;

    private ZDocIndex indexes;

    // 顶层目录都有哪些文件和目录不要扫描的
    private Set<String> topIgnores;

    public ZDocHome(ZIO io) {
        this.io = io;
        this.libs = new ZCache<ZDocHtmlCacheItem>();
        this.tmpl = new ZCache<ZDocHtmlCacheItem>();
        this.docs = new ZCache<ZDocNode>();
        this.rss = new ArrayList<ZDir>();
        this.topIgnores = new HashSet<String>();
        this.vars = Lang.context();
        this.rules = new ArrayList<ZDocRule>();
        this.indexes = new ZDocIndex();
    }

    public ZDocHome clear() {
        libs.clear();
        tmpl.clear();
        docs.clear();
        htmls.clear();
        vars.clear();
        rules.clear();
        topIgnores.clear();
        return this;
    }

    public ZDir src() {
        return src;
    }

    public ZDocHome src(ZDir dir) {
        log.infof("home @ %s", src.path());
        src = dir;
        return init(src.getFile("zdoc.conf"));
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
        }
        // 开始逐个分析文档
        log.info("walking docs ...");

        // 准备解析器
        final Parser paZDoc = new ZDocParser();

        // 定义遍历器
        ZFWalker walker = new ZFWalker() {
            public boolean invoke(int i, ZFile zf) {
                if (zf.isDir())
                    return true;
                if (!zf.isFile())
                    return false;

                String tp = zf.type().toLowerCase();
                String rph = src.relative(zf);

                // ZDoc
                if (tp.matches("^zdoc|man$")) {
                    log.infof("zdoc:", rph);
                    Parsing ing = new Parsing(io.readString(zf));
                    paZDoc.scan(ing);
                    paZDoc.build(ing);
                    ing.root.normalize();
                    docs.set(rph, ing.root);
                }
                // Markdown
                else if (tp.matches("^md|markdown$")) {
                    log.infof("md:", rph);
                    throw Lang.noImplement();
                }
                // HTML
                else if (tp.matches("^html?$")) {
                    log.infof("html:", rph);
                    String html = Streams.readAndClose(io.readString(zf));
                    htmls.set(rph, html);
                }
                // 其他文件
                else {
                    return false;
                }
                return true;
            }
        };

        // 开始遍历
        List<ZFile> tops = src.ls(true);
        for (ZFile top : tops) {
            // 忽略
            if (topIgnores.contains(top)) {
                continue;
            }
            // 目录
            else if (top.isDir()) {
                ((ZDir) top).walk(true, walker);
            }
            // 文件
            else if (top.isFile()) {
                walker.invoke(0, top);
            }
            // 不肯
            else {
                throw Lang.impossible();
            }
        }

        // 返回自身
        return this;
    }

    public ZCache<ZDocHtmlCacheItem> libs() {
        return libs;
    }

    public ZCache<ZDocHtmlCacheItem> tmpl() {
        return tmpl;
    }

    public ZCache<ZDocNode> docs() {
        return docs;
    }

    public ZCache<String> htmls() {
        return htmls;
    }

    public Context vars() {
        return vars;
    }

    public List<ZDocRule> rules() {
        return rules;
    }

    public ZDocIndex indexes() {
        return indexes;
    }

    private void _read_rss(MultiLineProperties pp) {
        String[] ss = Strings.splitIgnoreBlank(pp.get("zdoc-rs"));
        for (String s : ss) {
            ZDir d = src.getDir(s);
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

    private void _set_cache_item(final ZCache<ZDocHtmlCacheItem> zc,
                                 String fname) {
        if (!Strings.isBlank(fname)) {
            topIgnores.add(fname);
            ZDir d = src.getDir(fname);
            if (null != d) {
                d.walk(true, new ZFWalker() {
                    public boolean invoke(int i, ZFile f) {
                        if (f.isDir())
                            return true;
                        if (!f.name().toLowerCase().matches(".*[.]html?$"))
                            return false;
                        ZDocHtmlCacheItem ci = new ZDocHtmlCacheItem();
                        ci.file(f).html(Streams.readAndClose(io.readString(f)));
                        String key = src.relative(f).replace('/', '.');
                        zc.set(key, ci);
                        return true;
                    }
                });
            }
        }
    }
}
