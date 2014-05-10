package uk.ac.ncl.cs.zequn.swa.filesystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ZequnLi
 *         Date: 14-4-26
 */
public class LogAccess {
    private Connection connection;
    private final String table;
    public LogAccess(String name){
        this.table = name;
        this.connection = Dao4H2.getInstance().getConnection();
    }

    public String getTable() {
        return table;
    }

    public void init() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS `"+this.table+"`");
        connection.createStatement().execute("CREATE TABLE `"+this.table+"` (\n" +
                "  `logInfo` varchar NOT NULL,\n" +
                "  `index` bigint(20)  NOT NULL AUTO_INCREMENT,\n" +
                "  PRIMARY KEY (`index`)\n" +
                ") ");

    }
    /*
    CREATE TABLE `log` (

                `logInfo` varchar NOT NULL,
                `index` bigint(20)  NOT NULL AUTO_INCREMENT,
                PRIMARY KEY (`index`)
               )
     */

    public void insertTuple(String ...args )  {
        boolean flag = false;
        StringBuilder builder = new StringBuilder();

        for(String s:args){
            if(!flag){
                builder.append(s);
                flag = true;
            }else {
                builder.append(","+s);
            }
        }
//        builder.toString();
        try {
            PreparedStatement statement = connection.prepareStatement("Insert into "+table+" (logInfo) values (?)");
            statement.setString(1,builder.toString());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void output2CSV(String filePath,String fileName){
        File file = new File(filePath,fileName);
        if(file.exists()){
            throw new IllegalArgumentException("file exist");
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            ResultSet resultSet = connection.createStatement().executeQuery("select logInfo from "+table);
            while (resultSet.next()){
                String str = resultSet.getString("logInfo");
                bufferedWriter.write(str+"\n");
            }
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void main(String [] args) throws SQLException {
//        MemoryMonitorImpl memoryMonitor = new MemoryMonitorImpl(1000);
//        memoryMonitor.start();
//        LogAccess access = new LogAccess("log");
//        access.init();
//        for (int i = 0 ;i<100000;i++){
//            access.insertTuple("3","2","4");
//            memoryMonitor.inputRateCheck();
//        }
//
//        access.output2CSV("D:/","log.csv");
//        System.exit(0);
//    }


}
