解析-中间数据结构
====
> 世间任何文档，都是相似的


### 如何描述一个文档

抽象的看，任何一个文档都可以下列结构来描述

    文档级属性 {         # ZDocMetas
        标题     
        作者
        子标题
        创建日期
        指定样式表
        …
    }
    
    标题                # ZDocNode.depth=0
    
        … 一块内容 …     # ZDocNode.depth=-1
        
        标题 A
            
            … 一块内容 …
            … 一块内容 …
            
* 无论对于 HTML, markdown 我们都可以将其文档理解成这样的结构
* 当然，我们可以用 zDoc 推荐的书写方式更加直白的写出这样的结构。

我们可以看出，所谓的 zDoc 文档结构，就是一个 ZDocNode 组成的文档结构树。所有的叶子节点
是段落内容，所有的中间节点则作为标题

### 关于 ZDocNode

任何一个节点，都可能表示以下的内容

   Type   | 说明
----------|--------------
NODE      | 文档节点，通常作为文档的根节点
HEADER    | 标题，根据 depth 来决定标题级别
PARAGRAPH | 普通文字块，不能有子节点
BLOCKQUOTE| 缩进文字，子节点只能是 BLOCKQUOTE
TABLE     | 表格，支持 attrs `$cols`，是个数组，表示自身的列居左居右设置
THEAD     | 表头行
TH        | 表头单元格
TR        | 表格行
TD        | 单元格
OL        | 有序列表
UL        | 无序列表
LI        | 列表项
HR        | 分割线
CODE      | 代码
HTML      | 一段 HTML 代码，不会有子节点，内容为直接输出，通常是 HTML 文档的解析结果
COMMENT   | 注释
LINK      | 外部链接对象，记录了对象原始的路径，它的子节点可以是任何一个 ZDocNode
OBJ       | 嵌入式对象

#### ZDocNode 对象的结构

    {
        type    : NODE|…|OBJ,
        depth   : 0,              // 深度，0 表示根节点
        eles    : [ZDocEle..],     // 本节点的显示内容
        parent  : ZDocNode,        // 父节点，文档只有一个根节点
        children: [ZDocNode..],    // 子节点，空和 null 是一个意思
        attrs   : {..},            // 对于这个段落的补充描述，是个名值对象
    }
    
* 一个解析出来的 ZDocNode 树，根节点必定为 `NODE`
* 根节点的 `attrs` 就是文档的属性
    
#### 标题 : HEADER

标题的 depth 一定不是 -1 ，否则就是错误
    
#### 表格 : TABLE

表格的父子结构必须是

    TABLE       // 属性 "$cols" 存放了各个列对齐方式，数组，null 表示自动
        THEAD   // 可选
            TH
        TR      
            TD
        
#### 列表 : OL|UL|LI

列表的父子结构必须

    OL | UL   // 属性 "itype" 可以是 "#,1,a,A,i,I" 任意一个
        LI
            OL | UL
                LI    // 可以没有子 BLOCK
                    PARAGRAPH
                    PARAGRAPH


    
#### 代码 : CODE

* 代码里的 content 会是纯文本
* 对于 \n 敏感，认为是换行
* 属性 `code-type` 可选，为进一步说明代码的格式内容
    
#### 嵌入式对象 : OBJ

* content 会被认为是一段 JSON 字符串，描述这个对象

#### 原始HTML代码 : HTML

* 里面的内容会在 HTML 输出时输出，否则会忽略
* 解析时，会替换内部的占位符以及图片链接和超链接做相应的替换


### 关于 ZDocEle

> 下面让我们只关注一个普通段落 ...

任何一个普通段落，可以认为是一个由 ZDocEle 构成的数组。这有点像 HTML。ZDocNode 就是 block 元素，
而 ZDocEle 就是 inline 元素。

ZDocEle 会有如下一些类型

Type   | 说明
-------|-------------------
INLINE | 普通行内文字 
QUOTE  | 被反引号括起来的文字
IMG    | 图片
BR     | 段内换行
SUP    | 标注
SUB    | 脚注

那么它的数据结构为:

    {
        type  : TEXT|..|SUB ,  // 元素的类型
        quote : ` | ' | "      // 被引号包裹的类型，0 表示没有被引号包裹
        href  : '…',           // 为这个元素包裹一个链接
        src   : '…',           // 元素为一个图片
        width : 1920,          // 图片宽度，仅对图片有效，<=0 表示不指定
        height: 1080,          // 图片高度，仅对图片有效，<=0 表示不指定
        text  : 'xxxxx',       // 文字内容，对于图片为图片的 title    
        style : {              // 元素的显示风格，支持的同义 CSS 属性为
            "font-weight" : "bold",
            "font-style" : "italic",
            "text-decoration" : "underline",
            "color" : "#00A",
            "background-color" : "#FF0"
        }
    }

### 弱弱的总结一下

总之，zDoc 项目就是提供各种解析器(ZDocParser)，可以将:

* HTML
* zdoc
* markdown

这样的文件类型转换成上述的结构，同时它也提供各种渲染器(ZDocRender)，
将这个结构转换成:

* HTML
* zdoc
* markdown
* PDF
* MS Word





























    



















    
