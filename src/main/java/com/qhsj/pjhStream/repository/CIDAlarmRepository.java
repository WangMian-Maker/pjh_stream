package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.CIDAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CIDAlarmRepository extends JpaRepository<CIDAlarm,Long> {
}
