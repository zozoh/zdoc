package org.nutz.zdoc;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Files;
import org.nutz.lang.Stopwatch;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.vfs.simple.ZSimpleDir;
import org.nutz.vfs.simple.ZSimpleIO;
import org.nutz.zdoc.impl.FreemarkerTemplateFactory;
import org.nutz.zdoc.impl.html.RenderToHtml;

public class ZDoc {

    private static final Log log = Logs.get();

    public static final String VERSION = "2.0";

    private static final ZSimpleIO io = new ZSimpleIO();

    public static void main(String[] args) {
        log.infof("zDoc v%s", VERSION);
        RenderToHtml home = new RenderToHtml(io);
        ZSimpleDir src = new ZSimpleDir(Files.findFile(args[0]));
        ZSimpleDir dest = new ZSimpleDir(Files.createDirIfNoExists(args[1]));

        log.infof(" src : %s", src.path());
        log.infof(" dest: %s", dest.path());

        Stopwatch sw = Stopwatch.begin();
        log.info("....................................... parsing");
        home.src(src);

        log.info("....................................... rendering");
        home.dest(dest);
        ZDocTemplateFactory tfa = new FreemarkerTemplateFactory(home);
        Rendering ing = new Rendering(io, tfa);
        // 设置全局的上下文
        ing.context().setv("siteTitle", home.title);
        ing.context().setv("tags", home.tags);
        ing.context().setv("othersTag", home.othersTag);
        ing.context().setv("tagPath", home.tagPath);
        List<ZDocTag> topTags = new ArrayList<ZDocTag>(home.topTags.length);
        for (String str : home.topTags) {
            ZDocTag tag = home.tags.get(str);
            if (null != tag) {
                topTags.add(tag);
            }
        }
        ing.context().setv("topTags", topTags);
        // 开始渲染
        home.render(ing);

        sw.stop();
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.infof("all done in :%s", sw.toString());
    }

}
