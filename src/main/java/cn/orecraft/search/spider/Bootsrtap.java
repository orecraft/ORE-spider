package cn.orecraft.search.spider;

import cn.orecraft.search.spider.mysql.Db;
import cn.orecraft.search.spider.mysql.DbConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Bootsrtap {
    public static void main(String[] args){
        Logger.getLogger("SPIDER").info("Starting OreCraft Web Spider.");
        DbConfig dcfg=new DbConfig("127.0.0.1",3306,"root","XIAOyifan2003", "mysql");
        Map<String,String> map = new HashMap<String, String>();
        map.put("aaa","a' or 1=1");
        map.put("bbb","b' or 1=1");
        try {
            System.out.print(new Db(dcfg).name("test_java").where("aaa","!=","aaa").delete());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
