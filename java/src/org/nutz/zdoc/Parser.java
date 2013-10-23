package org.nutz.zdoc;

public interface Parser {

    /**
     * 主要表示各个文字块的父子关系，以及文字块的性质（比如是否是表格，或者列表）
     * <p>
     * 这个阶段主要针对的是段落进行分析。同时扫描也会处理行间连接符 `\`
     * 
     * @param ing
     *            解析时
     */
    void scan(Parsing ing);

    /**
     * 本阶段会针对各个段落，深入分析，也会结合各个段落之间的关系， 最终得到 zDoc 的中间数据结构
     * <p>
     * 解析的结果存放在解析时的 `root` 字段下
     * 
     * @param ing
     *            解析时
     */
    void build(Parsing ing);

}
