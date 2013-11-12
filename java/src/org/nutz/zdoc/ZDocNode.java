package org.nutz.zdoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;

public class ZDocNode {

    private ZDocNodeType type;

    private int depth;

    private List<ZDocEle> eles;

    private ZDocNode parent;

    private List<ZDocNode> children;

    private ZDocAttrs attrs;

    public ZDocNode() {
        this.eles = new LinkedList<ZDocEle>();
        this.children = new LinkedList<ZDocNode>();
        this.attrs = new ZDocAttrs();
        this.type = ZDocNodeType.NODE;
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
        return eles.isEmpty() && children.isEmpty();
    }

    public boolean isTop() {
        return 0 == depth;
    }

    public boolean isHeader() {
        return ZDocNodeType.HEADER == type;
    }

    public boolean isBlockquote() {
        return ZDocNodeType.BLOCKQUOTE == type;
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

    public ZDocNode parent(ZDocNode parent) {
        this.parent = parent;
        this.parent.children.add(this);
        this.depth(parent.depth + 1);

        return this;
    }

    public List<ZDocNode> children() {
        return children;
    }

    public ZDocNode normalize() {
        if (!(eles instanceof ArrayList<?>)) {
            ArrayList<ZDocEle> list = new ArrayList<ZDocEle>(eles.size());
            list.addAll(eles);
            eles = list;
        }
        if (!(children instanceof ArrayList<?>)) {
            ArrayList<ZDocNode> list = new ArrayList<ZDocNode>(children.size());
            list.addAll(children);
            children = list;
            for (ZDocNode child : children)
                child.normalize();
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
