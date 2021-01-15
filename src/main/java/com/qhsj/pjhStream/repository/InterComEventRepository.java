package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.InterComEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterComEventRepository extends JpaRepository<InterComEvent,Long> {
}
