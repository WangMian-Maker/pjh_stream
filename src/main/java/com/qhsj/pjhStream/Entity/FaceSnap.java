package com.qhsj.pjhStream.Entity;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@Data
public class FaceSnap extends Information{
    private byte channel;//通道号
    @OneToOne
    private ByData small;
    @OneToOne
    private ByData big;
    private int dwFaceScore;
    private byte byAgeGroup;
    private byte bySex;
}
