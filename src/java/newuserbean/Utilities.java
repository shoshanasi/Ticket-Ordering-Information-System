
package newuserbean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Utilities {
    protected static Connection getConenction() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/mydb" +
                "?autoReconnect=true&useSSL=false";
        Connection con = DriverManager.getConnection(url, "user", "user");
        return con;
    }
    
    public static void close(AutoCloseable resource) {
       try {
           if(resource != null) {
               resource.close();
           }
       } catch (Exception ex) {
            Logger.getLogger(MainBean.class.getName()).log(Level.SEVERE, null, ex);
       }       
   }
}
