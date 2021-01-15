package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class TPSStatData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;
    private byte byLane ;//车道号
    private byte bySpeed ;//车道过车平均数据
    private int dwLightVehicle ;//小型车数量
    private int dwMidVehicle ;//中型车数量
    private int dwHeavyVehicle ;//重型车数量
    private int dwTimeHeadway ;//车头时距
    private int dwSpaceHeadway ;//车头间距
    private float fSpaceOccupyRation ;//空间占有率
    private float fTimeOccupyRation ;//时间占有率
}
