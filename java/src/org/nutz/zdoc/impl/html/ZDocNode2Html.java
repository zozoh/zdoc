package org.nutz.zdoc.impl.html;

import java.util.ArrayList;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;
import org.nutz.zdoc.ZDocNode;
import org.nutz.zdoc.ZDocNodeType;

public class ZDocNode2Html {

    void joinNode(StringBuilder sb, ZDocNode nd) {
        // 标题
        if (nd.is(ZDocNodeType.HEADER)) {
            nodeAsHeader(sb, nd);
        }
        // 普通段落
        else if (nd.is(ZDocNodeType.PARAGRAPH)) {
            nodeAsParagraph(sb, nd);
        }
        // 代码
        else if (nd.is(ZDocNodeType.CODE)) {
            nodeAsCode(sb, nd);
        }
        // UL | OL
        else if (nd.is(ZDocNodeType.UL) || nd.is(ZDocNodeType.OL)) {
            nodeAsList(sb, nd);
        }
        // TABLE
        else if (nd.is(ZDocNodeType.TABLE)) {
            nodeAsTable(sb, nd);
        }
        // COMMENT
        else if (nd.is(ZDocNodeType.COMMENT)) {
            nodeAsComment(sb, nd);
        }
        // HR
        else if (nd.is(ZDocNodeType.HR)) {
            nodeAsHr(sb, nd);
        }
        // BLOCKQUOTE
        else if (nd.is(ZDocNodeType.BLOCKQUOTE)) {
            nodeAsBlockquote(sb, nd);
        }
        // 肯定有啥错
        else {
            throw Lang.makeThrow("Unrenderable node :\n %s", nd);
        }
    }

    private void nodeAsHeader(StringBuilder sb, ZDocNode nd) {
        _join_newline_of_node(sb, nd);
        String tagName = "h" + Math.max(6, nd.depth());
        sb.append("<" + tagName + ">");
        {
            joinEles(sb, nd);
        }
        sb.append("</" + tagName + ">");
        // 继续循环子节点
        {
            for (ZDocNode sub : nd.children()) {
                this.joinNode(sb, sub);
            }
        }
    }

    private void nodeAsParagraph(StringBuilder sb, ZDocNode nd) {
        _join_newline_of_node(sb, nd);
        sb.append("<p>");
        this.joinEles(sb, nd);
    }

    private void nodeAsBlockquote(StringBuilder sb, ZDocNode nd) {
        _join_newline_of_node(sb, nd);
        sb.append("<blockquote>");
        {
            this.joinEles(sb, nd);
            for (ZDocNode sub : nd.children()) {
                this.joinNode(sb, sub);
            }
        }
        _join_newline_of_node(sb, nd);
        sb.append("</blockquote>");
    }

    private void nodeAsHr(StringBuilder sb, ZDocNode nd) {
        _join_newline_of_node(sb, nd);
        sb.append("<hr>");
    }

    private void nodeAsComment(StringBuilder sb, ZDocNode nd) {
        _join_newline_of_node(sb, nd);
        sb.append("<!--").append(nd.text()).append("-->");
    }

    private void nodeAsTable(StringBuilder sb, ZDocNode nd) {
        _join_newline_of_node(sb, nd);
        sb.append("<table border=\"1\" cellspacing=\"2\" cellpadding=\"4\">");
        for (ZDocNode row : nd.children()) {
            _join_newline_of_node(sb, row);
            sb.append(String.format("<%s>", row.type()));
            for (ZDocNode cell : row.children()) {
                _join_newline_of_node(sb, cell);
                sb.append(String.format("<%s>", cell.type()));
                {
                    joinEles(sb, cell);
                }
                _join_newline_of_node(sb, cell);
                sb.append(String.format("</%s>", cell.type()));
            }
            _join_newline_of_node(sb, row);
            sb.append(String.format("</%s>", row.type()));
        }
        _join_newline_of_node(sb, nd);
        sb.append("</table>");
    }

    private void nodeAsList(StringBuilder sb, ZDocNode nd) {
        String tagName = nd.type().toString().toLowerCase();
        _join_newline_of_node(sb, nd);
        sb.append(String.format("<%s>", tagName));
        // 加入 LI
        for (ZDocNode li : nd.children()) {
            _join_newline_of_node(sb, li);
            sb.append("<li>");
            // 加入 LI 的内容
            joinEles(sb, li);
            // 看看是否有子节点
            for (ZDocNode child : li.children()) {
                joinNode(sb, child);
            }
            sb.append("</li>");
        }
        _join_newline_of_node(sb, nd);
        sb.append(String.format("</%s>", tagName));
    }

