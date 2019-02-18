
package eventsinformation;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.sql.rowset.FilteredRowSet;
import newuserbean.*;

/**
 * Implementation for EventsManagerInfo interface.
 * Extends the capabilities of EventsInfoImpl to manager permission. Therfore 
 * able to add, delete and edit shows, events etc.
 * In order to instantiate this object, you need to provide besides for database
 * login information also a user and password for the web site that has manager
 * permissions.
 * @author Shani Shapiro
 */
public class EventsManagerInfoImpl extends EventsInfoImpl implements EventsManagerInfo {
    
    /**
     * Constructor for a new manager object enabling the receiving and editing 
     * of information in the db. In order to construct we need to get database
     * access (user, password), and another (user, password) for web site 
     * manager access.
     * @param dbUser username for database access.
     * @param dbPass the password for database access.
     * @param managerUserID username for web site manager access
     * @param managerPass the password for web site access.
     * @throws SQLException
     * @throws NoAccessException 
     */
    public EventsManagerInfoImpl(String dbUser, String dbPass, 
            String managerUserID, String managerPass) throws SQLException, 
            NoAccessException {
        super(dbUser, dbPass);
        super.setUser(managerUserID, managerPass);
        if(!super.isManager()) {
            throw new NoAccessException("User doesn't have manager access. ");
        }
        
    }
    
    /**
     * Constructor used to upgrade a "normal" user object of EventsInfoImpl to 
     * user with manager access permissions.
     * Should only be called for an EventsInfoImpl object that returns true on
     * isManager() method.
     * @param userInfo an object created for a user that has manager access to 
     * the web site.
     * @throws SQLException
     * @throws NoAccessException 
     */
    public EventsManagerInfoImpl(EventsInfoImpl userInfo) throws SQLException, NoAccessException {
        super(userInfo);
        if(!userInfo.isManager()) {
            throw new NoAccessException("User doesn't have manager access. ");
        }
        
    }
    
