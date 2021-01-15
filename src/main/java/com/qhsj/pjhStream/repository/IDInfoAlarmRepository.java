package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.IDInfoAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDInfoAlarmRepository extends JpaRepository<IDInfoAlarm,Long> {
}
