package org.nutz.zdoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Node;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.SimpleNode;
import org.nutz.vfs.ZFile;

/**
 * 保持了一份文档树的索引
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZDocIndex extends SimpleNode<ZFile> implements
        Comparable<ZDocIndex> {

    private String title;

    private List<ZDocAuthor> authors;

    private List<ZDocAuthor> verifiers;

    private String path;

    private String docBase;

    private ZDocNode docRoot;

    private String rawTex;

    private String briefHtml;

    private List<ZDocTag> tags;

    private Date lm;

    private String rpath;

    private String bpath;

    public ZDocIndex() {
        authors = new LinkedList<ZDocAuthor>();
        verifiers = new LinkedList<ZDocAuthor>();
        tags = new LinkedList<ZDocTag>();
    }

    public String toString() {
        return toString(0);
    }

    private NutMap _map;

    public NutMap toMap() {
        if (_map == null) {
            _map = new NutMap();
            _map.setv("title", title)
                .setv("path", path)
                .setv("tags", tags)
                .setv("authors", this.authorsListMap())
                .setv("verifiers", this.verifiersListMap())
                .setv("lm", lm)
                .setv("rawText", rawTex)
                .setv("briefHtml", briefHtml)
                .setv("rpath", rpath)
                .setv("bpath", bpath)
                .setv("rTargetPath", Files.renameSuffix(rpath, ".html"));
        }
        return _map;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        ZFile f = file();
        String prefix = Strings.dup("   ", indent);
        sb.append(prefix);
        if (null == f) {
            sb.append("???");
        } else if (f.isDir()) {
            sb.append("D:").append(f.name());
        } else if (f.isFile()) {
            sb.append("F:").append(f.name());
        }
        sb.append(String.format("[%s]", Strings.sNull(title)));
        indent++;
        for (ZDocIndex child : children()) {
            sb.append('\n').append(child.toString(indent));
        }
        return sb.toString();
    }

    /**
     * 
     * 遍历索引数，它会保证父节点先被调用到，<br>
     * 且如果 file 为 null，会被忽略
     * 
     * @param walker
     *            遍历器
     */
    public void walk(final Callback<ZDocIndex> walker) {
        if (null != file())
            walker.invoke(this);

        eachChild(new Each<ZDocIndex>() {
            public void invoke(int index, ZDocIndex zi, int length) {
                zi.walk(walker);
            }
        });
    }

    private List<ZDocIndex> children() {
        final List<ZDocIndex> list = new LinkedList<ZDocIndex>();
        eachChild(new Each<ZDocIndex>() {
            public void invoke(int index, ZDocIndex zi, int length) {
                list.add(zi);
            }
        });
        return list;
    }

    public ZDocIndex clear() {
        title = null;
        authors.clear();
        verifiers.clear();
        docRoot = null;
        rawTex = null;
        set(null).clearChildren();
        return this;
    }

    @Override
    public ZDocIndex parent(Node<ZFile> node) {
        super.parent(node);
        // 清空 rpath
        this.rpath = null;
        this.bpath = null;
        return this;
    }

    public String title() {
        return title;
    }

    public ZDocIndex title(String title) {
        if (Strings.isBlank(title) && null != file()) {
            this.title = file().name();
        } else {
            this.title = title;
        }
        return this;
    }

    public List<ZDocAuthor> authors() {
        return authors;
    }

    public List<NutMap> authorsListMap() {
        if (null != authors && !authors.isEmpty()) {
            List<NutMap> list = new ArrayList<NutMap>(authors.size());
            for (ZDocAuthor au : authors)
                list.add(au.toMap());
            return list;
        }
        return null;
    }

    public ZDocIndex author(String author) {
        if (Strings.isBlank(author))
            return this;
        return author(new ZDocAuthor(author));
    }

    public ZDocIndex author(ZDocAuthor author) {
        this.authors.add(author);
        return this;
    }

    public ZDocIndex updateAuthors(List<String> authors) {
        if (null != authors && !authors.isEmpty()) {
            this.authors.clear();
            for (String au : authors)
                author(au);
        }
        return this;
    }

    public List<ZDocAuthor> verifiers() {
        return verifiers;
    }

    public List<NutMap> verifiersListMap() {
        if (null != verifiers && !verifiers.isEmpty()) {
            List<NutMap> list = new ArrayList<NutMap>(verifiers.size());
            for (ZDocAuthor au : verifiers)
                list.add(au.toMap());
            return list;
        }
        return null;
    }

    public ZDocIndex verifier(String verifier) {
        if (Strings.isBlank(verifier))
            return this;
        return verifier(new ZDocAuthor(verifier));
    }

    public ZDocIndex verifier(ZDocAuthor verifier) {
        this.verifiers.add(verifier);
        return this;
    }

    public ZDocIndex updateVerifier(List<String> verifiers) {
        if (null != verifiers && !verifiers.isEmpty()) {
            this.verifiers.clear();
            for (String au : verifiers)
                verifier(au);
        }
        return this;
    }

    public String path() {
        return path;
    }

    public ZDocIndex path(String path) {
        this.path = path;
        // 清空 rpath
        this.rpath = null;
        return this;
    }

    public String docBase() {
        return docBase == null ? path : docBase;
    }

    public ZDocIndex docBase(String docBase) {
        this.docBase = docBase;
        return this;
    }

    public ZFile file() {
        return this.get();
    }

    public ZDocIndex file(ZFile file) {
        this.set(file);
        // 如果未曾设置标题，就用文件名做标题设置一下
        if (Strings.isBlank(title())) {
            title(Files.getMajorName(file.path()));
        }
        // 清空 rpath
        this.rpath = null;
        return this;
    }

    public String rawTex() {
        return rawTex;
    }

    public ZDocIndex rawTex(CharSequence rawTex) {
        this.rawTex = rawTex.toString();
        return this;
    }

    public String briefHtml() {
        return briefHtml;
    }

    public ZDocIndex briefHtml(String briefHtml) {
        this.briefHtml = briefHtml;
        return this;
    }

    public ZDocNode docRoot() {
        return docRoot;
    }

    public ZDocIndex docRoot(ZDocNode docRoot) {
        this.docRoot = docRoot;
        return this;
    }

    public List<ZDocTag> tags() {
        return tags;
    }

    public void addTag(ZDocTag tag) {
        if (null != tag)
            tags.add(tag);
    }

    public Date lm() {
        return lm;
    }

    public ZDocIndex lm(Date lm) {
        this.lm = lm;
        return this;
    }

    public ZDocIndex lm(long lm) {
        this.lm = new Date(lm);
        return this;
    }

    public String rpath() {
        if (null == rpath) {
            rpath = rpath(null);
        }
        return rpath;
    }

    /**
     * 获得相对于某个父节点的相对路径
     * 
     * @param zi
     *            父节点
     * @return 相对路径
     */
    public String rpath(ZDocIndex zi) {
        List<ZDocIndex> ans = this.ancestors();
        Collections.reverse(ans);
        LinkedList<String> ss = new LinkedList<String>();
        Iterator<ZDocIndex> it = ans.iterator();
        while (it.hasNext()) {
            ZDocIndex an = it.next();
            // 最后一个是根节点，忽略它
            if (!it.hasNext())
                break;
            if (zi == an)
                break;
            ss.addFirst(an.path());
        }

        ss.add(path());
        return Lang.concat("/", ss).toString();
    }

    public String bpath() {
        if (null == bpath) {
            int depth = depth();
            bpath = Strings.dup("../", file().isDir() ? depth : depth - 1);
        }
        return bpath;
    }

    public ZDocIndex root() {
        return (ZDocIndex) top();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<ZDocIndex> ancestors() {
        List list = parents();
        return list;
    }

    @Override
    public int compareTo(ZDocIndex zi) {
        return lm.compareTo(zi.lm) * -1;
    }

}
