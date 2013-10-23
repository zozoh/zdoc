package org.nutz.zdoc;

import java.util.List;

import org.nutz.cache.ZCache;
import org.nutz.vfs.ZDir;

public class Rendering {

    public ZDir home;

    public ZDir dest;

    public ZCache<String> libs;

    public ZCache<String> tmpl;

    public ZCache<String> vars;

    public List<ZDocRule> rules;

    public ZDocIndex indexes;

}
