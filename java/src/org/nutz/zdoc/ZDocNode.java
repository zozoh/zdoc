package org.nutz.zdoc;

import static org.nutz.zdoc.ZDocNodeType.LI;
import static org.nutz.zdoc.ZDocNodeType.NODE;
import static org.nutz.zdoc.ZDocNodeType.OL;
import static org.nutz.zdoc.ZDocNodeType.UL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Strings;

public class ZDocNode {

    private ZDocNodeType type;

    private int depth;

    private List<ZDocEle> eles;

    private ZDocNode parent;

    private ZDocNode firstChild;

    private ZDocNode lastChild;

    private ZDocNode prev;

    private ZDocNode next;

    private List<ZDocNode> children;

    private ZDocAttrs attrs;

    public ZDocNode() {
        this.eles = new LinkedList<ZDocEle>();
        this.children = new LinkedList<ZDocNode>();
        this.attrs = new ZDocAttrs();
        this.type = NODE;
    }

    public boolean isNew() {
        return type == ZDocNodeType.NODE && isEmpty();
    }

    public String toString() {
        boolean attMap = attrs.getInnerMap().isEmpty();
        return String.format("%s:%s:%s:\"%s\"",
                             Strings.dup("> ", depth),
                             type,
                             attMap ? ""
                                   : Json.toJson(attrs.getInnerMap(),
                                                 JsonFormat.compact()
                                                           .setQuoteName(false)),
                             text());
    }

    public String printAll() {
        StringBuilder sb = new StringBuilder(this.toString());

        for (ZDocNode child : this.children)
            sb.append('\n').append(child.printAll());

        return sb.toString();
    }

    public boolean isEmpty() {
        return eles.isEmpty() && children.isEmpty() && attrs.isEmpty();
    }

    public boolean isTop() {
        return 0 == depth;
    }

    public boolean is(ZDocNodeType type) {
        return this.type == type;
    }

    public ZDocNodeType type() {
        return type;
    }

    public ZDocNode type(ZDocNodeType type) {
        this.type = type;
        return this;
    }

    public int depth() {
        return depth;
    }

    public ZDocNode depth(int depth) {
        this.depth = depth;
        return normalizeDepth();
    }

    public ZDocNode normalizeDepth() {
        if (null != this.children) {
            int d2 = depth + 1;
            for (ZDocNode child : children)
                child.depth(d2);
        }
        return this;
    }

    public List<ZDocEle> eles() {
        return eles;
    }

    public ZDocNode addEle(ZDocEle ele) {
        if (null != ele)
            this.eles.add(ele.normalize());
        return this;
    }

    public ZDocNode addEles(Collection<? extends ZDocEle> eles) {
        this.eles.addAll(eles);
        return this;
    }

    public boolean hasEles() {
        return !eles.isEmpty();
    }

    public String text() {
        if (null == eles || eles.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        Iterator<ZDocEle> it = eles.iterator();
        sb.append(it.next().text());

        while (it.hasNext())
            sb.append(Strings.sNull(it.next().text(), ""));

        return sb.toString();
    }

    public ZDocNode text(String text) {
        eles.clear();
        eles.add(new ZDocEle().text(text));
        return this;
    }

    public ZDocNode parent() {
        return parent;
    }

    public ZDocNode firstChild() {
        return firstChild;
    }

    public ZDocNode lastChild() {
        return lastChild;
    }

    public ZDocNode prev() {
        return prev;
    }

    public ZDocNode prev(ZDocNode nd) {
        this.prev = nd;
        nd.next = this;
        return this;
    }

    public ZDocNode prevEach(Each<ZDocNode> callback) {
        ZDocNode nd = this;
        int i = 0;
        while (nd.prev != null) {
            callback.invoke(i++, nd.prev, -1);
            nd = nd.prev;
        }
        return this;
    }

    public List<ZDocNode> prevAll() {
        final List<ZDocNode> list = new LinkedList<ZDocNode>();
        prevEach(new Each<ZDocNode>() {
            public void invoke(int index, ZDocNode nd, int length) {
                list.add(nd);
            }
        });
        return list;
    }

    public ZDocNode next() {
        return next;
    }

    public ZDocNode next(ZDocNode nd) {
        this.next = nd;
        nd.prev = this;
        return this;
    }

    public ZDocNode nextEach(Each<ZDocNode> callback) {
        ZDocNode nd = this;
        int i = 0;
        while (nd.next != null) {
            callback.invoke(i++, nd.next, -1);
            nd = nd.next;
        }
        return this;
    }

    public List<ZDocNode> nextAll() {
        final List<ZDocNode> list = new LinkedList<ZDocNode>();
        nextEach(new Each<ZDocNode>() {
            public void invoke(int index, ZDocNode nd, int length) {
                list.add(nd);
            }
        });
        return list;
    }

    public ZDocNode parent(ZDocNode parent) {
        this.parent = parent;

        if (!parent.hasChildren()) {
            parent.firstChild = this;
            parent.lastChild = this;
        } else {
            parent.lastChild.next(this);
            parent.lastChild = this;
        }
        this.parent.children.add(this);
        this.depth(parent.depth + 1);

        return this;
    }

    public List<ZDocNode> children() {
        return children;
    }

    public boolean hasChildren() {
        return null != children && !children.isEmpty();
    }

    public ZDocNode normalizeChildren() {
        if (!(eles instanceof ArrayList<?>)) {
            ArrayList<ZDocEle> list = new ArrayList<ZDocEle>(eles.size());
            list.addAll(eles);
            eles = list;
        }
        // 对于 LI 来说，如果有子 LI ，则用 UL|OL（根据第一个LI来决定）包裹
        if (is(LI) && hasChildren()) {
            ZDocNode li0 = node(0);
            ZDocNode xL = new ZDocNode();
            if ("OL".equalsIgnoreCase(li0.attrs().getString("$line-type", "XX"))) {
                xL.type(OL);
            } else {
                xL.type(UL);
            }
            xL.takeoverChildren(this);
            xL.parent(this);
        }
        // 确保所有的子节点都是 ArrayList (可能没啥用 ...)
        if (!(children instanceof ArrayList<?>)) {
            ArrayList<ZDocNode> list = new ArrayList<ZDocNode>(children.size());
            // 抛弃空节点
            for (ZDocNode child : children) {
                if (!child.isNew())
                    list.add(child.normalizeChildren());
            }
            children = list;
        }
        return this;
    }

    /**
     * 接管一个节点全部的子节点
     * 
     * @param nd
     *            节点
     * @return 自身
     */
    public ZDocNode takeoverChildren(ZDocNode nd) {
        if (nd.hasChildren()) {
            for (ZDocNode child : nd.children) {
                child.parent(this);
            }
        }
        return this;
    }

    /**
     * 根据一个子节点下标路径获取某个子孙节点 <br>
     * 比如:
     * 
     * <pre>
     * node(1,0)   得到当前节点第二个子节点的第一个子节点
     * node()      得到当前节点
     * </pre>
     * 
     * @param iPaths
     *            子节点的 index
     * @return 某子孙节点，null 表示不存在
     */
    public ZDocNode node(int... iPaths) {
        ZDocNode nd = this;
        if (null != iPaths && iPaths.length > 0)
            try {
                for (int i = 0; i < iPaths.length; i++) {
                    nd = nd.children().get(iPaths[i]);
                }
            }
            catch (Exception e) {
                return null;
            }
        return nd;
    }

    public ZDocAttrs attrs() {
        return attrs;
    }

}
