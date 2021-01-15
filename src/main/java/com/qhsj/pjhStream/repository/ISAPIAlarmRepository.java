package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.ISAPIAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISAPIAlarmRepository extends JpaRepository<ISAPIAlarm,Long> {
}
