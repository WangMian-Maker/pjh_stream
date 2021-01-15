package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.ITSCarInfor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITSCarInforRpository extends JpaRepository<ITSCarInfor,Long> {
}
