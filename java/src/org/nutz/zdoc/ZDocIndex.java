package org.nutz.zdoc;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback2;
import org.nutz.vfs.ZFile;

/**
 * 保持了一份文档树的索引
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZDocIndex {

    private ZDocIndex parent;

    private List<ZDocIndex> children;

    private String title;

    private String author;

    private String path;

    private ZFile file;

    private ZDocNode docRoot;

    private String rawTex;

    public ZDocIndex() {
        children = new LinkedList<ZDocIndex>();
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String prefix = Strings.dup("    ", indent);
        sb.append(prefix).append(path).append(" >> ").append(file);
        if (null != author || null != title) {
            sb.append(prefix)
              .append(String.format("\n[%s] @%s", title, author));
        }
        indent++;
        for (ZDocIndex child : children) {
            sb.append('\n').append(child.toString(indent));
        }
        return sb.toString();
    }

    public int depth() {
        int re = 0;
        ZDocIndex zi = this;
        while (null != zi.parent) {
            re++;
            zi = zi.parent;
        }
        return re;
    }

    /**
     * 
     * 遍历索引数，它会保证父节点先被调用到，<br>
     * 且如果 file 为 null，会被忽略
     * 
     * @param walker
     *            遍历器
     */
    public void walk(Callback2<ZDocIndex, ZFile> walker) {
        if (null != file)
            walker.invoke(this, file);
        for (ZDocIndex child : children)
            child.walk(walker);
    }

    public ZDocIndex clear() {
        title = null;
        author = null;
        file = null;
        children.clear();
        return this;
    }

    public ZDocIndex parent() {
        return parent;
    }

    public ZDocIndex parent(ZDocIndex parent) {
        this.parent = parent;
        parent.children().add(this);
        return this;
    }

    public List<ZDocIndex> children() {
        return children;
    }

    public String title() {
        return title;
    }

    public ZDocIndex title(String title) {
        this.title = title;
        return this;
    }

    public String author() {
        return author;
    }

    public ZDocIndex author(String author) {
        this.author = author;
        return this;
    }

    public String path() {
        return path;
    }

    public ZDocIndex path(String path) {
        this.path = path;
        return this;
    }

    public ZFile file() {
        return file;
    }

    public ZDocIndex file(ZFile file) {
        this.file = file;
        return this;
    }

    public String rawTex() {
        return rawTex;
    }

    public ZDocIndex rawTex(CharSequence rawTex) {
        this.rawTex = rawTex.toString();
        return this;
    }

    public ZDocNode docRoot() {
        return docRoot;
    }

    public ZDocIndex docRoot(ZDocNode docRoot) {
        this.docRoot = docRoot;
        return this;
    }

}
