package org.nutz.zdoc;

public interface Am {

    /**
     * 根据字符串序列的情况，判断是否能被当前自动机接受
     * 
     * @param ing
     *            解析时
     * @param cs
     *            字符串集合
     * @return true 能被自动机接受
     */
    boolean test(Parsing ing, ZDocChars cs);

    /**
     * 从一段字符开始读取数据进行解析，一运行到自动机认为已经结束为止
     * 
     * @param ing
     *            解析时
     * @param cs
     *            字符集合
     * 
     * @return 本自动机的运行的结果
     */
    AmResult run(Parsing ing, ZDocChars cs);

}
