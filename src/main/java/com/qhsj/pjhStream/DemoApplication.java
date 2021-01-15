package com.qhsj.pjhStream;

import com.qhsj.pjhStream.Entity.EquipServiceInfo;
import com.qhsj.pjhStream.service.AlarmJavaDemoView;
import com.qhsj.pjhStream.service.HCNetSDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class DemoApplication {
	public static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
