package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.TPSRealInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TPSRealInfoRepository extends JpaRepository<TPSRealInfo,Long> {
}
