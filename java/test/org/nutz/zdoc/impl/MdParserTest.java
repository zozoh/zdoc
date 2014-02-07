package org.nutz.zdoc.impl;

import static org.nutz.zdoc.ZDocNodeType.HEADER;
import static org.nutz.zdoc.ZDocNodeType.LI;
import static org.nutz.zdoc.ZDocNodeType.NODE;
import static org.nutz.zdoc.ZDocNodeType.OL;

import org.junit.Before;
import org.junit.Test;
import org.nutz.zdoc.AbstractParsingTest;
import org.nutz.zdoc.ZDocNode;

public class MdParserTest extends AbstractParsingTest {

    @Before
    public void before() {
        parser = new MdParser();
    }
    
    @Test
    public void test_hierachy_li_00() {
        String s = "#AAA\n";
        s += " 1. L0\n";
        s += " 2. L1\n";
        s += "     1. L11\n";
        s += " 1. L2\n";

        ZDocNode root = PS(s);

        System.out.println(root.printAll());

        _C(root, NODE, 1, "{}", "");
        _C(root, HEADER, 1, "{tagName:'h1'}", "AAA", 0);
        _C(root, OL, 3, "{'$line-type':'OL','$line-indent':1}", "", 0, 0);
        _C(root, LI, 0, "{}", "L0", 0, 0, 0);
        _C(root, LI, 1, "{}", "L1", 0, 0, 1);
        _C(root, OL, 1, "{'$line-type':'OL','$line-indent':2}", "", 0, 0, 1, 0);
        _C(root, LI, 0, "{}", "L11", 0, 0, 1, 0, 0);
        _C(root, LI, 0, "{}", "L2", 0, 0, 2);
    }
}
