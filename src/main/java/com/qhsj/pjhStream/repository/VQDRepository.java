package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.VQD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VQDRepository extends JpaRepository<VQD,Long> {
}
