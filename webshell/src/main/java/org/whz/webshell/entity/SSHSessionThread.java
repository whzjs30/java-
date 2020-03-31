package org.whz.webshell.entity;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSHSessionThread {
    @Getter
    @Setter
    private Long activeTime;
    @Getter
    @Setter
    private Session session;

    private BufferedReader stdout;
    private BufferedReader stderr;
    private PrintWriter printWriter;

    private List<String> std=new ArrayList<String>();

    private List<String> stderror=new ArrayList<String>();

    @Getter
    @Setter
    private Integer msgSize=0; //正确消息计数
    @Getter
    @Setter
    private Integer errorMsgSize=0;//错误消息计数

    private  Integer readLineCount=0; //已传递正确消息计数
    private  Integer readErrorLineCount=0;//已传递错误消息计数

    private Thread readThread;//读取正确消息得线程指针
    private Thread readErroThread;//读取错误消息得线程指针
    private boolean PTYinited=false;//PTY客户端初始化状态

    private static int threadNUM=0;//线程计数 没什么用

    /**
     * 通过PTY虚拟终端发消息
     * @param CMD
     */
    public void sendCMD(String CMD) {
        //初始化
        if (session != null) {
            if(!PTYinited) {
                try {
                    //混合流 错误/成功信息都在一个流里
                    session.requestDumbPTY();
                    session.startShell();
                    stdout = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout()),StandardCharsets.UTF_8));
                    printWriter=new PrintWriter(session.getStdin());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PTYinited=true;
            }

            //发送命令
            printWriter.write(CMD);
            printWriter.write("\r\n");
            printWriter.flush();
            if(readThread==null){
                readMsg();
            }
        }
    }

    /**
     * 读正确消息得线程
     */
    private void readMsg() {
        new Thread(()->{
            readThread=Thread.currentThread();
            readThread.setName("readThread"+threadNUM++);
            String line;
            try {
                while ((line = stdout.readLine()) != null){
                    synchronized (this) {
                        std.add(line);
                        msgSize++;
                    }
                }
            } catch (IOException e) {
            }catch (NullPointerException e){
                System.out.println("session超时");

            }finally {
                //todo
            }
        }).start();
    }
    /**
     * 读错误消息得线程
     */
    private void readErrorMsg() {
        new Thread(()->{
            readErroThread=Thread.currentThread();
            readThread.setName("readThread"+threadNUM++);
            String line;
            try {
                while ((line = stderr.readLine()) != null) {
                    stderror.add(line);
                    errorMsgSize++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }catch (NullPointerException e){
                System.out.println("session超时");
            }finally {
            }
        }).start();
    }

    //会话超时 关闭会话
    public synchronized void close(){
        try {
            if (session != null) {
                session.close();
                session=null;
            }
            IOUtils.closeQuietly(stdout);
            stdout=null;
            IOUtils.closeQuietly(stderr);
            stderr=null;
            IOUtils.closeQuietly(printWriter);
            printWriter=null;

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(readThread!=null&&readThread.isAlive()) {
                readThread.interrupt();//没有用
                readThread=null;
            }
            if(readErroThread!=null&&readErroThread.isAlive()) {
                readErroThread.interrupt();//没有用
                readThread=null;
            }
            PTYinited=false;
        }

    }

    /**
     * 策略 获取一定时间内SSH会话响应得消息
     * @return
     */
    public Map<String,List<String>> getResult(){
        HashMap<String, List<String>> resultMap = new HashMap<>();
        ArrayList<String> result;
        int loop= 3;
        for (int i = 1; i <= loop; i++) {
            try {
                Thread.sleep(i*i * 250L);
            } catch (InterruptedException e) {
            }

            if (getStd().size() > 0 || getStderror().size() > 0) {//收到消息了
                if (getStd().size() > 0) {
                    List<String> temp;
                    synchronized (this) {
                        temp = getStd();
                        int number = getMsgSize() - getReadLineCount();
                        if(number>0){
                            temp = temp.subList(temp.size() - number, temp.size());
                        }
                        result = new ArrayList<>(temp);
                        temp.clear();
                        resultMap.put("successList", result);
                        addReadCount(result.size());
                    }
                }

                if (getStderror().size() > 0) {
                    List<String> temp;
                    synchronized (this) {
                        temp = getStderror();
                        int number = getErrorMsgSize() -getReadErrorLineCount();
                        if(number>0){
                            temp = temp.subList(temp.size() - number, temp.size());
                        }
                        result = new ArrayList<>(temp);
                        temp.clear();
                        resultMap.put("errorList", result);
                        addErrorReadCount(result.size());
                    }
                }
                break;
            }
        }
        return resultMap;
    }
    public void addReadCount(int count){
        readLineCount+=count;
    }
    public void addErrorReadCount(int count){
        readErrorLineCount+=count;
    }

    public synchronized  Integer getReadLineCount() {
        return readLineCount;
    }

    public synchronized  Integer getReadErrorLineCount() {
        return readErrorLineCount;
    }
    public synchronized List<String> getStd() {
        return std;
    }

    public synchronized List<String> getStderror() {
        return stderror;
    }
}
