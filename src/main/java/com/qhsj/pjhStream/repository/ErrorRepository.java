package com.qhsj.pjhStream.repository;

import com.qhsj.pjhStream.Entity.Error;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorRepository extends JpaRepository<Error,Long> {
}
