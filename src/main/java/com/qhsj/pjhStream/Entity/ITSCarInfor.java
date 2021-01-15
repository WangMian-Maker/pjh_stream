package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class ITSCarInfor extends Information{
    private String plateLicense ;//车牌
    private int carIndex ;//车牌序号
    @OneToMany
    private List<ByData> imgs ;
}
