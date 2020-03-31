package org.whz.webshell.util;

import java.util.concurrent.ConcurrentHashMap;

public class ThreadUtil {

    public static ConcurrentHashMap threadCountMap=new  ConcurrentHashMap<Long,Integer>();
    private static Integer threadNum=1;

    public static void setThreadName(){
        long id = Thread.currentThread().getId();
        if((threadCountMap.get(id))==null){
            threadCountMap.put(id,threadNum);
            Thread.currentThread().setName(Integer.toString(threadNum));
            threadNum++;
        }
    }
}
