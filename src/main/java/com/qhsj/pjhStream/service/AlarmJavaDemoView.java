package com.qhsj.pjhStream.service;/*
 * AlarmJavaDemoView.java
 */

import com.qhsj.pjhStream.DemoApplication;
import com.qhsj.pjhStream.Entity.*;
import com.qhsj.pjhStream.Entity.Error;
import com.qhsj.pjhStream.repository.*;
import com.sun.jna.Pointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The application's main frame.
 */
@Component
public class AlarmJavaDemoView {
    @Autowired
    private ACSAlarmRepository acsAlarmRepository;
    @Autowired
    private AIOPPictureRepository aiopPictureRepository;
    @Autowired
    private AIOPVideoRepository aiopVideoRepository;
    @Autowired
    private AreaWaringRepository areaWaringRepository;
    @Autowired
    private CarInforRepository carInforRepository;
    @Autowired
    private CIDAlarmRepository cidAlarmRepository;
    @Autowired
    private ErrorRepository errorRepository;
    @Autowired
    private FaceDetectRepository faceDetectRepository;
    @Autowired
    private FaceMatchRepository faceMatchRepository;
    @Autowired
    private FaceSnapRepository faceSnapRepository;
    @Autowired
    private IDInfoAlarmRepository idInfoAlarmRepository;
    @Autowired
    private InterComEventRepository interComEventRepository;
    @Autowired
    private  ISAPIAlarmRepository isapiAlarmRepository;
    @Autowired
    private ParkRepository parkRepository;
    @Autowired
    private PDCRepository pdcRepository;
    @Autowired
    private TPSRealInfoRepository tpsRealInfoRepository;
    @Autowired
    private TPSStatInfoRepository tpsStatInfoRepository;
    @Autowired
    private VQDRepository vqdRepository;
    @Autowired
    private ITSCarInforRpository itsCarInforRpository;


    HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
    HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息
    private int[] lAlarmHandles=new int[200];

    private List<Camera> cameras;

    public List<Camera> getCameras() {
        return cameras;
    }

