package uk.ac.ncl.cs.zequn.swa.filesystem;

import uk.ac.ncl.cs.zequn.swa.model.Tuple;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitor;

import java.sql.*;

/**
 * @author ZequnLi
 *         Date: 14-4-26
 */
public class TupleAccess {
    private Connection connection;
    public TupleAccess(){
        this.connection = Dao.getInstance().getConnection();
    }
    public void init() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS `cache`");
        connection.createStatement().execute("CREATE TABLE `cache` (\n" +
                "  `size` int(11) NOT NULL,\n" +
                "  `result` double NOT NULL,\n" +
                "  `index` bigint(20) unsigned zerofill NOT NULL AUTO_INCREMENT,\n" +
                "  PRIMARY KEY (`index`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");

    }

    public void insertTuple(Tuple tuple)  {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `cache` (`size`, `result`) VALUES (?, ?)");
            preparedStatement.setInt(1,tuple.getSize());
            preparedStatement.setDouble(2,tuple.getResult());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Tuple getTuple()  {
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("select * from cache limit 1");
            if(resultSet.next()){
                int size = resultSet.getInt("size");
                double result = resultSet.getDouble("result");
                long index = resultSet.getLong("index");
                Statement statement = connection.createStatement();
                statement.execute("delete from cache where `cache`.`index`=" + index);
                statement.close();
                return new Tuple(size,result);
            }
            return null;

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
//
//    public static void main(String [] args) throws SQLException {
//        MemoryMonitor memoryMonitor = new MemoryMonitor(1000,null,null,null);
//        Tuple tuple = new Tuple(12,3222);
//        long counter = 0;
//        TupleAccess access = new TupleAccess();
//        access.init();
//        memoryMonitor.start();
//        while(true){
//            memoryMonitor.inputRateCheck();
//            counter++;
//            access.insertTuple(tuple);
//            if(counter %100 ==0){
//                System.out.println(counter);
//            }
//        }
//    }


}
