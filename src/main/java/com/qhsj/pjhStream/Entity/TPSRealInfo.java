package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class TPSRealInfo extends Information{
    private int channel ;//通道号
    private int equipmentId ;//设备ID
    private byte startID ;//开始码
    private byte cmd ;//命令号
    private byte carWay ;//对应车道
    private byte carSpeed ;//对应车速
    private byte byLaneState ;
    private byte byQueueLen ;
    private int wLoopState ;
    private int wStateMask ;
    private int dwDownwardFlow ;
    private int dwUpwardFlow ;
    private byte byJamLevel ;
}
