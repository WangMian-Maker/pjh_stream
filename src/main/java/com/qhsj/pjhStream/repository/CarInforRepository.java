package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.CarInfor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarInforRepository extends JpaRepository<CarInfor,Long> {
}
