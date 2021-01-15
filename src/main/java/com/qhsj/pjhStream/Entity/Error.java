package com.qhsj.pjhStream.Entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;

@Data
@Entity
public class Error extends Information {

    private String alarm ;
}
