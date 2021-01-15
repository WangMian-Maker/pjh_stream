package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.*;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.awt.*;

@Entity
@Data
public class Camera {
    /**
     *
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//
    /**
     *
     */
    private String name;//
    private Status status;//
    /**
     *
     */
    private String account;
    /**
     *
     */
    private String ip;
    /**
     *
     */
    private Integer isInMachine;
    /**
     *
     */
    private String originPos;
    /**
     *
     */
    private String password;
    /**
     *
     */
    private Integer port;
    /**
     *
     */
    private String rtspUrl;
    /**
     *
     */
    private Integer wayNum;
    /**
     *
     */
    private String httpUrl;
    /**
     *
     */
    private String rtmpUrl;

    @Column(name = "type")
    private String type;

    private Direction direction;
    private CameraType cameraType;
    private Double x;
    private Double y;
}
