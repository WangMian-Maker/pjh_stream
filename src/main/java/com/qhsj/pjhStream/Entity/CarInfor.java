package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Data
@Entity
public class CarInfor extends Information{
    private String plateLicense ;//车牌
    private int carIndex ;//车牌序号
    @OneToOne
    private ByData closeImg ;//近景图
    @OneToOne
    private ByData remoteImg ;//远景图
    @OneToOne
    private ByData cardImg ;//车牌图
}
