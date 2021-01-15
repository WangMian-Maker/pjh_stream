package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.AIOPVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIOPVideoRepository extends JpaRepository<AIOPVideo,Long> {
}
