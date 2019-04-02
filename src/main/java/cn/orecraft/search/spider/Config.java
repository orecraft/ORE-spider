package cn.orecraft.search.spider;

import java.io.*;
import java.util.Properties;

public class Config {
    public static Properties loadConfig(File f) throws IOException {
        if(!f.exists())
            return null;
        InputStream inputStream = new FileInputStream(f);
        Properties p =new Properties();
        p.load(inputStream);
        return p;
    }
}
