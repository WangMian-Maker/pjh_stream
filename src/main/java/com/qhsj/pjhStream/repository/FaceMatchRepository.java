package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.FaceMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaceMatchRepository extends JpaRepository<FaceMatch,Long> {
}
