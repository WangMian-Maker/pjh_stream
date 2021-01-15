package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
public class TPSStatInfo extends Information{
    public int channel;//通道号
    public byte byStart;//开始码
    public byte byCmd;//命令号
    @Column(columnDefinition = "DATE")
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date startTime;//统计开始时间
    public int dwSamplePeriod;//统计时间(秒)
    @OneToMany
    public List<TPSStatData> tPSStats;
}
