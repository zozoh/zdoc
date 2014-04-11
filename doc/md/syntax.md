---
title:zDoc 的语法
---

> zDoc 是一种 wiki，设计它的目的和 markdown 是一样的。 后来我决定再重写一个解析器，
> 同时支持 zDoc 和 markdown 这两种语法。由于 markdown 语法偏弱，所以我选择了 
> GFM(github 的一种 markdown 方言) 来进行支持。

**下面几点，在你阅读本篇文档的时候，需要先了解一下 …**

1. 根据不同的文件扩展名，zDoc 解析器可以决定用哪种方法来解析 wiki 文件。
    默认的，`.md` 文件就是 `GFM` 格式，`.zdoc` 文件就是 `zDoc` 格式
2. 在同一个文件里，你不能混用 zDoc 和 GFM 语法，但是你可以混用 HTML
3. 对于解析器，一个制表符和4个空格，是一样的

------------------


# 文档的元数据

> 我支持你用 zDoc，GFM(markdown的方言)和 HTML的一个我规定的子集书写你的文档。
> 一个文档可以有任意的元数据，以便其他程序使用。通常，这些元数据是声明在文档的开头部分。


### zDoc 文档
    
    #title  : 文档标题
    #author : zozoh
    #author : wendal
    #tags   : [Git,编译]
    
* 对于名称为 "author" 或者 "verifier" 的元数据，一定认其值为一个列表
* 或者用 `[` 和 `]` 包裹的值，半角逗号分隔也表示列表

### GFM 文档

_除了文档标题，均采用 HTML 注释_
    
    ---
    author : zozoh
    tags   : 
    - Git
    - 编译
    ---

### HTML 文档

    <html>
    ...
    </html> 

* zDoc 解析器将任何 HTML 文件理解成根节点下仅有多个 `HTML` 类型的节点
* 根据占位符和链接，来拆分 HTML 字符串 

# zDoc和GFM 的语法

### 段落标题
    
    zDoc 的标题层级根据缩进的级别来划分
    ---------------------------------------- zdoc
    标题 A
        内容 A
        
        标题 B
            内容 B

    ---------------------------------------- GFM
    ## 标题 A
    内容 A
    ### 标题 B
    内容 B

* zDoc 的文档层级划分是参考了 Python 语言，这是它 `GFM` 最大的区别
* 对于 zDoc 来说，确定一个段落是否是标题，主要是看它后面有没有一个段落缩进层级比它更多。

