package org.nutz.zdoc;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.util.SimpleContext;

public class ZDocAttrs extends SimpleContext {

    /**
     * 向集合增加一个项目，如果不存在，创建一个 List，如果存在，变成成一个 List
     * 
     * @param nm
     *            字段名
     * @param val
     *            字段值
     * @return 自身以便链式赋值
     */
    @SuppressWarnings("unchecked")
    public ZDocAttrs add(String nm, Object val) {
        if (null == val)
            return this;
        Object obj = this.get(nm);
        // 原来就木有
        if (null == obj) {
            if (val instanceof List<?>) {
                this.set(nm, val);
            } else {
                List<Object> list = new LinkedList<Object>();
                _add_val_to_list(list, val);
                this.set(nm, list);
            }
        }
        // 已经是一个 List
        else if (obj instanceof List<?>) {
            List<Object> list = (List<Object>) obj;
            _add_val_to_list(list, val);
        }
        // 不是一个 List，给它搞成 List
        else {
            List<Object> list = new LinkedList<Object>();
            list.add(obj);
            _add_val_to_list(list, val);
            this.set(nm, list);
        }

        return this;
    }

    private void _add_val_to_list(final List<Object> list, Object val) {
        Lang.each(val, new Each<Object>() {
            public void invoke(int index, Object o, int length) {
                list.add(o);
            }
        });
    }

}
