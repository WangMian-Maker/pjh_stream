package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@Data
public class InterComEvent extends Information {
    private byte byEventType;//可视对讲事件
    private String equipID;//设备编号
    @OneToOne
    private ByData img;
}
