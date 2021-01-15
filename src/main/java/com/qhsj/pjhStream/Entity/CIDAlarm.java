package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class CIDAlarm extends Information{
    private String sCIDCode;//报警主机CID报告
    private String sCIDDescribe;//
    private byte byReportType;//报告类型
    private int wDefenceNo;//防区号
}
