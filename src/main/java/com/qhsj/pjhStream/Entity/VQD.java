package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class VQD  extends Information{
    private String streamID;//流ID
    private int channel;//通道号
    private String sMonitorIP;
    private byte byResult;
    private byte bySignalResult;
    private byte byBlurResult;
}
