package org.nutz.zdoc;

public enum AmResult {

    // 自动机读取完了需要的数据
    FINISHED,

    // 自动机暂时挂起，由其他自动机接管
    HUNGUP,

    // 自动机还需要更多的数据
    NEED_MORE

}
