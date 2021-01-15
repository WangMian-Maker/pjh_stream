package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;

import javax.persistence.OneToOne;

@Entity
@Data
public class IDInfoAlarm extends Information{
    private int dwMajor;//身份证刷卡信息
    private int dwMinor;//
    private String IDCard;//身份证号
    private String name;//姓名
    @OneToOne
    private ByData snapImg;//抓拍
    @OneToOne
    private ByData idCardImg;//身份证图片
    @OneToOne
    private ByData fingerImg;//指纹图片
}
