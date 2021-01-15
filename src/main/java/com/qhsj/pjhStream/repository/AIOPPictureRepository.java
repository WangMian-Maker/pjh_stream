package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.AIOPPicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIOPPictureRepository extends JpaRepository<AIOPPicture,Long> {
}
