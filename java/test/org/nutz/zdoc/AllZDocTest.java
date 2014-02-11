package org.nutz.zdoc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.zdoc.am.MdAmTest;
import org.nutz.zdoc.am.ZDocAmTest;
import org.nutz.zdoc.impl.MdParserTest;
import org.nutz.zdoc.impl.MdScannerTest;
import org.nutz.zdoc.impl.ZDocParserTest;
import org.nutz.zdoc.impl.ZDocScannerTest;
import org.nutz.zdoc.impl.html.MarkdownNode2HtmlTest;
import org.nutz.zdoc.util.ZDTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ZDTest.class,
                     ZLinkInfoTest.class,
                     ZDocAmTest.class,
                     ZDocScannerTest.class,
                     ZDocParserTest.class,
                     MdAmTest.class,
                     MdScannerTest.class,
                     MdParserTest.class,
                     MarkdownNode2HtmlTest.class})
public class AllZDocTest {}
