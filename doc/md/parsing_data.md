解析的中间数据结构
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
			
无论对于 HTML, markdown 我们都可以将其文档理解成这样的结构，同时，为了能统一修改样式，
zDoc 解析器也会理解 CSS。

当然，我们可以用 zdoc 推荐的书写方式更加直白的写出这样的结构。

我们可以看出，所谓的 ZDoc 文档结构，就是一个 ZDocNode 组成的文档结构树。所有的叶子节点
是段落内容，所有的中间节点则作为标题

### 关于 ZDocNode

任何一个节点，都可能表示以下的内容

* 标题 : HEADER
* 普通段落 : BLOCK
* 表格 : TABLE
* 有序列表 : OL
* 无序列表 : UL
* 无序列表 : LI
* 代码 : CODE
* 嵌入式对象 : OBJ

#### ZDocNode 对象的结构

	{
		type    : HEADER|…|OBJ,
		depth   : -1,              // 深度，-1 表示普通段落
		content : [ZDocEle..],     // 本节点的显示内容
		parent  : ZDocNode,        // 父节点，文档只有一个根节点
		children: [ZDocNode..],    // 子节点，空和 null 是一个意思
		attrs   : {..},            // 对于这个段落的补充描述，是个名值对象
	}
	
#### 标题 : HEADER

标题的 depth 一定不是 -1 ，否则就是错误
	
#### 表格 : TABLE

* 表格的每个 children ZDocNode 都是一行
* 每行的 children ZDocNode 都是一个单元格
* 这个单元格类型就是 BLOCK
		
#### 列表 : OL|UL|LI

* 列表的 content 会被无视，attrs 会描述列表里的一些配置内容
* 列表的 depth 为 parent.depth+1
* 列表的每个 children 都是 ZDocNode.LI，且 depth 一定为 parent.depth+1
* 列表项(LI)可以有 children，可以是另外一个 OL|UL|CODE|TABLE|OBJ
	
#### 代码 : CODE

* 代码里的 content 会是纯文本
* 对于 \n 敏感，认为是换行
	
#### 嵌入式对象 : OBJ

* content 会被认为是一段 JSON 字符串，描述这个对象

### 关于 ZDocEle

> 下面让我们只关注一个普通段落 ...

任何一个普通段落，可以认为是一个由 ZDocEle 构成的数组。这有点像 HTML。ZDocNode 就是 block 元素，
而 ZDocEle 就是 inline 元素。

ZDocEle 会有如下一些类型

* 普通文字 : TEXT
* 段内换行 : BR
* 标注 : SUP
* 脚注 : SUB

那么它的数据结构为:

	{
		type  : TEXT|..|SUB ,  // 元素的类型
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
		},
		className : 'xyz'      // 默认为空，指向一个 CSS 的 rule
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





























	



















    