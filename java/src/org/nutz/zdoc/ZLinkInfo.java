package org.nutz.zdoc;

import org.nutz.lang.Strings;

/**
 * 描述一个文档内链接的信息。
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZLinkInfo {

    private String link;

    private String title;

    public ZLinkInfo parse(String str) {
        str = Strings.trim(str);
        int pos = str.indexOf(' ');
        if (pos > 0) {
            link = str.substring(0, pos);
            title = Strings.trim(str.substring(pos + 1));
            if (Strings.isQuoteBy(title, '"', '"')
                || Strings.isQuoteBy(title, '\'', '\'')
                || Strings.isQuoteBy(title, '(', ')')) {
                title = title.substring(1, title.length() - 1);
            }
        } else {
            link = str;
        }
        return this;
    }

    public String link() {
        return link;
    }

    public ZLinkInfo link(String link) {
        this.link = link;
        return this;
    }

    public boolean hasLink() {
        return !Strings.isBlank(link);
    }

    public String title() {
        return title;
    }

    public ZLinkInfo title(String title) {
        this.title = title;
        return this;
    }

    public boolean hasTitle() {
        return !Strings.isBlank(title);
    }

}