    private void nodeAsCode(StringBuilder sb, ZDocNode nd) {
        _join_newline_of_node(sb, nd);
        sb.append("<pre code-type=\""
                  + nd.attrs().getString("code-type", "unknown")
                  + "\">\n");
        sb.append(nd.text());
        sb.append("</pre>");
    }

    private void joinEle(StringBuilder sb, ZDocEle ele) {
        switch (ele.type()) {
        case INLINE:
        case SUP:
        case SUB:
            eleAsInline(sb, ele);
            break;
        case QUOTE:
            eleAsQuote(sb, ele);
            break;
        case IMG:
            eleAsImg(sb, ele);
            break;
        case BR:
            eleAsBr(sb, ele);
            break;
        default:
            throw Lang.impossible();
        }
    }

    private void eleAsBr(StringBuilder sb, ZDocEle ele) {
        sb.append("<br>");
    }

    private void eleAsImg(StringBuilder sb, ZDocEle ele) {
        if (ele.hasAttr("href")) {
            sb.append("<a href=\"").append(ele.href()).append(">");
        }
        // -------------------------------------------------
        sb.append("<img src=\"").append(ele.src()).append('"');
        int w = ele.width();
        if (w > 0) {
            sb.append(" width=\"").append(w).append('"');
        }
        int h = ele.height();
        if (h > 0) {
            sb.append(" height=\"").append(h).append('"');
        }
        if (!Strings.isBlank(ele.text())) {
            sb.append(" title=\"").append(ele.text()).append("\">");
        }
        // -------------------------------------------------
        if (ele.hasAttr("href")) {
            sb.append("</a>");
        }
    }

    private void eleAsQuote(StringBuilder sb, ZDocEle ele) {
        sb.append("<code>").append(ele.text()).append("</code>");
    }

    private void eleAsInline(StringBuilder sb, ZDocEle ele) {
        // 要生成的标签
        ArrayList<String> tagNames = new ArrayList<String>(10);
        if (ele.hasAttr("href")) {
            tagNames.add("a");
        } else if (ele.is(ZDocEleType.SUB)) {
            tagNames.add("sub");
        } else if (ele.is(ZDocEleType.SUP)) {
            tagNames.add("sup");
        } else if (ele.hasStyleAs("font-weight", "bold")) {
            tagNames.add("b");
        } else if (ele.hasStyleAs("font-style", "italic")) {
            tagNames.add("i");
        } else if (ele.hasStyleAs("text-decoration", "underline")) {
            tagNames.add("u");
        }

        // 整理 style 字段
        StringBuilder sbStyle = new StringBuilder();
        if (ele.hasStyleAs("text-decoratioin", "line-through")) {
            sbStyle.append("text-decoratioin:line-through;");
        } else if (ele.hasStyle("color")) {
            sbStyle.append("color:").append(ele.style("color")).append(";");
        }

        // 如果仅有 style
        if (sbStyle.length() > 0 && tagNames.isEmpty()) {
            tagNames.add("span");
        }

        // 输出开始标签
        if (!tagNames.isEmpty()) {
            sb.append("<").append(tagNames.get(0));
            if (sbStyle.length() > 0) {
                sb.append(" style=\"").append(sbStyle).append("\"");
            }
            sb.append(">");
            for (int i = 1; i < tagNames.size(); i++) {
                sb.append("<").append(tagNames.get(i)).append('>');
            }
        }

        // 输出内容
        sb.append(ele.text());

        // 输出结束标签
        for (int i = tagNames.size() - 1; i >= 0; i--) {
            sb.append("</").append(tagNames.get(i)).append('>');
        }

    }

    private void joinEles(StringBuilder sb, ZDocNode nd) {
        for (ZDocEle ele : nd.eles()) {
            joinEle(sb, ele);
        }
    }

    private void _join_newline_of_node(StringBuilder sb, ZDocNode nd) {
        sb.append("\n").append(Strings.dup("    ", nd.depth()));
    }

}
