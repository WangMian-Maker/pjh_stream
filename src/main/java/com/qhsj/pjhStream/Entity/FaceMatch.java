package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@Data
public class FaceMatch extends Information{
    @OneToOne
    private ByData pSnapPicBuffer;//人脸抓拍
    @OneToOne
    private ByData struSnapInfo_pBuffer1;//人脸对比结果
    @OneToOne
    private ByData struBlackListInfo_pBuffer1;
    private float fSimilarity;//相似度
    private String blackListName;
    private String faceID;
    private String facesID;
}
