package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.TPSStatInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TPSStatInfoRepository extends JpaRepository<TPSStatInfo,Long> {
}
