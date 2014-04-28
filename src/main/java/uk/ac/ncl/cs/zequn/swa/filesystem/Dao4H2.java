package uk.ac.ncl.cs.zequn.swa.filesystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author ZequnLi
 *         Date: 14-4-26
 */
public class Dao4H2 {
    private static Dao4H2 ourInstance = new Dao4H2();
    private Connection connection;
    public static Dao4H2 getInstance() {
        return ourInstance;
    }

    private Dao4H2()  {
        try {
            Class.forName("org.h2.Driver");
            //this.connection = DriverManager.getConnection("jdbc:h2:mem:log;DB_CLOSE_DELAY=-1");
            this.connection = DriverManager.getConnection("jdbc:h2:tcp://localhost/mem:log");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Connection getConnection(){
        return this.connection;
    }
    public void closeConnection() throws SQLException {
        this.connection.close();
    }
}
