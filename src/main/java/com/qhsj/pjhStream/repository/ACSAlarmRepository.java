package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.ACSAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ACSAlarmRepository extends JpaRepository<ACSAlarm,Long> {
}
