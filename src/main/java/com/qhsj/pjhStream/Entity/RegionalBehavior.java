package com.qhsj.pjhStream.Entity;

public enum RegionalBehavior {
    GOINTOWARING,//穿越警戒面
    GOINTOAREA,//目标进入区域,
    LEAVEAREA,//目标离开区域,
    ROUNDINTRUSION,//周界入侵,
    OTHER//其他行为分析报警
}
