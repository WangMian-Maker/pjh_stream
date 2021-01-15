package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;


@Entity
@Data
public class PDC extends Information{
    private int dwEnterNum;//进入人数
    private int dwLeaveNum;//离开人数
    private int dwRelativeTime;//相对时标
    private int dwAbsTime;//绝对时标
    @Column(columnDefinition = "DATE")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date startTime;//开始时间
    @Column(columnDefinition = "DATE")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date endTime;//结束时间
}
