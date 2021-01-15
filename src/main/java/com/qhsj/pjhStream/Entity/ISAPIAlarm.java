package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.List;

@Entity
@Data
public class ISAPIAlarm extends Information{
    private byte byDataType;//ISAPI报警信息{ get; set; }
    private byte byPicturesNumber;//图片张数
    @OneToOne
    private ByData data;//保存XML或者Json数据
    @OneToMany
    private List<ByData> imgs;
}
