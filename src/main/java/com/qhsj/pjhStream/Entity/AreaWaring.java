package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Data
@Entity
public class AreaWaring extends Information{
    private RegionalBehavior behavior ;
    private int targetId ;

    @OneToOne
    private ByData img ;
}