### 段落
    
    zDoc 和 GFM 一样，段落为连续两个段内换行，
    段内换行 `\n` 表示一个空格，符号`\`表示跳过段内换行
    下面两段文字输出的效果是一致的
    ---------------------------------------- zdoc
    这里是段落A
    这里还是段落A，实际是与上一行是一行
    
    ---------------------------------------- GFM
    这里是段落A
    这里还是段落A，实际是与上一行是一行
    
> 对于段落，是自动作为自己前一个标题的内容，对于 zDoc 来说会根据缩进多一些判断。判断是否是一个
> 新标题的开始

### 内嵌锚点
    
    对应 GFM 和 zDoc 任何一个标题都会有一个自动的锚点，锚点的值为
        MD5(标题保留字母数字下划线和一切256以上的字符)
    同时你可以通过内嵌 HTML 来做到这一点，声明自己自定义的锚点
    
    <a name="my_anchor"></a>
    
### 链接
    
    zDoc 的链接写在 [..] 中，如果 [^..] 表示要在新窗口打开
    ---------------------------------------- zDoc 
    [http://nutzam.com]            本窗口跳转
    [^http://nutzam.com]           新窗口跳转
    [http://nutzam.com Google]     本窗口跳转, 带文字
    [http://nutzam.com <abc.png>]  本窗口跳转, 带图标
    [#$标题A]           页内链接，会根据"标题A"自动计算锚点的值
    [#my_anchor]       自定义的页内链接
    [../readme.md]     指向一个 GFM 文档，链接文字会被这个文档标题替代
    [../readme.zdoc]   指向一个 zDoc 文档，链接文字会被这个文档标题替代
    
    ---------------------------------------- GFM
    [Nutz](http://nutzam.com)  相当于 [http://nutzam.com Nutz]
    http://nutzam.com          相当于 [http://nutzam.com]
    [](#$标题A)                 相当于 [#$标题A]
    [](#my_anchor)             相当于 [#my_anchor]
    [](../readme.md)           相当于 [../readme.md]
    [](../readme.zdoc)         相当于 [../readme.zdoc]

### 图片
    
    zDoc 的图片写在 <..> 中，只要不和它支持的 HTML 标签冲突即不会有问题
    ---------------------------------------- zDoc 
    <../imgs/abc.png>          采用原始宽高
    <../imgs/abc.png alt text> 采用原始宽高，指定鼠标悬停文字
    <80:../imgs/abc.png>       指定宽度为 80
    <x90:..imgs/abc.png>       指定高度为 90
    <80x90:imgs/abc.png>       指定宽高为 80x90
    
    ---------------------------------------- GFM
    ![](../imgs/abc.png)           相当于  <../imgs/abc.png>
    ![alt text](../imgs/abc.png)   相当于  <../imgs/abc.png alt text>
    ![80:](../imgs/abc.png)        相当于  <80:../imgs/abc.png>
    
### 列表

    ---------------------------------------- zDoc 
    # 有序列表
        # 有序列表
        # 有序列表
    * 无序列表
        * 无序列表
        * 无序列表
    
    ---------------------------------------- GFM
    1. 有序列表
        a. 有序列表
        B. 有序列表
    * 无序列表
        - 无序列表
        + 无序列表
        
> 对于列表来说，只有遇到了和自己类型不一致的列表，或者非列表段落才算终止。
> 列表内的缩进段落均属于当前列表

### 代码

    通过 `<Java>` 来声明代码的语言类型，可选   
    ---------------------------------------- zDoc 
    {{{<Java>
    public class Abc {
        ...
    }
    }}}
    
    GFM 的代码片段，语言声明也是可选的
    ---------------------------------------- GFM
    ```Java
    public class Abc {
        ...
    }
    ```
    或者所有代码至少带一个缩进
    
        public class Abc {
            ...
        }
    
### 表格

    对 zDoc 来说，行开始是否为 `||` 很重要，表头可选
    ---------------------------------------- zDoc 
    || A1  || B1  || C1  ||
    || --- ||:---:|| ---:||
    || A2  || B2  || C2  ||
    
    对 GFM 来说，表头为必须，是否有有表头分隔行很重要
    ---------------------------------------- GFM
    | A1  | B1  | C1 |
    | --- |:---:|---:|
    | A2  | B2  | C2 |
    
> 对于 `.zdoc` 的文件，你可以用 GFM 支持的表格形式编写表格

### 引用
    
    zDoc 和 GFM 支持同样的引用格式
    
    > 一级引用
    > > 二级引用
    > 回到一级引用
    Z

### 行内文字

    通过 {..} 来为一段文字声明特殊的样式
    ---------------------------------------- zDoc 
    粗体         {*some text}
    斜体         {/some text}
    下划线    {_some text}
    穿越线    {~some text}
    红色斜体       {#F00;_some text}
    穿越线斜体  {*/~some text}
    标注         {^sup}
    底注         {,sub}
    
    ---------------------------------------- GFM
    粗体         **some text** 或 __some text__
    斜体         *some text*   或 _some text_
    穿越线    <del>some text</del>
        
### 分割线

    支持连续 ---- 以上的减号作为分割线

### 逃逸字符

    任何一个被 `` 包裹的文字，都被逃逸，连续两个 `` 表示一个普通的 ` 字符
    如果仅仅想逃逸特殊符号，之前加一个 '\' 即可，特殊字符 '\' 如果不是在行尾
    那么它就是用来逃逸下一个字符，因此它也能被用来逃逸自身
    
### 占位符

* 无论 zDoc 和 GFM，以及 HTML 都支持 `${placeholder}` 来声明占位符，
* 根据 `zdoc.conf` 中声明的 `zdoc-vars` 来为占位符设值

### 外部链接文档

如果很多文档都有共同的段落，你可以声明一个外部链接段落，引入共同的部分，这是个补充语法，
zDoc,GFM, HTML 都支持，你只需要在任意的地方声明一个 HTML 注释即可

    <!--@import head.menu-->
    
