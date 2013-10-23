package org.nutz.zdoc;

public interface Render {

    /**
     * 展开阶段会将所有的 `LINK` 节点被其所链接的文档根节点替换， <br>
     * 同时也会替换文档内部的链接，同时它也会应用所有的占位符
     * 
     * @param ing
     *            渲染时
     * @param root
     *            要被展开的节点
     */
    void extend(Rendering ing, ZDocNode root);

    /**
     * 遍历中间结果节点集，向目标输出结果
     * 
     * @param ing
     *            渲染时
     * @param root
     *            要被输出的节点
     */
    void output(Rendering ing, ZDocNode root);

}
