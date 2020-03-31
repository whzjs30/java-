package org.whz.webshell.service;

import ch.ethz.ssh2.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.whz.webshell.entity.SSHSessionThread;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 根据TOMCAT session 保存SSH会话
 */
@Service
public class SshService {

    private static final long sshSessionTimeout=1000L*60*2;
    private static Map<String, SSHSessionThread> SessionMap=new HashMap<>();


    @Autowired
    private SimpleSSHAgent SimpleSSHAgent;

    public void invalidSession(HttpServletRequest request, HttpServletResponse response){
        HttpSession httpsession = request.getSession(false);
        if(httpsession!=null){
            String currentHttpSessionId = httpsession.getId();
            SSHSessionThread sshSessionThread = SessionMap.get(currentHttpSessionId);
            if(sshSessionThread!=null){
                sshSessionThread.close();
            }
        }
    }
    public Map<String, List<String>> execCMD(HttpServletRequest request, HttpServletResponse response) {
        Map<String, List<String>> resultMap = new HashMap<>();
        String cmd=request.getParameter("cmd");
        String currentHttpSessionId=null;
        SSHSessionThread sshSessionInfo;
        //去除换行符
        cmd = check(cmd);
        if(!StringUtils.isEmpty(cmd)) {
            HttpSession httpsession = request.getSession(true);
            //是否首次登录
            try {
                currentHttpSessionId = httpsession.getId();
                sshSessionInfo = SessionMap.get(currentHttpSessionId);
                if (sshSessionInfo == null) {//会话不存在
                    if (SimpleSSHAgent.conn == null) {//连接初始化与否
                        //初始化连接
                        //是否自定义远程
                        if (request.getParameter("type") != null && request.getParameter("type").equals("remote")) {
                            SimpleSSHAgent.openConnecion(request.getParameter("host"), request.getParameter("user"), request.getParameter("psd"));
                        } else {//登录配置文件默认地址
                            SimpleSSHAgent.openConnecion();
                        }
                    }
                    Session newSSHSession = SimpleSSHAgent.conn.openSession();
                    sshSessionInfo = new SSHSessionThread();
                    sshSessionInfo.setSession(newSSHSession);
                    SessionMap.put(currentHttpSessionId, sshSessionInfo);
                } else {
                    synchronized (sshSessionInfo) {//防止超时检查线程同时操作
                    if (sshSessionInfo.getSession() == null) {//会话超时
                        if (SimpleSSHAgent.conn == null) {//连接初始化与否
                            //初始化连接
                            //是否自定义远程
                            if (request.getParameter("type") != null && request.getParameter("type").equals("remote")) {
                                SimpleSSHAgent.openConnecion(request.getParameter("host"), request.getParameter("user"), request.getParameter("psd"));
                            } else {//登录配置文件默认地址
                                SimpleSSHAgent.openConnecion();
                            }
                        }
                        Session newSSHSession = SimpleSSHAgent.conn.openSession();
                        sshSessionInfo.setSession(newSSHSession);
                        SessionMap.put(currentHttpSessionId, sshSessionInfo);
                    }
                }
            }
                    sshSessionInfo.setActiveTime(System.currentTimeMillis());
                } catch(Exception e){
                    e.printStackTrace();
                }

            //会话已建立
            SSHSessionThread INFO = SessionMap.get(currentHttpSessionId);
            Thread.currentThread().setName("callThread");
            //发送消息
            INFO.sendCMD(cmd);
            resultMap=INFO.getResult();
        }
        return resultMap;
    }


    @PostConstruct
    private void init() {
        Timer();
    }

    /**
     * 定时清除过期的SSH会话
     */
    private static void Timer(){
        new Thread(() -> {
            Thread.currentThread().setName("Timer");
            while (true){
                try {
                    Thread.sleep(1000L*60);
                }catch (InterruptedException e){
                }
                Iterator<Map.Entry<String, SSHSessionThread>> iterator = SessionMap.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<String, SSHSessionThread> next = iterator.next();
                    SSHSessionThread sshSessionInfo = next.getValue();
                    Long activeTime = sshSessionInfo.getActiveTime();
                    long currentTimeMillis = System.currentTimeMillis();
                    if(sshSessionInfo.getSession()!=null&&currentTimeMillis-activeTime>sshSessionTimeout){//超时就关闭SSH会话 释放引用
                        sshSessionInfo.close();
                        System.out.println("已无效Session");
                        next.setValue(sshSessionInfo);
                    }
                }
            }
        }){}.start();

    }
    public static String check(String cmd){
        if(StringUtils.isEmpty(cmd)){
            return "";
        }
        String s1 = cmd.replace("\n", " ");
        String s2 = s1.replace("\r", " ");
        String s3 = s2.toLowerCase();
        if(s3.equals("vi")||s3.equals("vim")){
            return "";
        }
        return s3;
    }

}
