package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class EquipServiceInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String m_sDeviceIP;//已登录设备的IP地址
    private String m_sUsername;//设备用户名
    private String m_sPassword;//设备密码
    private int m_sPort;//端口
}
