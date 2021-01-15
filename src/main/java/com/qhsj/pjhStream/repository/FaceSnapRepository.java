package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.FaceSnap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaceSnapRepository extends JpaRepository<FaceSnap,Long> {
}
