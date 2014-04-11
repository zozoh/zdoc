---
title  : zDoc的渲染
author : zozoh(zozohtnt@gmail.com)
tags :
- zDoc
---

> 一个 zDoc 文档集合可以被输出成为任何介质

# 渲染整个文档集合

在 zDoc 的观点里，一次仅仅渲染一个文档是不够的。通常你会写很多文档，
然后放到一个文件夹下面，我们在 [](pages.md) 里声明了一个 zDoc工作目录的约定。
你解析出来这个目录的结构大约参见如下的对象结构

```java
public class ZDocHome {

    protected ZDir src;
    protected ZCache<ZDocHtmlCacheItem> libs;
    protected ZCache<ZDocHtmlCacheItem> tmpl;
    protected List<ZDir> rss;
    protected Context vars;
    protected List<ZDocRule> rules;
    protected ZDocIndex index;
    ...
```
你的渲染将面对整个 *ZDocHome*

# 渲染时变量

*大小写敏感*

Name        | 说明
------------|------------------------------
doc.content | 文档被渲染过后的内容
doc.key     | 文档
doc.author  | 文档作者
doc.title   | 文档标题
doc.tags    | 文档的标签（列表）
doc.lm      | 文档最后修改日期(Date 对象)
doc.rpath   | 文档相对根的路径，比如 *"post/2013/abc.zdoc"*
doc.bpath   | 文档回溯到根的路径, 顶层为 **""**，比如 **"../"**
doc.*xxx*   | 这个 *xxx* 来自根节点的 attrs

# zDoc 模板语言

当开始渲染一个文档的时候，你可以在文档以及文档的任何地方使用变量，变量有如下类型:

 Type | 说明
------|----------
VAL   | 普通值
OBJ   | 对象
LIST  | 一组值的列表
MAP   | 一个名值对，值可以是这四种类型

# 变量的使用与逻辑控制

你可以在你的*模板*的任何地方这些变量，使用的方法如下：

**VAL:普通值**

    ... xxx {{page.next.title}} xxx ...

**循环**

    {{@each: page.links as link}}
        <a>{{link}}</a>
    {{/each}}


**条件判断**

    {{@if: page.links}}
        // 可以嵌套其他语句
    {{/if}}

暂时不支持 `if-else if-else` 或者 `switch-case` 之类的分支判断。

**函数**

声明函数可以用作递归，以便输出树形数据结构

    {{@func: show_node : i, node}}
        <li> 
            <b>{{i}}</b> - {{node.text}}
            {{@if: node.children}}
                {{@each: node.children as child}}
                    {{call: show_node : child.num, child}}
                {{/each}}
            {{/if}}
        </li>
    {{/func}}




















