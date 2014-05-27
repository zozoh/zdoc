package org.nutz.zdoc.impl.html;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.nutz.am.AmFactory;
import org.nutz.zdoc.BaseParserTest;
import org.nutz.zdoc.Rendering;
import org.nutz.zdoc.ZDocNode;
import org.nutz.zdoc.impl.MdParser;

public class MarkdownNode2HtmlTest extends BaseParserTest {

    private ZDocNode2Html nd2html;

    @Before
    public void before() {
        parser = new MdParser();
        nd2html = new ZDocNode2Html();
    }

    @Test
    public void test_simple_em() {
        String s = "X**Y**Z";
        ZDocNode root = PS(s);
        // ...........................................
        Rendering ing = new Rendering(null, null);
        // ...........................................
        StringBuilder sb = new StringBuilder();
        nd2html.joinNode(sb, root.node(0), ing);
        // ...........................................
        assertEquals("\n<p>X<b>Y</b>Z", sb.toString());
    }

    @Override
    protected AmFactory genAmFactory() {
        return NewAmFactory("markdown");
    }

    @Override
    protected String getRootAmName() {
        return "mdParagraph";
    }

}
