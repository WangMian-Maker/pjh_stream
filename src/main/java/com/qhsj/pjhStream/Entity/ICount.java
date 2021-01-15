package com.qhsj.pjhStream.Entity;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.*;

@Data
@Entity
public class ICount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;
    private int inCount ;
    private int outCount ;
}
