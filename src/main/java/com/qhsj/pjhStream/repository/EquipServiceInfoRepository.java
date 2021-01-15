package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.EquipServiceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipServiceInfoRepository extends JpaRepository<EquipServiceInfo,Long> {
}
