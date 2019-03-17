package cn.orecraft.search.spider.mysql;

import org.apache.commons.lang3.StringUtils;
import com.sun.istack.internal.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class Db {
    private String table;
    private DbConfig config;
    private Connection connection;
    private final String driver = "com.mysql.jdbc.Driver";
    private ArrayList<String> wheres = new ArrayList<String>();
    private ArrayList<String> extCmd = new ArrayList<String>();
    private String inst_tpl = "INSERT INTO %table% %fields% values%values%;";
    private String select_tpl = "SELECT * FROM %table% %where%;";
    private String count_tpl = "SELECT COUNT(*) FROM %table% %where%;";
    private String update_tpl = "UPDATE %table% SET %data% %where%;";
    private String delete_tpl = "DELETE FROM %table% %where%;";
    public Db(DbConfig config){
        this.config=config;
    }
    public Db name(String name){
        this.table=name;
        return this;
    }
    public Db where(String field,String op,String value){
        if(wheres.size()==0){
            //第一次where不加and
            wheres.add(field+op+"'"+value.replace("'","\\'")+"'");
        }else{
            wheres.add("and");
            wheres.add(field+op+"'"+value.replace("'","\\'")+"'");
        }
        return this;
    }
    public Db whereOr(String field,String op,String value){
        if(wheres.size()==0){
            //第一次where不加or
            wheres.add(field+op+"'"+value.replace("'","\\'")+"'");
        }else{
            wheres.add("or");
            wheres.add(field+op+"'"+value.replace("'","\\'")+"'");
        }

        return this;
    }
    public int delete() throws SQLException {
        connect();
        String where_str=StringUtils.join(wheres," ");
        if(!where_str.equalsIgnoreCase("")){
            where_str=" where "+where_str;
        }
        String sql=delete_tpl;
        sql=sql.replace("%table%",this.table)
                .replace("%where%",where_str);
        java.util.logging.Logger.getAnonymousLogger().info(sql);
        return connection.prepareStatement(sql).executeUpdate();
    }
    public ResultSet select() throws SQLException {
        connect();
        String where_str=StringUtils.join(wheres," ");
        if(!where_str.equalsIgnoreCase("")){
            where_str=" where "+where_str;
        }
        String sql=select_tpl;
        sql=sql.replace("%table%",this.table)
        .replace("%where%",where_str);
        java.util.logging.Logger.getAnonymousLogger().info(sql);
        return connection.prepareStatement(sql).executeQuery();

    }
    public boolean insert(Map<String,String> map) throws SQLException {
        connect();
        //Set<Map.Entry<String, String>> entryseSet=map.entrySet();
        ArrayList<String> fields = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        for (Map.Entry<String,String> entry:map.entrySet()) {
            fields.add(entry.getKey());
            values.add("'"+entry.getValue().replace("'","\\'")+"'");
        }
        //Build sql
        String fields_str = StringUtils.join(fields,",");
        String values_str = StringUtils.join(values,",");
        String sql=inst_tpl
                .replace("%table%",this.table)
                .replace("%fields%","("+fields_str+")")
                .replace("%values%","("+values_str+")");
        java.util.logging.Logger.getAnonymousLogger().info(sql);
        return connection.prepareStatement(sql).execute();
    }
    public int update(Map<String,String> map) throws SQLException {
        connect();

        String where_str=StringUtils.join(wheres," ");
        if(!where_str.equalsIgnoreCase("")){
            where_str=" where "+where_str;
        }
        ArrayList<String> data=new ArrayList<String>();
        for (Map.Entry<String,String> entry:map.entrySet()) {
            data.add(entry.getKey()+"="+"'"+entry.getValue().replace("'","\\'")+"'"+" ");
        }
        String data_str = StringUtils.join(data,",");
        String sql=update_tpl;
        sql=sql.replace("%table%",this.table)
                .replace("%where%",where_str)
                .replace("%data%",data_str);
        java.util.logging.Logger.getAnonymousLogger().info(sql);
        return connection.prepareStatement(sql).executeUpdate();
    }
    public int count() throws SQLException {
        connect();
        String where_str=StringUtils.join(wheres," ");
        if(!where_str.equalsIgnoreCase("")){
            where_str=" where "+where_str;
        }
        String sql=count_tpl;
        sql=sql.replace("%table%",this.table)
                .replace("%where%",where_str);
        java.util.logging.Logger.getAnonymousLogger().info(sql);
        ResultSet resultSet = connection.prepareStatement(sql).executeQuery();
        if(resultSet.first()){
            return resultSet.getInt(1);
        }else{
            return 0;
        }

    }
    private void connect() throws SQLException {
        if(connection!=null&&!connection.isClosed()){  //如果已经连接的话，就不再连接了^-^
            return;
        }
        try {
            Class.forName(driver);
            connection = (Connection) DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s",config.getHost(),config.getPort(),config.getDatabase()), config.getUser(), config.getPass());
        }catch (ClassNotFoundException e){
            Logger.getLogger(this.getClass()).info("Final error!");
            e.printStackTrace();
        }

    }


}
