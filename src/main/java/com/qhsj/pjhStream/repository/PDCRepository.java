package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.PDC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PDCRepository  extends JpaRepository<PDC,Long> {
}
