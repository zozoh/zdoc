package org.nutz.zdoc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.zdoc.am.MarkdownAmTest;
import org.nutz.zdoc.am.ZDocAmTest;
import org.nutz.zdoc.impl.MarkdownScannerTest;
import org.nutz.zdoc.impl.ZDocParserTest;
import org.nutz.zdoc.impl.ZDocScannerTest;
import org.nutz.zdoc.util.ZDTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ZDTest.class,
                     ZLinkInfoTest.class,
                     ZDocScannerTest.class,
                     ZDocAmTest.class,
                     MarkdownAmTest.class,
                     ZDocParserTest.class,
                     MarkdownScannerTest.class})
public class AllZDocTest {}
