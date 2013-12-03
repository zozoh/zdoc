package org.nutz.zdoc;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback2;
import org.nutz.lang.util.Node;
import org.nutz.lang.util.SimpleNode;
import org.nutz.vfs.ZFile;

/**
 * 保持了一份文档树的索引
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZDocIndex extends SimpleNode<ZFile> {

    private String title;

    private String author;

    private String path;

    private ZDocNode docRoot;

    private String rawTex;

    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String prefix = Strings.dup("    ", indent);
        sb.append(prefix).append(path).append(" >> ").append(file());
        if (null != author || null != title) {
            sb.append(prefix)
              .append(String.format("\n[%s] @%s", title, author));
        }
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
    public void walk(final Callback2<ZDocIndex, ZFile> walker) {
        if (null != file())
            walker.invoke(this, file());

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
        author = null;
        docRoot = null;
        rawTex = null;
        set(null).clearChildren();
        return this;
    }

    @Override
    public ZDocIndex parent(Node<ZFile> node) {
        super.parent(node);
        return this;
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
        return this.get();
    }

    public ZDocIndex file(ZFile file) {
        this.set(file);
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
