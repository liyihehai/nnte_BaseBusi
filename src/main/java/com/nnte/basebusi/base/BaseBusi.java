package com.nnte.basebusi.base;

import com.nnte.basebusi.excption.BusiException;
import com.nnte.framework.base.BaseNnte;
import com.nnte.framework.utils.LogUtil;

public abstract class BaseBusi extends BaseNnte {

    public static final String Logger_Name = "nnte_BaseBusi";

    public void outLogExp(BusiException be){ LogUtil.logExp(getFrame_loggerName(),
            LogUtil.LogLevel.warn,be); }
}
