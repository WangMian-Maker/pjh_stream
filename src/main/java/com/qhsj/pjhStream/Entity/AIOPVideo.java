package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@Data
public class AIOPVideo extends Information{
    private String szTaskID;//AI开放平台视频检测报警上传
    @OneToOne
    private ByData text;//保存Aiodata数据
    @OneToOne
    private ByData img;
}
