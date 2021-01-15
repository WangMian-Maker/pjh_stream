package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@Data
public class AIOPPicture extends Information{
    private String szPID;//AI开放平台图片检测报警上传
    @OneToOne
    private ByData text;//保存Aiodata数据
}
