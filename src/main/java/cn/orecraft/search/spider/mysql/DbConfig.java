package cn.orecraft.search.spider.mysql;

public class DbConfig {
    private String host;
    private int port;
    private String user;
    private String pass;
    private String database;

    public DbConfig(String host, int port, String user, String pass,String database) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPass() {
        return pass;
    }

    public String getDatabase() {
        return database;
    }
}
