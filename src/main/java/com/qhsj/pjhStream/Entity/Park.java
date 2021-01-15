package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Data
public class Park extends Information{
    private byte byParkError;//停车场数据上传异常状态
    private String byParkingNo;//车位编号
    private byte byLocationStatus;//车辆状态
    private String sLicense;//车牌号
    @OneToMany
    private List<ByData> imgs;
}
