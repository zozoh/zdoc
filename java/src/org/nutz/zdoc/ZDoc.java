package org.nutz.zdoc;

import org.nutz.lang.Files;
import org.nutz.lang.Stopwatch;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.vfs.simple.ZSimpleDir;
import org.nutz.vfs.simple.ZSimpleIO;
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

        log.info("....................................... show indexes");
        System.out.println(home.index());

        log.info("....................................... rendering");
        home.dest(dest);
        home.render(io);

        sw.stop();
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.infof("all done in :%s", sw.toString());
    }

}
