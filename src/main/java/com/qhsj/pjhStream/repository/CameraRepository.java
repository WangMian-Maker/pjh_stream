package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CameraRepository extends JpaRepository<Camera,Long> {
    @Query(value = "select *from camera where camera_type=2",nativeQuery = true)
    public List<Camera> findAICamera();
}
