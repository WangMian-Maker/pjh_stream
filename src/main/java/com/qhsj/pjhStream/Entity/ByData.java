package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class ByData{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    private byte[] img ;
}
