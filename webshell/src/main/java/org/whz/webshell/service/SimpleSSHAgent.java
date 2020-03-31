package org.whz.webshell.service;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.whz.webshell.util.ThreadUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PropertySource("classpath:SshCfg/SSH.properties")
@Data
@Component
//@Scope("prototype")
public class SimpleSSHAgent extends BaseService {
    private static Logger logger = LoggerFactory.getLogger(SshService.class);

    @Value("${ssh.host}")
    private String host;
    @Value("${ssh.port}")
    private int port;
    @Value("${ssh.user}")
    private String user;
    @Value("${ssh.psd}")
    private String password;
    @Value("${ssh.session.timeout}")
    private int sessionTimeout = 1000;
    @Value("${ssh.remote.charset}")
    private String RemoteCharset;


    public static Connection conn;

    public SimpleSSHAgent() {
        ThreadUtil.setThreadName();

    }


    public void openConnecion() {
        try {
            conn = new Connection(host, port);
            conn.connect();
            boolean success = conn.authenticateWithPassword(user, password);
            if (!success) {

            }
        } catch (Exception e) {
            conn.close();
            conn = null;
            throw new RuntimeException("ssh2 验证失败 确认用户密码!");
        }
    }
    public void openConnecion(String host,String user,String password) {
            try {
                conn = new Connection(host, port);
                conn.connect();
                boolean success = conn.authenticateWithPassword(user, password);
                if (!success) {

                }
            } catch (Exception e) {
                conn.close();
                conn = null;
                throw new RuntimeException("ssh2 验证失败 确认用户密码!");
            }

}
    public Map<String, List<String>> execCMD(String CMD,Session session) {
        HashMap<String, List<String>> resultMap = new HashMap<>();
        ArrayList<String> successList = new ArrayList<>();
        ArrayList<String> errorList = new ArrayList<>();


        InputStream is = null;
        InputStream isr = null;

        BufferedReader br = null;
        BufferedReader brr = null;

        if (conn != null) {
            try {
//                session = conn.openSession();
//                session.requestDumbPTY();
//                session.startShell();
                session.execCommand(CMD);

                is = new StreamGobbler(session.getStdout());
                br = new BufferedReader(new InputStreamReader(is));

                isr = new StreamGobbler(session.getStderr());
                brr = new BufferedReader(new InputStreamReader(isr));

                while (true) {
                    String line = br.readLine();
                    if (!StringUtils.isEmpty(line)) {
                        successList.add(line);
                    }
                    if (line == null) {
                        break;
                    }
                }
                while (true) {
                    String line = brr.readLine();
                    if (!StringUtils.isEmpty(line)) {
                        errorList.add(line);
                    }
                    if (line == null) {
                        break;
                    }
                }
                resultMap.put("successList", successList);
                resultMap.put("errorList", errorList);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (brr != null) {
                    try {
                        brr.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                if (session != null) {
//                    session.close();
//                    session = null;
//                }

            }

        }

        return resultMap;
    }


    private static void setConn(Connection connection ){
        conn=connection;
    }
}