    @Override
    public synchronized boolean insertCity(String cityName) throws SQLException {
        int updateCount = 0;
        String query = "INSERT INTO cities VALUES (?) ";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);        
            pstat.setString(1, cityName);
            updateCount = pstat.executeUpdate();
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
        return updateCount != 0;
    }
    
    /**
     * Deletes rows with the given value from db table.
     * @param table the table db to delete from.
     * @param colomnName the column with the matching value.
     * @param value
     * @return
     * @throws SQLException
     * @throws NamingException 
     */
    private synchronized boolean deleteFromTable(String table, String columnName,
            String value) throws SQLException{
        int updateCount = 0;
        String query = "DELETE FROM " +  table + " WHERE " + columnName + " = ? ";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);        
            pstat.setString(1, value);
            updateCount = pstat.executeUpdate();
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
        return updateCount != 0;
    }
      
    @Override
    public boolean deleteCity(String cityName) throws SQLException, NamingException{
        return deleteFromTable("cities", "name", "cityName");
    }
    
    @Override
    public synchronized boolean insertCategory(String categoryName) throws SQLException {
        String query = "INSERT INTO show_category VALUES (?) ";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);     
            pstat.setString(1, categoryName);
            return pstat.executeUpdate() != 0;
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public boolean deleteCategory(String categoryName) throws SQLException {
        return deleteFromTable("show_category", "category_name", categoryName);
    }

    @Override
    public synchronized boolean insertTheater(TheaterBean theater) throws SQLException {
        String insertTheater = "INSERT INTO theaters (theater_name, " + 
                "theater_address, phone, website, city_name) values (?, ?, ?, ?, ?)";
        String insertSeats = "INSERT INTO seats (theater_code, row, seat) " +
                "VALUES (?, ?, ?)";
        PreparedStatement pstat = null;
        try {            
            
            pstat = createPreparedStatment(insertTheater);
            pstat.setString(1, theater.getName());
            pstat.setString(2, theater.getAddress());
            pstat.setString(3, theater.getPhone());
            pstat.setString(4, theater.getWebsite());
            pstat.setString(5, theater.getCity());
            
            conn.setAutoCommit(false);
            if(pstat.executeUpdate() == 0) {
                return false;
            }
            ResultSet generatedCode = pstat.getGeneratedKeys();
            generatedCode.next();
            theater.setCode(generatedCode.getInt(1));
            
                 
            
            createPreparedStatment(insertSeats);
            
            pstat.setInt(1, theater.getCode());
            for(int i=1; i <= theater.getRows(); i++) {
                for(int j=1; j <= theater.getSeats(); j++) {
                    pstat.setInt(2, i);
                    pstat.setInt(3, j);
                    pstat.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } 
        catch(Exception E) {
            if(conn != null) {
                System.err.print("Transaction is being rolled back");
                conn.rollback();
            }
            throw E;
        }
        finally {
            conn.setAutoCommit(true);
            if(pstat != null)
                pstat.close();
            close();
        }
    }
    
    @Override
    public boolean deleteTheater(int theaterCode) throws SQLException {
        return deleteFromTable("theaters", "theater_code", "theaterCode");
    }

    @Override
    public synchronized boolean insertShow(Show show) throws SQLException {
        String insertShow = "INSERT INTO shows (category_name, " + 
                "show_name, description, year_produced, show_length) values (?, ?, ?, ?, ?)";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(insertShow);
            pstat.setString(1, show.getCategory());
            pstat.setString(2, show.getName());
            pstat.setString(3, show.getDescription());
            pstat.setString(4, show.getYear());
            pstat.setString(5, show.getLength());
            
            return pstat.executeUpdate() != 0;
            
        }       
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public boolean deleteShow(int showCode) throws SQLException {
        return deleteFromTable("shows", "show_code", "showCode");

    }

    @Override
    public synchronized boolean insertEvent(EventBean event) throws SQLException {
        
        String insertEvent = "INSERT INTO events (event_date, " + 
                "event_time, show_code, theater_code) values (?, ?, ?, ?)";
        String insertTickets = "INSERT INTO tickets (event_code, seat_id, price) " +
                "VALUES (?, ?, ?)";
        PreparedStatement pstat = null;
        try {    
            ResultSet theaterSeats = getTheaterSeats(event.getTheater());
            
            pstat = createPreparedStatment(insertEvent);
            pstat.setDate(1, event.getDate());
            pstat.setTime(2, event.getTime());
            pstat.setInt(3, event.getShow());
            pstat.setInt(4, event.getTheater());
            
            conn.setAutoCommit(false);
            if(pstat.executeUpdate() == 0) {
                return false;
            }
            ResultSet generatedCode = pstat.getGeneratedKeys();
            generatedCode.next();
            event.setCode(generatedCode.getInt(1));           
                 
            createPreparedStatment(insertTickets);
            
            pstat.setInt(1, event.getCode());
            pstat.setBigDecimal(3, event.getPrice());
            while(theaterSeats.next()) {
                pstat.setInt(2, theaterSeats.getInt("seat_id"));
                pstat.executeUpdate();
            }
            
            conn.commit();
            return true;
        } 
        catch(Exception E) {
            if(conn != null) {
                System.err.print("Transaction is being rolled back");
                conn.rollback();
            }
            throw E;
        }
        finally {
            conn.setAutoCommit(true);  
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public boolean deleteEvent(int eventCode) throws SQLException {
        return deleteFromTable("events", "event_code", "eventCode");
    }

    @Override
    public synchronized FilteredRowSet getEventTickets(int eventCode) throws SQLException {
        String query = "SELECT * FROM tickets "
                + " NATURAL JOIN seats " 
                + " WHERE  event_code = ? "
                + " ORDER BY row, seat " ;
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query); 
            pstat.setInt(1, eventCode);
            ResultSet rs = pstat.executeQuery();

            FilteredRowSet filteredRS = factory.createFilteredRowSet();
            filteredRS.populate(rs);

            return filteredRS;
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    /**
     * Changes the price of tickets that the value in the given column is equal 
     * to the given value.
     * @param column the column we make the condition on.
     * @param value the value for the column
     * @param price the new price to set.
     * @throws NoAccessException if this object doesn't have permission(not manager).
     * @return true if successful, false otherwise.
     */
    private synchronized boolean setTicketPriceByColumn(String column, 
            int value, String price) throws SQLException {
        String query = "UPDATE tickets " +
            " SET price = ? " +
            " WHERE " + column + " = ? ";   
        PreparedStatement pstat = null;
        try {          
            pstat = createPreparedStatment(query);
            pstat.setString(1, price);
            pstat.setInt(2, value);
                        
            return pstat.executeUpdate() != 0;
            
        }       
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public boolean setTicketPriceByEvent(int eventCode, String price) throws SQLException {
        return setTicketPriceByColumn("event_code", eventCode, price);
    }

    @Override
    public boolean setTicketPrice(int ticketNo, String price) throws SQLException {
        return setTicketPriceByColumn("ticket_no", ticketNo, price);        
    }

    @Override
    public synchronized boolean setTicketsPriceByRow(int eventCode, int row, String price) 
            throws SQLException {
        String query = "UPDATE tickets AS T "
                + " SET price = ? "
                + " WHERE event_code = ? AND "
                + "(SELECT row FROM seats WHERE seat_id = T.seat_id) = ?";        
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);
            pstat.setString(1, price);
            pstat.setInt(2, eventCode);
            pstat.setInt(3, row);
                        
            return pstat.executeUpdate() != 0;
            
        }       
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public synchronized boolean reserveTicket(int ticketNo, String userID) throws SQLException {
        String reserveTicket = "UPDATE tickets " +
            " SET user_id = ?, assigned = yes " +
            " WHERE ticket_no = ? AND assigned = no ";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(reserveTicket);
            pstat.setString(1, userID);
            pstat.setInt(2, ticketNo);
                        
            return pstat.executeUpdate() != 0;
            
        }       
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public FilteredRowSet getUserTickets(String userID) throws SQLException {
        return super.getUserTickets(userID);
    }    

    @Override
    public UserBean getUserInfo(String userID) throws SQLException {
        UserBean user = new UserBean();
        user.setUserID(userID);
        getUserInfo(user);
        
        return user;
    }
    
    @Override
    public boolean setUserInfo(UserBean user) throws SQLException {
        return setAnyUserInfo(user.getUserID(), user);
    }
    
}
