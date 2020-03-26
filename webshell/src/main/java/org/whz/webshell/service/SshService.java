package org.whz.webshell.service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;

@PropertySource("classpath:SshCfg/SSH.properties")
public class SshService {

    @Value("${ssh.host}")
    private String host;
    @Value("${ssh.use}")
    private int port ;
    @Value("${ssh.use}")
    private String user ;
    @Value("${ssh.use}")
    private String password ;

    @Setter
    @Getter
    private Session Jschsession;
    public SshService() {
        init();
    }

    protected   void init()  {
        try {

            JSch jSch = new JSch();
            Session session = jSch.getSession(user,host,port);
            session.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");  // 跳过检测
            session.setConfig(sshConfig);
            this.setJschsession(session);

        }catch (JSchException e){
            e.printStackTrace();
        }
    }
}
