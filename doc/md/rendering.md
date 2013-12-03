关于渲染
====
> 一个 zDoc 文档集合可以被输出成为任何介质

### 渲染整个文档集合

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

### 渲染的时变量

当开始渲染一个文档的时候，你可以在文档以及文档的任何地方使用变量，变量有如下类型:

 Type | 说明
------|----------
VAL   | 普通值
NODE  | 树节点
LIST  | 一组值的列表
MAP   | 一个名值对





















