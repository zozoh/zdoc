package org.nutz.zdoc;

public enum ZDocNodeType {

    /**
     * 文档节点，通常作为文档的根节点
     */
    NODE,
    /**
     * 标题，根据 depth 来决定标题级别
     */
    HEADER,
    /**
     * 普通文字块，不能有子节点
     */
    PARAGRAPH,
    /**
     * 引用文字块，子节点只能是 BLOCKQUOTE
     */
    BLOCKQUOTE,
    /**
     * 表格，支持 attrs `cols`，是个数组，表示自身的列居左居右设置
     */
    TABLE,
    /**
     * 表头行
     */
    THEAD,
    /**
     * 表头单元格
     */
    TH,
    /**
     * 表格行
     */
    TR,
    /**
     * 单元格
     */
    TD,
    /**
     * 有序列表
     */
    OL,
    /**
     * 无序列表
     */
    UL,
    /**
     * 列表项
     */
    LI,
    /**
     * 分割线
     */
    HR,
    /**
     * 代码
     */
    CODE,
    /**
     * HTML 代码，不会有子节点，内容为直接输出，通常是 HTML 文档的解析结果
     */
    HTML,
    /**
     * 注释
     */
    COMMENT,
    /**
     * 外部链接对象，记录了对象原始的路径，它的子节点可以是任何一个 ZDocNode
     */
    LINK,
    /**
     * 嵌入式对象
     */
    OBJ
}
