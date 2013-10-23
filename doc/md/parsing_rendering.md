解析&渲染
====

### 一个解析的过程

zDoc 解析器就是为了把输入的文档分析成为一个内存的中间结构，以便调用者使用。这个过程被成为`解析时`，
解析过后，会通过渲染器，将中间结果输出成为想要的结构，这个过程被成为`渲染时`。

1. **解析时**
	a. **扫描(scan)** : 主要表示各个文字块的父子关系，以及文字块的性质（比如是否是表格，或者列表）
	   这个阶段主要针对的是段落进行分析。同时扫描也会处理行间连接符 `\`
	
	b. **分析(build)** : 本阶段会针对各个段落，深入分析，也会结合各个段落之间的关系，
	   最终得到 zDoc 的中间数据结构。解析的结果存放在解析时的 `root` 字段下

2. **渲染时**
	a. **展开(extend)** : 展开阶段会将所有的 `LINK` 节点被其所链接的文档根节点替换，
	   同时也会替换文档内部的链接，同时它也会应用所有的占位符
	   
	b. **输出(output)** : 遍历中间结果节点集，向目标输出结果


### 扫描的中间结果

**每个自然行都是一个 `ZDocLine`** 

	{
		origin : "这里是原始的输入文字";
		text   : "这里是预期使用的文字";
		indent : 3;     // 4个空格或者一个tab被认为是 1 个 indent，默认为 0
		type   : UL | OL | TR | THEAD | TSEP | BLOCK | BLANK;
	}
	
* `TSEP` 类型特指表格的分隔线

**一组相同 `indent` 且相同 `type` 的 `ZDocLine` 构成一个 `ZDocBlock`**
	
	{
		List<ZDocLine> lines;
		indent      : 3;
		type        : UL | OL | TABLE | BLOCK | BLANK;
	}


### 解析时对象 : Parsing
	
	{
		Reader        reader;      // 记录了文字输入流，扫描过后关闭
		List<ZDocBlock>  blocks;   // 记录一个扫描结果数组，分析时使用
		List<ZDocAm>     ams;      // 一个自动机堆栈，当自动机退栈时会向节点添加 content
		int           depth;       // 当前的工作深度，root 为 0，H1-6 深度也是 1-6
		ZDocNode      root;        // 根节点
		ZDocNode      current;     // 当前的工作节点，分析逻辑会决定继续添加 content 
		                           // 还是开始一个新节点。开始新节点前，会回收自动机堆栈
		                           // 并会退回到 current 的某一个 parent
		ZDocEle       ele;         // 当前正在处理的文字元素
	}

### 渲染时对象 : Rendering

	{
		ZDir           home;     // 工作目录
		ZDir           dest;     // 目标输出目录
		ZCache<String> libs;     // 缓存所有的外部链接
		ZCache<String> tmpl;     // 缓存所有的模板
		ZCache<String> vars;     // 缓存所有的变量
		List<ZDocRule> rules;    // 模板映射规则
		ZDocIndex      indexes;  // 根据 index.xml 生成的根节点
	}












