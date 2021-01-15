package com.qhsj.pjhStream.Entity;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Data
public class Information  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATE")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date time ;
    private String equipmentIP ;
    private String info ;
}
