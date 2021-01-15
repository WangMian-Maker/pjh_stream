package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.Park;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkRepository extends JpaRepository<Park,Long> {
}
