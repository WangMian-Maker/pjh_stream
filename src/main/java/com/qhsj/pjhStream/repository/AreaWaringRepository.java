package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.AreaWaring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaWaringRepository extends JpaRepository<AreaWaring,Long> {
}
