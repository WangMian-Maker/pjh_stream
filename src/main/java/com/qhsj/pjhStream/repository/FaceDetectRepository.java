package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.FaceDetect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaceDetectRepository extends JpaRepository<FaceDetect,Long> {
}
