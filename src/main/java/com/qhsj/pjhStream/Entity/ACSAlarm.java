package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@Data
public class ACSAlarm extends Information{
    private int dwMajor;//门禁主机报警信息
    private int dwMinor;
    private String IDCard;//卡号
    private int dwCardReaderNo;//读卡器编号
    @OneToOne
    private ByData img;
}
