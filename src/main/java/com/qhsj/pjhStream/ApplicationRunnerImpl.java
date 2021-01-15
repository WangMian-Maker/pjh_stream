package com.qhsj.pjhStream;

import com.qhsj.pjhStream.Entity.Camera;
import com.qhsj.pjhStream.Entity.EquipServiceInfo;
import com.qhsj.pjhStream.repository.CameraRepository;
import com.qhsj.pjhStream.service.AlarmJavaDemoView;
import lombok.extern.java.Log;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner, DisposableBean {
    @Autowired
    private AlarmJavaDemoView alarmJavaDemoView;
    @Autowired
    private CameraRepository cameraRepository;
    @Override
    public void destroy() throws Exception {
        alarmJavaDemoView.endServer();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        alarmJavaDemoView.setCameras(cameraRepository.findAICamera());
        alarmJavaDemoView.startServer();

    }
}
