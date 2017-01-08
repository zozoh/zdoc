zdoc
====

新版的 zdoc 解析库，支持 zdoc, html, markdown

打包方法
------------------------------------

```
mvn assembly:single
```

运行方法
------------------------------------

```
# src 源文件夹, 必须包含zdoc.conf文件. 可参看 https://github.com/nutzam/nutzam/tree/master/pages
# dest 目标文件夹
java -jar zdoc.jar src dest
```