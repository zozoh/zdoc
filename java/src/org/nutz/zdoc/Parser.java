package org.nutz.zdoc;

public interface Parser {

    /**
     * 解析的结果存放在解析时的 `root` 字段下
     * 
     * @param ing
     *            解析时
     */
    void build(Parsing ing);

}