需要说明的一点是， zDoc 和 GFM 你必须把这个声明放到一个单独的段落里，所以下面的链接是不生效的，
其效果就是普通的注释:

    A<!--@import head.menu-->B

你必须写成这样才行

    A
    
    <!--@import head.menu-->
    
    B
    
当然，在一行中，前后空格是无所谓的


### 嵌入式 HTML

`zDoc` 还是 `GFM` 都支持直接书写 `HTML 子集`， GFM 由于 markdown 的语法对于缩进不敏感,
因此可以在任何段落里书写 HTML，比如

    这是我的一段<b style="color:red">加粗加红色</b> 文字
    <p>
    我可以用 `<p>` 声明一个段落
    
对于 `zDoc`，你也可以在任何段落写 HTML，但是如果你想写大段的 HTML，比如表格等比较复杂的 HTML
结构，你需要这么写

    <HTML>
    <Table>
        <tr><td>列A</td><td>列B</td></tr>
        <tr><td>aa</td><td>bb</td></tr>
    </table>
    </HTML>
    
即，你的 HTML 代码，由 `<html>` 标签包裹，且这两个开始结束标签必须是单独一行。这时，会调用
`HTML 子集` 对应的解析方式来分析 HTML。

当然如果你想在普通的段落里随便嵌入 `<a>` 和 `<img>` 等 `HTML 子集` 允许的行内标签，对于
`zDoc` 和 `GFM` 都是一样的，您随意 ^_^


    
# HTML 子集

> 无论是 GFM 还是 zDoc 都支持内嵌 HTML 标签，当解析器遇到和可以理解的 HTML 标签
> 它会将其进行解析，丢弃不支持的属性，你可以在行内或者段落甚至文档级别应用 HTML 标签。
> 同时，如果你的文档本身就是 html 文档，zDoc 从 <body> 开始，会统一理解你的这些标签。

**下面有一些注意事项**

* 所有不认识的属性和标签将被丢弃，就好像你没写过它们一样
* 标签的大小写并不敏感
* 标签的属性可以不必用双引号包裹，单引号，或者不包裹均可
* 对于 `.html|.htm` 文件，如果没有元数据 `zdoc=true`，则认为是普通 HTML 文件
    * 普通 HTML 将会替换相应的 src,href 等属性
    * 仅仅是这样

TAG           | Name | Attributes  | Example
------------- | ---- | ----------- | --------------------------------
 A            | 链接  | href,target="_blank" | `<a href="http://nutzam.com">Nutzam</a>`
 A            | 锚点  | name    | `<a name="my_anchor"></a>`
 BR           | 换行  | --      | `<br>`
 B,I,U,EM     | 文字  | style   | `<b style="color:red">哈哈</b>`
 STRONG       | 文字  | style   | `<strong style="color:red">哈哈</strong>`
 SUB,SUP      | 文字  | style   | 脚注和尾注
 CODE         | 文字  | --      | 相当于反引号引用的部分
 IMG          | 图片  | src,width,height,title | `<img src="abc.png">`
 P            | 段落  | align   | `<p align="left">我是一段文字`
 BLOCKQUOTE   | 引用  | --      | `<blockquote>我是引用文字</blockquote>`
 H1-H6        | 标题  | --      | `<h2>我是二级标题</h2>`
 OL,UL,LI     | 列表  | --      | --
 TABLE        | 表格  | --      | --
 COLGROUP,COL | 表格  | align   | 只有这里才能声明单元格左右对齐
 TR,THEAD     | 表格  | --      | `THEAD` 里只能是 `TH`, `TR` 里只能是 `TD`
 TH,TD        | 表格  | --      | --
 HR           | 水平线 | --     | `<hr>`
 !--          | 注释  | --      | `<!--这里随便写什么注释-->`
 PRE          | 代码  | title   | `<pre title="java">public class .. </pre>`

    

** 对于文字的 HTML 标签，支持 style 指定的 CSS **

    color : #FF0 | #FF00AA | red
    background-color :  #FF0 | #FF00AA | red
    text-decoration: line-through
    







    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    