    public void setCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }

    private String path;
    

    String m_sListenIP;
    int iListenPort;

    int lUserID;//用户句柄
    //int lAlarmHandle;//报警布防句柄
    int lListenHandle;//报警监听句柄

    public static FMSGCallBack fMSFCallBack;//报警回调函数实现
    public static FMSGCallBack_V31 fMSFCallBack_V31;//报警回调函数实现

    public static FGPSDataCallback fGpsCallBack;//GPS信息查询回调函数实现

    public AlarmJavaDemoView() {

        lUserID = -1;
        lListenHandle = -1;
        fMSFCallBack = null;
        fMSFCallBack_V31 = null;
        fGpsCallBack = null;
        if(System.getProperty("os.name").startsWith("Win")){
            path="C:/pic/";
        }
        else {
            path="/home/pic_stream/";
        }
        File file=new File(path);
        if(!file.exists()){
            file.mkdir();
        }
        int i=0;
        while (i<200){
            lAlarmHandles[i]=-1;
            i++;
        }


        boolean initSuc = DemoApplication.hCNetSDK.NET_DVR_Init();
        if (initSuc != true)
        {
                 //JOptionPane.showMessageDialog(null, "初始化失败");
        }

         HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG struGeneralCfg = new HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG();
        struGeneralCfg.byAlarmJsonPictureSeparate =1; //控制JSON透传报警数据和图片是否分离，0-不分离，1-分离（分离后走COMM_ISAPI_ALARM回调返回）
        struGeneralCfg.write();

        if(!DemoApplication.hCNetSDK.NET_DVR_SetSDKLocalCfg(17, struGeneralCfg.getPointer()))
        {
              //JOptionPane.showMessageDialog(null, "NET_DVR_SetSDKLocalCfg失败");
        }
    }
    public void startServer(){
        try{
            for(Camera info:cameras){
                System.out.println(info.getIp()+":"+info.getPort()+"开始服务");
                String m_sDeviceIP=info.getIp();
                String m_sUsername=info.getAccount();
                String m_sPassword=info.getPassword();
                int m_sPort=info.getPort();
                jButtonLoginActionPerformed(m_sDeviceIP,m_sUsername,m_sPassword,m_sPort);
                SetupAlarmChan();
            }
            StartAlarmListen();
            System.out.println("开始服务");
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    public  void endServer(){
        try{
            StopAlarmListen();
            for(int i=0;i<lAlarmHandles.length;i++){
                if(lAlarmHandles[i]<0) continue;
                if(DemoApplication.hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandles[i]))
                {
                    //JOptionPane.showMessageDialog(null, "撤防成功");
                    lAlarmHandles[i] = -1;
                }
            }
        }catch (Exception e){

        }

    }
    public class FGPSDataCallback implements HCNetSDK.fGPSDataCallback
    {
        public void invoke(int nHandle, int dwState, Pointer lpBuffer, int dwBufLen, Pointer pUser)
        {
        }
    }

    public void AlarmDataHandle(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser)
    {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String newName = sf.format(new Date());
        System.out.println(HCNetSDK.COMM_ALARM_PDC);
        if(lCommand==HCNetSDK.COMM_ALARM_PDC){
            System.out.println("来人了");
        }
        try {
            String sAlarmType = new String();
            String[] newRow = new String[3];
            //报警时间
            Date today = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String[] sIP = new String[2];

            sAlarmType = new String("lCommand=0x") +  Integer.toHexString(lCommand);
            //lCommand是传的报警类型
            switch (lCommand)
            {
                case HCNetSDK.COMM_ALARM_V40:
                    HCNetSDK.NET_DVR_ALARMINFO_V40 struAlarmInfoV40 = new HCNetSDK.NET_DVR_ALARMINFO_V40();
                    struAlarmInfoV40.write();
                    Pointer pInfoV40 = struAlarmInfoV40.getPointer();
                    pInfoV40.write(0, pAlarmInfo.getByteArray(0, struAlarmInfoV40.size()), 0, struAlarmInfoV40.size());
                    struAlarmInfoV40.read();

                    switch (struAlarmInfoV40.struAlarmFixedHeader.dwAlarmType)
                    {
                        case 0:
                            struAlarmInfoV40.struAlarmFixedHeader.ustruAlarm.setType(HCNetSDK.struIOAlarm.class);
                            struAlarmInfoV40.read();
                            sAlarmType = sAlarmType + new String("：信号量报警") + "，"+ "报警输入口：" + struAlarmInfoV40.struAlarmFixedHeader.ustruAlarm.struioAlarm.dwAlarmInputNo;
                            break;
                        case 1:
                            sAlarmType = sAlarmType + new String("：硬盘满");
                            break;
                        case 2:
                            sAlarmType = sAlarmType + new String("：信号丢失");
                            break;
                        case 3:
                            struAlarmInfoV40.struAlarmFixedHeader.ustruAlarm.setType(HCNetSDK.struAlarmChannel.class);
                            struAlarmInfoV40.read();
                            int iChanNum = struAlarmInfoV40.struAlarmFixedHeader.ustruAlarm.sstrualarmChannel.dwAlarmChanNum;
                            sAlarmType = sAlarmType + new String("：移动侦测") + "，"+ "报警通道个数：" + iChanNum + "，"+ "报警通道号：";

                            for (int i=0; i<iChanNum; i++)
                            {
                                byte[] byChannel = struAlarmInfoV40.pAlarmData.getByteArray(i*4, 4);

                                int iChanneNo = 0;
                                for(int j=0;j<4;j++)
                                {
                                    int ioffset = j*8;
                                    int iByte = byChannel[j]&0xff;
                                    iChanneNo = iChanneNo + (iByte << ioffset);
                                }

                                 sAlarmType= sAlarmType + "+ch["+ iChanneNo +"]";
                            }

                            break;
                        case 4:
                            sAlarmType = sAlarmType + new String("：硬盘未格式化");
                            break;
                        case 5:
                            sAlarmType = sAlarmType + new String("：读写硬盘出错");
                            break;
                        case 6:
                            sAlarmType = sAlarmType + new String("：遮挡报警");
                            break;
                        case 7:
                            sAlarmType = sAlarmType + new String("：制式不匹配");
                            break;
                        case 8:
                            sAlarmType = sAlarmType + new String("：非法访问");
                            break;
                    }

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    Error error=new Error();
                    error.setEquipmentIP(sIP[0]);
                    error.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    error.setAlarm(sAlarmType);
                    error.setTime(new Date());
                    errorRepository.save(error);
                    break;
                case HCNetSDK.COMM_ALARM_V30:
                    HCNetSDK.NET_DVR_ALARMINFO_V30 strAlarmInfoV30 = new HCNetSDK.NET_DVR_ALARMINFO_V30();
                    strAlarmInfoV30.write();
                    Pointer pInfoV30 = strAlarmInfoV30.getPointer();
                    pInfoV30.write(0, pAlarmInfo.getByteArray(0, strAlarmInfoV30.size()), 0, strAlarmInfoV30.size());
                    strAlarmInfoV30.read();
                    switch (strAlarmInfoV30.dwAlarmType)
                    {
                        case 0:
                            sAlarmType = sAlarmType + new String("：信号量报警") + "，"+ "报警输入口：" + (strAlarmInfoV30.dwAlarmInputNumber+1);
                            break;
                        case 1:
                            sAlarmType = sAlarmType + new String("：硬盘满");
                            break;
                        case 2:
                            sAlarmType = sAlarmType + new String("：信号丢失");
                            break;
                        case 3:
                            sAlarmType = sAlarmType + new String("：移动侦测") + "，"+ "报警通道：";
                             for (int i=0; i<64; i++)
                             {
                                if (strAlarmInfoV30.byChannel[i] == 1)
                                {
                                   sAlarmType=sAlarmType + "ch"+(i+1)+" ";
                               }
                            }
                            break;
                        case 4:
                            sAlarmType = sAlarmType + new String("：硬盘未格式化");
                            break;
                        case 5:
                            sAlarmType = sAlarmType + new String("：读写硬盘出错");
                            break;
                        case 6:
                            sAlarmType = sAlarmType + new String("：遮挡报警");
                            break;
                        case 7:
                            sAlarmType = sAlarmType + new String("：制式不匹配");
                            break;
                        case 8:
                            sAlarmType = sAlarmType + new String("：非法访问");
                            break;
                    }
                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    Error error1=new Error();
                    error1.setEquipmentIP(sIP[0]);
                    error1.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    error1.setAlarm(sAlarmType);
                    error1.setTime(new Date());
                    errorRepository.save(error1);
                    //alarmTableModel.insertRow(0, newRow);
                    break;
                case HCNetSDK.COMM_ALARM_RULE:
                    HCNetSDK.NET_VCA_RULE_ALARM strVcaAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
                    strVcaAlarm.write();
                    Pointer pVcaInfo = strVcaAlarm.getPointer();
                    pVcaInfo.write(0, pAlarmInfo.getByteArray(0, strVcaAlarm.size()), 0, strVcaAlarm.size());
                    strVcaAlarm.read();
                    RegionalBehavior behavior=RegionalBehavior.OTHER;
                    switch (strVcaAlarm.struRuleInfo.wEventTypeEx)
                    {
                        case 1:
                            sAlarmType = sAlarmType + new String("：穿越警戒面") + "，" +
                                    "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                    "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                    "_byIvmsChannel:" +  strVcaAlarm.struDevInfo.byIvmsChannel +
                                    "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            behavior = RegionalBehavior.GOINTOWARING;
                            break;
                        case 2:
                            sAlarmType = sAlarmType + new String("：目标进入区域") + "，" +
                                    "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                    "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                    "_byIvmsChannel:" +  strVcaAlarm.struDevInfo.byIvmsChannel +
                                    "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            behavior = RegionalBehavior.GOINTOAREA;
                            break;
                        case 3:
                            sAlarmType = sAlarmType + new String("：目标离开区域") + "，" +
                                    "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                    "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                    "_byIvmsChannel:" +  strVcaAlarm.struDevInfo.byIvmsChannel +
                                    "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            behavior = RegionalBehavior.LEAVEAREA;
                            break;
                        default:
                            sAlarmType = sAlarmType + new String("：其他行为分析报警，事件类型：")
                                    + strVcaAlarm.struRuleInfo.wEventTypeEx +
                                    "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                    "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                    "_byIvmsChannel:" +  strVcaAlarm.struDevInfo.byIvmsChannel +
                                    "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            behavior = RegionalBehavior.OTHER;
                            break;
                    }
                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    AreaWaring waring=new AreaWaring();
                    waring.setEquipmentIP(sIP[0]);
                    waring.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    waring.setBehavior(behavior);
                    waring.setTime(new Date());
                    waring.setTargetId(strVcaAlarm.struTargetInfo.dwID);
                    if(strVcaAlarm.dwPicDataLen>0)
                    {

                        //FileOutputStream //fout;
                        try {
                            //fout = new //FileOutputStream(path+ new String(pAlarmer.sDeviceIP).trim()
                                    //+ "wEventTypeEx[" + strVcaAlarm.struRuleInfo.wEventTypeEx + "]_"+ newName +"_vca.jpg");
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strVcaAlarm.pImage.getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
                            byte[] bytes=new byte[strVcaAlarm.dwPicDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            waring.getImg().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    areaWaringRepository.save(waring);
                    break;
                case HCNetSDK.COMM_UPLOAD_PLATE_RESULT:
                    CarInfor carInfor=new CarInfor();
                    HCNetSDK.NET_DVR_PLATE_RESULT strPlateResult = new HCNetSDK.NET_DVR_PLATE_RESULT();
                    strPlateResult.write();
                    Pointer pPlateInfo = strPlateResult.getPointer();
                    pPlateInfo.write(0, pAlarmInfo.getByteArray(0, strPlateResult.size()), 0, strPlateResult.size());
                    strPlateResult.read();
                    try {
                        String srt3=new String(strPlateResult.struPlateInfo.sLicense,"GBK");
                        sAlarmType = sAlarmType + "：交通抓拍上传，车牌："+ srt3;
                        carInfor.setPlateLicense(srt3);
                    }
                    catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    carInfor.setTime(new Date());
                    carInfor.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    carInfor.setEquipmentIP(sIP[0]);
                    carInfor.setCarIndex(strPlateResult.struVehicleInfo.dwIndex);

                    if(strPlateResult.dwPicLen>0)
                    {

                        //FileOutputStream //fout;
                        try {
                            //fout = new //FileOutputStream(path+ new String(pAlarmer.sDeviceIP).trim() + "_"
                                    //+ newName+"_plateResult1.jpg");
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strPlateResult.pBuffer1.getByteBuffer(offset, strPlateResult.dwPicLen);
                            byte [] bytes = new byte[strPlateResult.dwPicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            carInfor.getCloseImg().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if(strPlateResult.dwPicPlateLen!=0){

                        //FileOutputStream //fout;
                        try {
                            //fout = new //FileOutputStream(path+ new String(pAlarmer.sDeviceIP).trim() + "_"
                                    //+ newName+"_plateResult2.jpg");
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strPlateResult.pBuffer2.getByteBuffer(offset, strPlateResult.dwPicLen);
                            byte [] bytes = new byte[strPlateResult.dwPicPlateLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            carInfor.getCardImg().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if(strPlateResult.dwFarCarPicLen!=0){

                        //FileOutputStream //fout;
                        try {
                            //fout = new //FileOutputStream(path+ new String(pAlarmer.sDeviceIP).trim() + "_"
                                    //+ newName+"_plateResult3.jpg");
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strPlateResult.pBuffer5.getByteBuffer(offset, strPlateResult.dwPicLen);
                            byte [] bytes = new byte[strPlateResult.dwFarCarPicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            carInfor.getRemoteImg().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    carInforRepository.save(carInfor);
                    break;
                case HCNetSDK.COMM_ITS_PLATE_RESULT:
                    ITSCarInfor itsCarInfor = new ITSCarInfor();
                    HCNetSDK.NET_ITS_PLATE_RESULT strItsPlateResult = new HCNetSDK.NET_ITS_PLATE_RESULT();
                    strItsPlateResult.write();
                    Pointer pItsPlateInfo = strItsPlateResult.getPointer();
                    pItsPlateInfo.write(0, pAlarmInfo.getByteArray(0, strItsPlateResult.size()), 0, strItsPlateResult.size());
                    strItsPlateResult.read();
                    try {
                        String srt3=new String(strItsPlateResult.struPlateInfo.sLicense,"GBK");
                        sAlarmType = sAlarmType + ",车辆类型："+strItsPlateResult.byVehicleType + ",交通抓拍上传，车牌："+ srt3;
                        itsCarInfor.setPlateLicense(srt3);
                    }
                    catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableM  odel.insertRow(0, newRow);
                    itsCarInfor.setTime(new Date());
                    itsCarInfor.setCarIndex(strItsPlateResult.struVehicleInfo.dwIndex);
                    itsCarInfor.setEquipmentIP(sIP[0]);
                    itsCarInfor.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    for(int i=0;i<strItsPlateResult.dwPicNum;i++)
                    {
                        if(strItsPlateResult.struPicInfo[i].dwDataLen>0)
                        {
    
                            //FileOutputStream //fout;
                            try {
                                String filename =path+ new String(pAlarmer.sDeviceIP).trim() + "_"
                                        + newName+"_type["+strItsPlateResult.struPicInfo[i].byType+"]_ItsPlate.jpg";
                                //fout = new //FileOutputStream(filename);
                                //将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers = strItsPlateResult.struPicInfo[i].pBuffer.getByteBuffer(offset, strItsPlateResult.struPicInfo[i].dwDataLen);
                                byte [] bytes = new byte[strItsPlateResult.struPicInfo[i].dwDataLen];
                                buffers.rewind();
                                buffers.get(bytes);
                                ByData byData=new ByData();
                                byData.setImg(bytes);
                                itsCarInfor.getImgs().add(byData);
                                //fout.write(bytes);
                                //fout.close();
                            }  catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                    itsCarInforRpository.save(itsCarInfor);
                    break;
                case HCNetSDK.COMM_ALARM_PDC:
                    PDC pdc=new PDC();
                    HCNetSDK.NET_DVR_PDC_ALRAM_INFO strPDCResult = new HCNetSDK.NET_DVR_PDC_ALRAM_INFO();
                    strPDCResult.write();
                    Pointer pPDCInfo = strPDCResult.getPointer();
                    pPDCInfo.write(0, pAlarmInfo.getByteArray(0, strPDCResult.size()), 0, strPDCResult.size());
                    strPDCResult.read();

                    if(strPDCResult.byMode == 0)
                    {
                        strPDCResult.uStatModeParam.setType(HCNetSDK.NET_DVR_STATFRAME.class);
                        sAlarmType = sAlarmType + "：客流量统计，进入人数："+ strPDCResult.dwEnterNum + "，离开人数：" + strPDCResult.dwLeaveNum +
                                ", byMode:" + strPDCResult.byMode + ", dwRelativeTime:" + strPDCResult.uStatModeParam.struStatFrame.dwRelativeTime +
                                ", dwAbsTime:" + strPDCResult.uStatModeParam.struStatFrame.dwAbsTime;
                        pdc.setDwEnterNum(strPDCResult.dwEnterNum);
                        pdc.setDwLeaveNum(strPDCResult.dwLeaveNum);
                        pdc.setDwRelativeTime(strPDCResult.uStatModeParam.struStatFrame.dwRelativeTime);
                        pdc.setDwAbsTime(strPDCResult.uStatModeParam.struStatFrame.dwAbsTime);
                    }
                    if(strPDCResult.byMode == 1)
                    {
                        strPDCResult.uStatModeParam.setType(HCNetSDK.NET_DVR_STATTIME.class);
                        String strtmStart = "" + String.format("%04d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwYear) +"-"+
                                String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwMonth) +"-"+
                                String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwDay) +" "+
                                String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwHour) +":"+
                                String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwMinute) +":"+
                                String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwSecond);
                        String strtmEnd = "" + String.format("%04d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwYear) +"-"+
                                String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwMonth) +"-"+
                                String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwDay) +" "+
                                String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwHour) +":"+
                                String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwMinute) +":"+
                                String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwSecond);
                        sAlarmType = sAlarmType + "：客流量统计，进入人数："+ strPDCResult.dwEnterNum + "，离开人数：" + strPDCResult.dwLeaveNum +
                                ", byMode:" + strPDCResult.byMode + ", tmStart:" + strtmStart + ",tmEnd :" + strtmEnd;

                        System.out.println(sf.parse(strtmStart));
                        pdc.setStartTime(sf.parse(strtmStart));
                        pdc.setEndTime(sf.parse(strtmEnd));
                    }

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(strPDCResult.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    pdc.setEquipmentIP(sIP[0]);
                    pdc.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    pdc.setTime(new Date());
                    pdcRepository.save(pdc);
                    break;

                case HCNetSDK.COMM_ITS_PARK_VEHICLE:
                    Park park=new Park();
                    HCNetSDK.NET_ITS_PARK_VEHICLE strItsParkVehicle = new HCNetSDK.NET_ITS_PARK_VEHICLE();
                    strItsParkVehicle.write();
                    Pointer pItsParkVehicle = strItsParkVehicle.getPointer();
                    pItsParkVehicle.write(0, pAlarmInfo.getByteArray(0, strItsParkVehicle.size()), 0, strItsParkVehicle.size());
                    strItsParkVehicle.read();
                    try {
                        String srtParkingNo=new String(strItsParkVehicle.byParkingNo).trim(); //车位编号
                        String srtPlate=new String(strItsParkVehicle.struPlateInfo.sLicense,"GBK").trim(); //车牌号码
                        sAlarmType = sAlarmType + ",停产场数据,车位编号："+ srtParkingNo + ",车位状态："
                                + strItsParkVehicle.byLocationStatus+ ",车牌："+ srtPlate;
                    }
                    catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    park.setEquipmentIP(sIP[0]);
                    park.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    park.setTime(new Date());
                    park.setByParkError(strItsParkVehicle.byParkError);
                    park.setByParkingNo(new String(strItsParkVehicle.byParkingNo).trim());
                    park.setByLocationStatus(strItsParkVehicle.byLocationStatus);
                    park.setSLicense(new String(strItsParkVehicle.struPlateInfo.sLicense).trim());
                    for(int i=0;i<strItsParkVehicle.dwPicNum;i++)
                    {
                        if(strItsParkVehicle.struPicInfo[i].dwDataLen>0)
                        {
    
                            //FileOutputStream //fout;
                            try {
                                String filename = path+ new String(pAlarmer.sDeviceIP).trim() + "_"
                                        + newName+"_type["+strItsParkVehicle.struPicInfo[i].byType+"]_ParkVehicle.jpg";
                                //fout = new //FileOutputStream(filename);
                                //将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers = strItsParkVehicle.struPicInfo[i].pBuffer.getByteBuffer(offset, strItsParkVehicle.struPicInfo[i].dwDataLen);
                                byte [] bytes = new byte[strItsParkVehicle.struPicInfo[i].dwDataLen];
                                buffers.rewind();
                                buffers.get(bytes);
                                ByData byData=new ByData();
                                byData.setImg(bytes);
                                park.getImgs().add(byData);
                                //fout.write(bytes);
                                //fout.close();
                            }  catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                    parkRepository.save(park);
                    break;
                case HCNetSDK.COMM_ALARM_TFS:
                    //TFS
                    HCNetSDK.NET_DVR_TFS_ALARM strTFSAlarmInfo = new HCNetSDK.NET_DVR_TFS_ALARM();
                    strTFSAlarmInfo.write();
                    Pointer pTFSInfo = strTFSAlarmInfo.getPointer();
                    pTFSInfo.write(0, pAlarmInfo.getByteArray(0, strTFSAlarmInfo.size()), 0, strTFSAlarmInfo.size());
                    strTFSAlarmInfo.read();

                    try {
                        String srtPlate=new String(strTFSAlarmInfo.struPlateInfo.sLicense,"GBK").trim(); //车牌号码
                        sAlarmType = sAlarmType + "：交通取证报警信息，违章类型："+ strTFSAlarmInfo.dwIllegalType + "，车牌号码：" + srtPlate
                                + "，车辆出入状态：" + strTFSAlarmInfo.struAIDInfo.byVehicleEnterState;
                    }
                    catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(strTFSAlarmInfo.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    break;
                case HCNetSDK.COMM_ALARM_AID_V41:
                    HCNetSDK.NET_DVR_AID_ALARM_V41 struAIDAlarmInfo = new HCNetSDK.NET_DVR_AID_ALARM_V41();
                    struAIDAlarmInfo.write();
                    Pointer pAIDInfo = struAIDAlarmInfo.getPointer();
                    pAIDInfo.write(0, pAlarmInfo.getByteArray(0, struAIDAlarmInfo.size()), 0, struAIDAlarmInfo.size());
                    struAIDAlarmInfo.read();
                    sAlarmType = sAlarmType + "：交通事件报警信息，交通事件类型："+ struAIDAlarmInfo.struAIDInfo.dwAIDType + "，规则ID："
                            + struAIDAlarmInfo.struAIDInfo.byRuleID + "，车辆出入状态：" + struAIDAlarmInfo.struAIDInfo.byVehicleEnterState;

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(struAIDAlarmInfo.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    break;
                case HCNetSDK.COMM_ALARM_TPS_V41:
                    HCNetSDK.NET_DVR_TPS_ALARM_V41 struTPSAlarmInfo = new HCNetSDK.NET_DVR_TPS_ALARM_V41();
                    struTPSAlarmInfo.write();
                    Pointer pTPSInfo = struTPSAlarmInfo.getPointer();
                    pTPSInfo.write(0, pAlarmInfo.getByteArray(0, struTPSAlarmInfo.size()), 0, struTPSAlarmInfo.size());
                    struTPSAlarmInfo.read();

                    sAlarmType = sAlarmType + "：交通统计报警信息，绝对时标："+ struTPSAlarmInfo.dwAbsTime
                            + "，能见度:" + struTPSAlarmInfo.struDevInfo.byIvmsChannel
                            + "，车道1交通状态:" + struTPSAlarmInfo.struTPSInfo.struLaneParam[0].byTrafficState
                            + "，监测点编号：" + new String(struTPSAlarmInfo.byMonitoringSiteID).trim()
                            + "，设备编号：" + new String(struTPSAlarmInfo.byDeviceID ).trim()
                            + "，开始统计时间：" + struTPSAlarmInfo.dwStartTime
                            + "，结束统计时间：" + struTPSAlarmInfo.dwStopTime;

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(struTPSAlarmInfo.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    break;
                case HCNetSDK.COMM_UPLOAD_FACESNAP_RESULT:
                    FaceSnap faceSnap=new FaceSnap();
                    //实时人脸抓拍上传
                    HCNetSDK.NET_VCA_FACESNAP_RESULT strFaceSnapInfo = new HCNetSDK.NET_VCA_FACESNAP_RESULT();
                    strFaceSnapInfo.write();
                    Pointer pFaceSnapInfo = strFaceSnapInfo.getPointer();
                    pFaceSnapInfo.write(0, pAlarmInfo.getByteArray(0, strFaceSnapInfo.size()), 0, strFaceSnapInfo.size());
                    strFaceSnapInfo.read();
                    sAlarmType = sAlarmType + "：人脸抓拍上传，人脸评分：" + strFaceSnapInfo.dwFaceScore + "，年龄段：" + strFaceSnapInfo.struFeature.byAgeGroup + "，性别：" + strFaceSnapInfo.struFeature.bySex;
                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(strFaceSnapInfo.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); //设置日期格式
                    String time = df.format(new Date()); // new Date()为获取当前系统时间
                    faceSnap.setEquipmentIP(sIP[0]);
                    faceSnap.setChannel(strFaceSnapInfo.struDevInfo.byIvmsChannel);
                    faceSnap.setTime(new Date());
                    faceSnap.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    faceSnap.setByAgeGroup(strFaceSnapInfo.struFeature.byAgeGroup);
                    faceSnap.setBySex(strFaceSnapInfo.struFeature.bySex);
                    faceSnap.setDwFaceScore(strFaceSnapInfo.dwFaceScore);
                    //人脸图片写文件
                    try {
                        //FileOutputStream small = new //FileOutputStream(path + time + "small.jpg");
                        //FileOutputStream big = new //FileOutputStream(path + time + "big.jpg");

                        if(strFaceSnapInfo.dwFacePicLen > 0)
                        {
                            try {
                                faceSnap.getSmall().setImg(strFaceSnapInfo.pBuffer1.getByteArray(0, strFaceSnapInfo.dwFacePicLen));
                                //small.write(strFaceSnapInfo.pBuffer1.getByteArray(0, strFaceSnapInfo.dwFacePicLen), 0, strFaceSnapInfo.dwFacePicLen);
                                //small.close();
                            } catch (Exception ex) {
                                Logger.getLogger(AlarmJavaDemoView.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                        if(strFaceSnapInfo.dwFacePicLen > 0)
                        {
                            try {
                                faceSnap.getBig().setImg(strFaceSnapInfo.pBuffer2.getByteArray(0, strFaceSnapInfo.dwBackgroundPicLen));
                                //big.write(strFaceSnapInfo.pBuffer2.getByteArray(0, strFaceSnapInfo.dwBackgroundPicLen), 0, strFaceSnapInfo.dwBackgroundPicLen);
                                //big.close();
                            } catch (Exception ex) {
                                Logger.getLogger(AlarmJavaDemoView.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(AlarmJavaDemoView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    faceSnapRepository.save(faceSnap);
                    break;
                case HCNetSDK.COMM_SNAP_MATCH_ALARM:
                    //人脸黑名单比对报警
                    FaceMatch faceMatch=new FaceMatch();
                    HCNetSDK.NET_VCA_FACESNAP_MATCH_ALARM strFaceSnapMatch = new HCNetSDK.NET_VCA_FACESNAP_MATCH_ALARM();
                    strFaceSnapMatch.write();
                    Pointer pFaceSnapMatch = strFaceSnapMatch.getPointer();
                    pFaceSnapMatch.write(0, pAlarmInfo.getByteArray(0, strFaceSnapMatch.size()), 0, strFaceSnapMatch.size());
                    strFaceSnapMatch.read();

                    if ((strFaceSnapMatch.dwSnapPicLen > 0) && (strFaceSnapMatch.byPicTransType == 0)) {

                        //FileOutputStream //fout;
                        try {
                            String filename =path + newName + "_pSnapPicBuffer" + ".jpg";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strFaceSnapMatch.pSnapPicBuffer.getByteBuffer(offset, strFaceSnapMatch.dwSnapPicLen);
                            byte[] bytes = new byte[strFaceSnapMatch.dwSnapPicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            faceMatch.getPSnapPicBuffer().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if ((strFaceSnapMatch.struSnapInfo.dwSnapFacePicLen > 0) && (strFaceSnapMatch.byPicTransType == 0)) {

                        //FileOutputStream //fout;
                        try {
                            String filename = path + newName + "_struSnapInfo_pBuffer1" + ".jpg";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strFaceSnapMatch.struSnapInfo.pBuffer1.getByteBuffer(offset, strFaceSnapMatch.struSnapInfo.dwSnapFacePicLen);
                            byte[] bytes = new byte[strFaceSnapMatch.struSnapInfo.dwSnapFacePicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            faceMatch.getStruSnapInfo_pBuffer1().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if ((strFaceSnapMatch.struBlackListInfo.dwBlackListPicLen > 0) && (strFaceSnapMatch.byPicTransType == 0)) {

                        //FileOutputStream //fout;
                        try {
                            String filename = path + newName + "_fSimilarity_" + strFaceSnapMatch.fSimilarity + "_struBlackListInfo_pBuffer1" + ".jpg";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strFaceSnapMatch.struBlackListInfo.pBuffer1.getByteBuffer(offset, strFaceSnapMatch.struBlackListInfo.dwBlackListPicLen);
                            byte[] bytes = new byte[strFaceSnapMatch.struBlackListInfo.dwBlackListPicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            faceMatch.getStruBlackListInfo_pBuffer1().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    sAlarmType = sAlarmType + "：人脸黑名单比对报警，相识度：" + strFaceSnapMatch.fSimilarity + "，黑名单姓名：" + new String(strFaceSnapMatch.struBlackListInfo.struBlackListInfo.struAttribute.byName, "GBK").trim() + "，\n黑名单证件信息：" + new String(strFaceSnapMatch.struBlackListInfo.struBlackListInfo.struAttribute.byCertificateNumber).trim();

                    //获取人脸库ID
                    byte[] FDIDbytes;
                    if ((strFaceSnapMatch.struBlackListInfo.dwFDIDLen > 0) && (strFaceSnapMatch.struBlackListInfo.pFDID != null)) {
                        ByteBuffer FDIDbuffers = strFaceSnapMatch.struBlackListInfo.pFDID.getByteBuffer(0, strFaceSnapMatch.struBlackListInfo.dwFDIDLen);
                        FDIDbytes = new byte[strFaceSnapMatch.struBlackListInfo.dwFDIDLen];
                        FDIDbuffers.rewind();
                        FDIDbuffers.get(FDIDbytes);
                        sAlarmType = sAlarmType + "，人脸库ID:" + new String(FDIDbytes).trim();
                        faceMatch.setFacesID(new String(FDIDbytes).trim());
                    }
                    //获取人脸图片ID
                    byte[] PIDbytes;
                    if ((strFaceSnapMatch.struBlackListInfo.dwPIDLen > 0) && (strFaceSnapMatch.struBlackListInfo.pPID != null)) {
                        ByteBuffer PIDbuffers = strFaceSnapMatch.struBlackListInfo.pPID.getByteBuffer(0, strFaceSnapMatch.struBlackListInfo.dwPIDLen);
                        PIDbytes = new byte[strFaceSnapMatch.struBlackListInfo.dwPIDLen];
                        PIDbuffers.rewind();
                        PIDbuffers.get(PIDbytes);
                        sAlarmType = sAlarmType + "，人脸图片ID:" + new String(PIDbytes).trim();
                        faceMatch.setFaceID(new String(PIDbytes).trim());
                    }
                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    faceMatch.setTime(new Date());
                    faceMatch.setEquipmentIP(sIP[0]);
                    faceMatch.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    faceMatch.setBlackListName(new String(strFaceSnapMatch.struBlackListInfo.struBlackListInfo.struAttribute.byName));
                    faceMatch.setFSimilarity(strFaceSnapMatch.fSimilarity);
                    faceMatchRepository.save(faceMatch);
                    //alarmTableModel.insertRow(0, newRow);
                    break;
                case HCNetSDK.COMM_ALARM_ACS: //门禁主机报警信息
                    ACSAlarm acsAlarm=new ACSAlarm();
                    HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo = new HCNetSDK.NET_DVR_ACS_ALARM_INFO();
                    strACSInfo.write();
                    Pointer pACSInfo = strACSInfo.getPointer();
                    pACSInfo.write(0, pAlarmInfo.getByteArray(0, strACSInfo.size()), 0, strACSInfo.size());
                    strACSInfo.read();

                    sAlarmType = sAlarmType + "：门禁主机报警信息，卡号："+  new String(strACSInfo.struAcsEventInfo.byCardNo).trim() + "，卡类型：" +
                            strACSInfo.struAcsEventInfo.byCardType + "，报警主类型：" + strACSInfo.dwMajor + "，报警次类型：" + strACSInfo.dwMinor;

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    acsAlarm.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    acsAlarm.setTime(new Date());
                    acsAlarm.setDwCardReaderNo(strACSInfo.struAcsEventInfo.dwCardReaderNo);
                    acsAlarm.setDwMajor(strACSInfo.dwMajor);
                    acsAlarm.setDwMinor(strACSInfo.dwMinor);
                    acsAlarm.setEquipmentIP(sIP[0]);
                    acsAlarm.setIDCard(new String(strACSInfo.struAcsEventInfo.byCardNo).trim());
                    if(strACSInfo.dwPicDataLen>0)
                    {

                        //FileOutputStream //fout;
                        try {
                            String filename = path+ new String(pAlarmer.sDeviceIP).trim() +
                                    "_byCardNo["+ new String(strACSInfo.struAcsEventInfo.byCardNo).trim() +
                                    "_"+ newName + "_Acs.jpg";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strACSInfo.pPicData.getByteBuffer(offset, strACSInfo.dwPicDataLen);
                            byte [] bytes = new byte[strACSInfo.dwPicDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            acsAlarm.getImg().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    acsAlarmRepository.save(acsAlarm);
                    break;
                case HCNetSDK.COMM_ID_INFO_ALARM: //身份证信息
                    IDInfoAlarm idInfoAlarm=new IDInfoAlarm();
                    HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM strIDCardInfo = new HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM();
                    strIDCardInfo.write();
                    Pointer pIDCardInfo = strIDCardInfo.getPointer();
                    pIDCardInfo.write(0, pAlarmInfo.getByteArray(0, strIDCardInfo.size()), 0, strIDCardInfo.size());
                    strIDCardInfo.read();

                    sAlarmType = sAlarmType + "：门禁身份证刷卡信息，身份证号码："+  new String(strIDCardInfo.struIDCardCfg.byIDNum).trim() + "，姓名：" +
                            new String(strIDCardInfo.struIDCardCfg.byName).trim() + "，报警主类型：" + strIDCardInfo.dwMajor + "，报警次类型：" + strIDCardInfo.dwMinor;

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    idInfoAlarm.setTime(new Date());
                    idInfoAlarm.setEquipmentIP(sIP[0]);
                    idInfoAlarm.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    idInfoAlarm.setDwMajor(strIDCardInfo.dwMajor);
                    idInfoAlarm.setDwMinor(strIDCardInfo.dwMinor);
                    idInfoAlarm.setIDCard(new String(strIDCardInfo.struIDCardCfg.byIDNum).trim());
                    idInfoAlarm.setName(new String(strIDCardInfo.struIDCardCfg.byName).trim());
                    //身份证图片
                    if(strIDCardInfo.dwPicDataLen>0)
                    {

                        //FileOutputStream //fout;
                        try {
                            String filename = path+ new String(pAlarmer.sDeviceIP).trim() +
                                    "_byCardNo["+ new String(strIDCardInfo.struIDCardCfg.byIDNum ).trim() +
                                    "_"+ newName + "_IDInfoPic.jpg";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strIDCardInfo.pPicData.getByteBuffer(offset, strIDCardInfo.dwPicDataLen);
                            byte [] bytes = new byte[strIDCardInfo.dwPicDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            idInfoAlarm.getIdCardImg().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    //抓拍图片
                    if(strIDCardInfo.dwCapturePicDataLen >0)
                    {

                        //FileOutputStream //fout;
                        try {
                            String filename = path+ new String(pAlarmer.sDeviceIP).trim() +
                                    "_byCardNo["+ new String(strIDCardInfo.struIDCardCfg.byIDNum ).trim() +
                                    "_"+ newName + "_IDInfoCapturePic.jpg";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strIDCardInfo.pCapturePicData.getByteBuffer(offset, strIDCardInfo.dwCapturePicDataLen);
                            byte [] bytes = new byte[strIDCardInfo.dwCapturePicDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            idInfoAlarm.getSnapImg().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    //抓拍图片
                    if(strIDCardInfo.dwFingerPrintDataLen !=0)
                    {

                        //FileOutputStream //fout;
                        try {
                            String filename = path+ new String(pAlarmer.sDeviceIP).trim() +
                                    "_byCardNo["+ new String(strIDCardInfo.struIDCardCfg.byIDNum ).trim() +
                                    "_"+ newName + "_IDFingerPic.jpg";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strIDCardInfo.pFingerPrintData.getByteBuffer(offset, strIDCardInfo.dwFingerPrintDataLen);
                            byte [] bytes = new byte[strIDCardInfo.dwFingerPrintDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            idInfoAlarm.getFingerImg().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    idInfoAlarmRepository.save(idInfoAlarm);
                    break;
                case HCNetSDK.COMM_UPLOAD_AIOP_VIDEO: //设备支持AI开放平台接入，上传视频检测数据
                    AIOPVideo aiopVideo=new AIOPVideo();
                    HCNetSDK.NET_AIOP_VIDEO_HEAD struAIOPVideo = new HCNetSDK.NET_AIOP_VIDEO_HEAD();
                    struAIOPVideo.write();
                    Pointer pAIOPVideo = struAIOPVideo.getPointer();
                    pAIOPVideo.write(0, pAlarmInfo.getByteArray(0, struAIOPVideo.size()), 0, struAIOPVideo.size());
                    struAIOPVideo.read();

                    String strTime =  String.format("%04d", struAIOPVideo.struTime.wYear) +"-"+
                                String.format("%02d", struAIOPVideo.struTime.wMonth) +"-"+
                                String.format("%02d", struAIOPVideo.struTime.wDay) +" "+
                                String.format("%02d", struAIOPVideo.struTime.wHour) +":"+
                                String.format("%02d", struAIOPVideo.struTime.wMinute) +":"+
                                String.format("%02d", struAIOPVideo.struTime.wSecond);

                    sAlarmType = sAlarmType + "：AI开放平台接入，上传视频检测数据，通道号:" + struAIOPVideo.dwChannel +
                            ", 时间:" + strTime;

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    aiopVideo.setTime(new Date());
                    aiopVideo.setEquipmentIP(sIP[0]);
                    aiopVideo.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    aiopVideo.setSzTaskID(new String(struAIOPVideo.szTaskID));
                     if(struAIOPVideo.dwAIOPDataSize>0)
                    {

                        //FileOutputStream //fout;
                        try {
                            String filename = path+ new String(pAlarmer.sDeviceIP).trim() +
                                    "_"+ newName + "_AIO_VideoData.json";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = struAIOPVideo.pBufferAIOPData.getByteBuffer(offset, struAIOPVideo.dwAIOPDataSize);
                            byte [] bytes = new byte[struAIOPVideo.dwAIOPDataSize];
                            buffers.rewind();
                            buffers.get(bytes);
                            aiopVideo.getText().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if(struAIOPVideo.dwPictureSize>0)
                    {

                        //FileOutputStream //fout;
                        try {
                            String filename = path+ new String(pAlarmer.sDeviceIP).trim() +
                                    "_"+ newName + "_AIO_VideoPic.jpg";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = struAIOPVideo.pBufferPicture.getByteBuffer(offset, struAIOPVideo.dwPictureSize);
                            byte [] bytes = new byte[struAIOPVideo.dwPictureSize];
                            buffers.rewind();
                            buffers.get(bytes);
                            aiopVideo.getImg().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    aiopVideoRepository.save(aiopVideo);
                    break;
                case HCNetSDK.COMM_UPLOAD_AIOP_PICTURE: //设备支持AI开放平台接入，上传视频检测数据
                    AIOPPicture aiopPicture=new AIOPPicture();
                    HCNetSDK.NET_AIOP_PICTURE_HEAD struAIOPPic = new HCNetSDK.NET_AIOP_PICTURE_HEAD();
                    struAIOPPic.write();
                    Pointer pAIOPPic = struAIOPPic.getPointer();
                    pAIOPPic.write(0, pAlarmInfo.getByteArray(0, struAIOPPic.size()), 0, struAIOPPic.size());
                    struAIOPPic.read();

                    String strPicTime = String.format("%04d", struAIOPPic.struTime.wYear) +"-"+
                                String.format("%02d", struAIOPPic.struTime.wMonth) +"-"+
                                String.format("%02d", struAIOPPic.struTime.wDay) +" "+
                                String.format("%02d", struAIOPPic.struTime.wHour) +":"+
                                String.format("%02d", struAIOPPic.struTime.wMinute) +":"+
                                String.format("%02d", struAIOPPic.struTime.wSecond);

                    sAlarmType = sAlarmType + "：AI开放平台接入，上传图片检测数据，通道号:" + new String(struAIOPPic.szPID) +
                            ", 时间:" + strPicTime;

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    aiopPicture.setTime(new Date());
                    aiopPicture.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    aiopPicture.setEquipmentIP(sIP[0]);
                    aiopPicture.setSzPID(new String(struAIOPPic.szPID));
                     if(struAIOPPic.dwAIOPDataSize>0)
                    {

                        //FileOutputStream //fout;
                        try {
                            String filename = path+ new String(pAlarmer.sDeviceIP).trim() +
                                    "_"+ newName + "_AIO_PicData.json";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = struAIOPPic.pBufferAIOPData.getByteBuffer(offset, struAIOPPic.dwAIOPDataSize);
                            byte [] bytes = new byte[struAIOPPic.dwAIOPDataSize];
                            buffers.rewind();
                            buffers.get(bytes);
                            aiopPicture.getText().setImg(bytes);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                     aiopPictureRepository.save(aiopPicture);
                    break;
                case HCNetSDK.COMM_ISAPI_ALARM: //ISAPI协议报警信息
                    ISAPIAlarm isapiAlarm=new ISAPIAlarm();
                    HCNetSDK.NET_DVR_ALARM_ISAPI_INFO struEventISAPI = new HCNetSDK.NET_DVR_ALARM_ISAPI_INFO();
                    struEventISAPI.write();
                    Pointer pEventISAPI = struEventISAPI.getPointer();
                    pEventISAPI.write(0, pAlarmInfo.getByteArray(0, struEventISAPI.size()), 0, struEventISAPI.size());
                    struEventISAPI.read();

                    sAlarmType = sAlarmType + "：ISAPI协议报警信息, 数据格式:" + struEventISAPI.byDataType +
                            ", 图片个数:" + struEventISAPI.byPicturesNumber;

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    isapiAlarm.setTime(new Date());
                    isapiAlarm.setInfo(newRow[0]+" "+newRow[1]+" "+newRow[2]);
                    isapiAlarm.setEquipmentIP(sIP[0]);
                    isapiAlarm.setByDataType(struEventISAPI.byDataType);
                    isapiAlarm.setByPicturesNumber(struEventISAPI.byPicturesNumber);
                    SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMddHHmmss");
                    String curTime = sf1.format(new Date());
                    //FileOutputStream //foutdata;
                    try {
                        String jsonfilename = path + new String(pAlarmer.sDeviceIP).trim() + curTime +"_ISAPI_Alarm_" + ".json";
                        //foutdata = new //FileOutputStream(jsonfilename);
                        //将字节写入文件
                        ByteBuffer jsonbuffers = struEventISAPI.pAlarmData.getByteBuffer(0, struEventISAPI.dwAlarmDataLen);
                        byte [] jsonbytes = new byte[struEventISAPI.dwAlarmDataLen];
                        jsonbuffers.rewind();
                        jsonbuffers.get(jsonbytes);
                        isapiAlarm.getData().setImg(jsonbytes);
                        //foutdata.write(jsonbytes);
                        //foutdata.close();
                    }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                    }

                    for(int i=0;i<struEventISAPI.byPicturesNumber;i++)
                    {
                        HCNetSDK.NET_DVR_ALARM_ISAPI_PICDATA struPicData = new HCNetSDK.NET_DVR_ALARM_ISAPI_PICDATA();
                        struPicData.write();
                        Pointer pPicData = struPicData.getPointer();
                        pPicData.write(0, struEventISAPI.pPicPackData.getByteArray(i*struPicData.size(), struPicData.size()), 0, struPicData.size());
                        struPicData.read();

                        //FileOutputStream //fout;
                        try {
                            String filename = path + new String(pAlarmer.sDeviceIP).trim() + curTime +
                                    "_ISAPIPic_"+ i + "_" + new String(struPicData.szFilename).trim() +".jpg";
                            //fout = new //FileOutputStream(filename);
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = struPicData.pPicData.getByteBuffer(offset, struPicData.dwPicLen);
                            byte [] bytes = new byte[struPicData.dwPicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            ByData byData=new ByData();
                            byData.setImg(bytes);
                            isapiAlarm.getImgs().add(byData);
                            //fout.write(bytes);
                            //fout.close();
                        }  catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    isapiAlarmRepository.save(isapiAlarm);
                    break;
                default:
                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    //alarmTableModel.insertRow(0, newRow);
                    break;
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AlarmJavaDemoView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31
    {
        //报警信息回调函数

        public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser)
        {
            System.out.println(lCommand);
            AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
            return true;
        }
    }

    public class FMSGCallBack implements HCNetSDK.FMSGCallBack
    {
        //报警信息回调函数

        public void invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser)
        {
            AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        }
     }


    /*
        注册登录
     */
    private void jButtonLoginActionPerformed(String m_sDeviceIP,String m_sUsername,String m_sPassword,int m_sPort) {

        //注册之前先注销已注册的用户,预览情况下不可注销
        if (lUserID > -1) {
            //先注销
            DemoApplication.hCNetSDK.NET_DVR_Logout(lUserID);
            lUserID = -1;
        }

        //注册

        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());


        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());


        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

        m_strLoginInfo.wPort = (short)m_sPort;

        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是

        m_strLoginInfo.write();
        lUserID = DemoApplication.hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);

        if (lUserID == -1) {
            //JOptionPane.showMessageDialog(null, "注册失败，错误号:" +  DemoApplication.hCNetSDK.NET_DVR_GetLastError());
        } else {
            //JOptionPane.showMessageDialog(null, "注册成功");
        }
    }



    /*
        退出程序
     */
//    private void exitMenuItemMouseClicked() {
//        // TODO add your handling code here:
//        if (lAlarmHandle > -1)
//        {
//            DemoApplication.hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle);
//            lAlarmHandle = -1;
//        }
//        if (lUserID > -1) {
//            //先注销
//            DemoApplication.hCNetSDK.NET_DVR_Logout(lUserID);
//            lUserID = -1;
//        }
//        DemoApplication.hCNetSDK.NET_DVR_Cleanup();
//    }//GEN-LAST:event_exitMenuItemMouseClicked
//
//    //报警撤防
//    public void Logout() {
//        //报警撤防
//        if (lAlarmHandle > -1)
//        {
//            if(!DemoApplication.hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle))
//            {
//                 //JOptionPane.showMessageDialog(null, "撤防失败");
//            }
//            else
//            {
//                lAlarmHandle = -1;
//            }
//        }
//
//        //注销
//        if (lUserID > -1) {
//            if(DemoApplication.hCNetSDK.NET_DVR_Logout(lUserID))
//            {
//                //JOptionPane.showMessageDialog(null, "注销成功");
//                lUserID = -1;
//            }
//        }
//    }



    /*
        布防
     */
    public void SetupAlarmChan() {
        if (lUserID == -1)
        {
            //JOptionPane.showMessageDialog(null, "请先注册");
            return;
        }
         if (lAlarmHandles[lUserID] < 0)//尚未布防,需要布防
         {
                if (fMSFCallBack_V31 == null)
                {
                    fMSFCallBack_V31 = new FMSGCallBack_V31();
                    Pointer pUser = null;
                    if (!DemoApplication.hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser))
                    {
                        System.out.println("设置回调函数失败!");
                    }
                }
                HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
                m_strAlarmInfo.dwSize=m_strAlarmInfo.size();
                m_strAlarmInfo.byLevel=1;//智能交通布防优先级：0- 一等级（高），1- 二等级（中），2- 三等级（低）
                m_strAlarmInfo.byAlarmInfoType=1;//智能交通报警信息上传类型：0- 老报警信息（NET_DVR_PLATE_RESULT），1- 新报警信息(NET_ITS_PLATE_RESULT)
                m_strAlarmInfo.byDeployType =1; //布防类型(仅针对门禁主机、人证设备)：0-客户端布防(会断网续传)，1-实时布防(只上传实时数据)
                m_strAlarmInfo.write();
                lAlarmHandles[lUserID] = DemoApplication.hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
                if (lAlarmHandles[lUserID] == -1)
                {
                    System.out.println("布防失败，错误号:" +  DemoApplication.hCNetSDK.NET_DVR_GetLastError());
                    //JOptionPane.showMessageDialog(null, "布防失败，错误号:" +  DemoApplication.hCNetSDK.NET_DVR_GetLastError());
                }
                else
                {
                    System.out.println("布防成功" );
                    //JOptionPane.showMessageDialog(null, "布防成功");
                }
          }
    }

    public void CloseAlarmChan() {
        //报警撤防
        if (lAlarmHandles[lUserID] > -1)
        {
            if(DemoApplication.hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandles[lUserID]))
            {
                //JOptionPane.showMessageDialog(null, "撤防成功");
                lAlarmHandles[lUserID] = -1;
            }
        }
    }

     /*************************************************
    函数:      initialTableModel
    函数描述:	初始化报警信息列表,写入列名称
     *************************************************/


    public void StartAlarmListen() {
        Pointer pUser = null;

        if (fMSFCallBack == null)
        {
             fMSFCallBack = new FMSGCallBack();
        }
        lListenHandle = DemoApplication.hCNetSDK.NET_DVR_StartListen_V30(m_sListenIP, (short)iListenPort,fMSFCallBack, pUser);
        if(lListenHandle < 0)
        {
            //JOptionPane.showMessageDialog(null, "启动监听失败，错误号:" +  DemoApplication.hCNetSDK.NET_DVR_GetLastError());
        }
        else
        {
             //JOptionPane.showMessageDialog(null, "启动监听成功");
        }
    }

    public void StopAlarmListen() {
        if(lListenHandle < 0)
        {
            return;
        }

        if(!DemoApplication.hCNetSDK.NET_DVR_StopListen_V30(lListenHandle))
        {
            //JOptionPane.showMessageDialog(null, "停止监听失败");
        }
        else
        {
             //JOptionPane.showMessageDialog(null, "停止监听成功");
        }
    }

    public void OneTest() {

        HCNetSDK.NET_DVR_SNAPCFG struSnapCfg = new HCNetSDK.NET_DVR_SNAPCFG();
        struSnapCfg.dwSize=struSnapCfg.size();
        struSnapCfg.bySnapTimes =1;
        struSnapCfg.wSnapWaitTime =1000;
        struSnapCfg.write();

        if (false == DemoApplication.hCNetSDK.NET_DVR_ContinuousShoot(lUserID, struSnapCfg))
	{
            int iErr = DemoApplication.hCNetSDK.NET_DVR_GetLastError();
            //JOptionPane.showMessageDialog(null, "网络触发失败，错误号：" + iErr);
            return;
        }

    }
}


