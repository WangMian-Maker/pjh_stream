package com.qhsj.pjhStream.controller;

import com.qhsj.pjhStream.Entity.Camera;
import com.qhsj.pjhStream.repository.CameraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping()
@RestController
public class CameraController {
    @Autowired
    private CameraRepository cameraRepository;
    @GetMapping("/cameraFindAll")
    public List<Camera> findAll(){
        return cameraRepository.findAll();
    }
}
