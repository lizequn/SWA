package uk.ac.ncl.cs.zequn.swa.filesystem;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author ZequnLi
 *         Date: 14-4-26
 */
public class Dao {
    private static Dao ourInstance = new Dao();
    private Connection connection;
    public static Dao getInstance() {
        return ourInstance;
    }

    private Dao()  {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost/swacache?"
            + "user=cache");
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
