package org.nutz.am;

public enum AmStatus {
    /**
     * 丢弃当前堆栈
     */
    DROP,
    /**
     * 继续，读取下一个字符，执行栈顶自动机的 run 方法
     */
    CONTINUE,
    /**
     * 将弹出操作栈顶自动机，并执行它的 done 方法
     */
    DONE,
    /**
     * 执行 DONE 操作，并重新进入下一个可能的自动机
     */
    DONE_BACK
}
