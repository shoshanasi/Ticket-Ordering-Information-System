
package eventsinformation;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
 */
public class EventsManagerInfoImpl extends EventsInfoImpl implements EventsManagerInfo {
    
    /**
     * Constructor for a new manager object enabling the receiving and editing 
     * of information in the db. In order to construct we need to get database
     * access for manager (user, password) for web site.
     * @param managerUserID username for web site manager access
     * @param managerPass the password for web site access.
     * @throws SQLException
     * @throws NoAccessException 
     */
    public EventsManagerInfoImpl(String managerUserID, String managerPass) 
            throws SQLException, NoAccessException {
        super();
        super.setUser(managerUserID, managerPass);
        if(!super.isManager()) {
            throw new NoAccessException("User doesn't have manager access. ");
        }        
    }
    
    /**
     * Constructor for a new manager object enabling the receiving and editing 
     * of information in the db. In order to construct we need to get database
     * access for manager (user, password) from the userInfo provided
     * for the web site.
     * Should only be called for a userBean with manager access.
     * @param userInfo an object created for a user that has manager access to 
     * the web site.
     * @throws SQLException
     * @throws NoAccessException 
     */
    public EventsManagerInfoImpl(MainBean userInfo) throws SQLException, NoAccessException {
        super();
        super.setUser(userInfo);
        if(!super.isManager()) {
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
        String query = "DELETE FROM " +  table + " WHERE " + columnName + " = ? ";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);        
            pstat.setString(1, value);
            return pstat.executeUpdate() != 0;
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
        
    }
      
    @Override
    public boolean deleteCity(String cityName) throws SQLException {
        return deleteFromTable("cities", "city_name", cityName);
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
                "theater_address, theater_phone, website, city_name) values (?, ?, ?, ?, ?)";
        String insertSeats = "INSERT INTO seats (theater_code, seats.row, seat) " +
                "VALUES (?, ?, ?)";
        PreparedStatement pstat = null;
        PreparedStatement pstatthe = null;
        
        try {            
            pstat = createPreparedStatmentKey(insertTheater);
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
                 
            pstatthe = createPreparedStatment(insertSeats);            
            pstatthe.setInt(1, theater.getCode());
            
            for(int i=1; i <= theater.getRows(); i++) {
                for(int j=1; j <= theater.getSeats(); j++) {
                    pstatthe.setInt(2, i);
                    pstatthe.setInt(3, j);
                    pstatthe.addBatch();
                }
            }
            pstatthe.executeBatch();
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
            if(pstatthe != null)
                pstatthe.close();
            close();
        }
    }
    
    @Override
    public boolean editTheater(TheaterBean theater) throws SQLException {
        String updateTheater = "UPDATE theaters SET theater_name = ?, "
                + "theater_address = ?,	theater_phone = ?, website = ?, "
                + "city_name = ? WHERE theater_code = ? ";
        PreparedStatement pstat = null;        
        try {            
            pstat = createPreparedStatment(updateTheater);
            pstat.setString(1, theater.getName());
            pstat.setString(2, theater.getAddress());
            pstat.setString(3, theater.getPhone());
            pstat.setString(4, theater.getWebsite());
            pstat.setString(5, theater.getCity());
            pstat.setInt(6, theater.getCode());
            
            return pstat.executeUpdate() != 0;            
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
        return deleteFromTable("theaters", "theater_code", String.valueOf(theaterCode));
    }

    @Override
    public synchronized boolean insertShow(ShowBean show) throws SQLException {
        String insertShow = "INSERT INTO shows (category_name, " + 
                "show_name, description, year_produced, show_length) values (?, ?, ?, ?, ?)";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatmentKey(insertShow);
            pstat.setString(1, show.getCategory());
            pstat.setString(2, show.getName());
            pstat.setString(3, show.getDescription());
            pstat.setString(4, show.getYear());
            pstat.setString(5, show.getLength());
            
            conn.setAutoCommit(false);
            if(0 == pstat.executeUpdate()) {
                return false;
            }
            
            ResultSet generatedCode = pstat.getGeneratedKeys();
            generatedCode.next();
            show.setShowCode(generatedCode.getInt(1));
            
            conn.commit();
            return true;            
        }   
        catch(Exception E) {
            if(conn != null) {
                System.err.print(E.toString());
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
    public boolean deleteShow(int showCode) throws SQLException {
        return deleteFromTable("shows", "show_code", String.valueOf(showCode));
    }

    @Override
    public synchronized boolean insertEvent(EventBean event) throws SQLException {
        String insertEvent = "INSERT INTO events (event_date, " + 
                "event_time, show_code, theater_code) values (?, ?, ?, ?)";
        String insertTickets = "INSERT INTO tickets (event_code, seat_id, price, assigned) " +
                "VALUES (?, ?, ?, 'yes')";
        String insertUnassignedTickets = "INSERT INTO tickets (event_code, price, assigned) " +
                "VALUES (?, ?, 'no')";
        PreparedStatement pstatEvent = null;
        PreparedStatement pstatTickets = null;
        PreparedStatement pstatUnassignedTickets = null;
        try {    
            ResultSet theaterSeats = getTheaterSeats(event.getTheaterCode());
            
            pstatEvent = createPreparedStatmentKey(insertEvent);
            pstatEvent.setDate(1, event.getSqlDate());
            pstatEvent.setTime(2, event.getSqlTime());
            pstatEvent.setInt(3, event.getShow());
            pstatEvent.setInt(4, event.getTheaterCode());
            
            conn.setAutoCommit(false);
            if(pstatEvent.executeUpdate() == 0) {
                return false;
            }
            ResultSet eventCode = pstatEvent.getGeneratedKeys();
            eventCode.next();
            event.setCode(eventCode.getInt(1));           
                 
            pstatTickets = createPreparedStatment(insertTickets);            
            pstatTickets.setInt(1, event.getCode());
            pstatTickets.setBigDecimal(3, event.getPrice());
            int numSeats = 0;
            while(theaterSeats.next()) {
                pstatTickets.setInt(2, theaterSeats.getInt("seat_id"));
                pstatTickets.addBatch();
                numSeats++;
            }
            pstatUnassignedTickets = createPreparedStatment(insertUnassignedTickets);            
            pstatUnassignedTickets.setInt(1, event.getCode());
            pstatUnassignedTickets.setBigDecimal(2, event.getPrice());
            for(int i=0; i < event.getUnassignedSeats(); i++) {
                pstatUnassignedTickets.addBatch();
                numSeats++;
            }
            
            pstatTickets.executeBatch();
            pstatUnassignedTickets.executeBatch();
            conn.commit();
            event.setEmptySeats(numSeats);
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
            if(pstatEvent != null)
                pstatEvent.close();
            close();
        }
    }

    @Override
    public boolean deleteEvent(int eventCode) throws SQLException {
        return deleteFromTable("events", "event_code", String.valueOf(eventCode));
    }
    
    /**
     * Gets a FilteredRowSet containing rows with all theatres, every theatre
     * has the number of rows and seats in a row.
     * @return FilteredRowSet with all theatres in db.
     * @throws SQLException 
     */
    @Override
    public FilteredRowSet getTheaters() throws SQLException {
        String query = "SELECT T.*, COUNT(*) AS theater_rows, S.row_seats "
                + " FROM theaters AS T "
                + " NATURAL JOIN (SELECT SE.theater_code, COUNT(*) AS row_seats "
                    + " FROM seats AS SE GROUP BY SE.theater_code, SE.row) AS S "
                + " GROUP BY T.theater_code;";
        ResultSet rs;
        FilteredRowSet filteredRS;
        PreparedStatement pstat = null;
        try {
            filteredRS = factory.createFilteredRowSet();
            synchronized(this) {
                pstat = createPreparedStatment(query); 
                rs = pstat.executeQuery();
                filteredRS.populate(rs);
            }
        }
        catch(SQLException ex) {
            throw ex;
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }               
        return filteredRS;
    }

    @Override
    public synchronized FilteredRowSet getEventTickets(int eventCode) throws SQLException {
        String query = "SELECT S.*, T.*, "
                + " T.user_id IS NULL AS available "
                + " FROM tickets AS T "
                + " LEFT JOIN seats AS S "
                + " ON T.seat_id = S.seat_id " 
                + " WHERE event_code = ? "
                + " ORDER BY S.row, S.seat " ;
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
            " SET user_id = ? " +
            " WHERE ticket_no = ? AND user_id IS NULL ";
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
    public MainBean getUserInfo(String userID) throws SQLException {
        MainBean user = new MainBean();
        user.setUserID(userID);
        getUserInfo(user);
        
        return user;
    }
    
    @Override
    public boolean setUserInfo(MainBean user) throws SQLException {
        return setAnyUserInfo(user.getUserID(), user);
    }
    
    @Override
    public boolean setUserInfo(User user) throws SQLException {
        return setAnyUserInfo(user.getUserID(), user);
    }

    @Override
    public FilteredRowSet getUsers() throws SQLException {
        return getTableColumns("user_id, user_name, email,"
                + " user_address, user_phone,  city_name ", "users");
    }
    
    
    
    private List<String> getTableList(String tableName, String colomnName, String initial) throws SQLException {
        ArrayList list;
        ResultSet rs = null;
        PreparedStatement pstat = null;
        try {
            String query = "SELECT " + colomnName + " FROM " +
                 tableName + " WHERE " + colomnName + " LIKE ?";
            
            synchronized(this) {
                pstat = createPreparedStatment(query);     
                pstat.setString(1, initial + "%");
                rs = pstat.executeQuery();
                list = new ArrayList();
                while(rs.next()) {
                    list.add(rs.getString(colomnName));
                }
            }
        }
        finally {
            if(rs != null)
                rs.close();
            if(pstat != null)
                pstat.close();
            close();
        }
        return list;
    }

    @Override
    public boolean editShow(ShowBean show) throws SQLException {
        String updateShow = "UPDATE shows SET category_name = ?, "
                + "show_name = ?, description = ?, year_produced = ?, "
                + " show_length = ? WHERE show_code = ? ";
        PreparedStatement pstat = null;        
        try {            
            pstat = createPreparedStatment(updateShow);
            pstat.setString(1, show.getCategory());
            pstat.setString(2, show.getName());
            pstat.setString(3, show.getDescription());
            pstat.setString(4, show.getYear());
            pstat.setString(5, show.getLength());
            pstat.setInt(6, show.getShowCode());
                        
            return pstat.executeUpdate() != 0;            
        } 
        finally {
            conn.setAutoCommit(true);
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public boolean editEvent(EventBean event) throws SQLException {
        String updateEvent = "UPDATE events SET event_date = ?, "
                + "event_time = ?, theater_code = ?, show_code = ? "
                + " WHERE event_code = ? ";
        PreparedStatement pstat = null;        
        try {            
            pstat = createPreparedStatment(updateEvent);
            pstat.setDate(1, event.getSqlDate());
            pstat.setTime(2, event.getSqlTime());
            pstat.setInt(3, event.getTheaterCode());
            pstat.setInt(4, event.getShow());
            pstat.setInt(5, event.getCode());
                        
            return pstat.executeUpdate() != 0;            
        } 
        finally {
            conn.setAutoCommit(true);
            if(pstat != null)
                pstat.close();
            close();
        }
    }
    
    @Override
    public FilteredRowSet searchEvents(Date fromDate, Date toDate, 
            Integer showCode, String city) throws SQLException {
        String query = "SELECT * FROM events "
                + " NATURAL JOIN shows "
                + " NATURAL JOIN theaters "
                + " NATURAL JOIN (SELECT event_code, SUM(user_id IS NULL) AS available "
                + " FROM tickets GROUP BY event_code) "
                + " AS available_tickets "
                + " WHERE ";
        if(city != null) {
            query += " city_name = ? ";
        }
        else {
            query += " ? ";
        }
        
        if(toDate != null) {
            query += " AND event_date >= ? ";
        }
        else {
            query += " AND ? ";
        }
        if(toDate != null) {
            query += " AND event_date <= ? ";
        }
        else {
            query += " AND ? ";
        }
        if(showCode != null) {
            query += " AND show_code = ? ";
        }
        else {
            query += " AND ? ";
        }                
        query += " ORDER BY event_date, event_time ";
        
        PreparedStatement pstat = null;
        FilteredRowSet filteredRS;
        try {
            synchronized(this) {
                pstat = createPreparedStatment(query); 
                if(city != null) {
                    pstat.setString(1, city);
                }
                else {
                    pstat.setBoolean(1, true);
                }
                if(fromDate != null) {
                    pstat.setDate(2, fromDate);
                }  
                else {
                    pstat.setDate(2, new Date((new java.util.Date()).getTime()));
                }
                if(toDate != null) {
                    pstat.setDate(3, toDate);
                }  
                else {
                    pstat.setBoolean(3, true);
                }
                if(showCode != null) {
                    pstat.setInt(4, showCode);
                }
                else {
                    pstat.setBoolean(4, true);
                }                               
                ResultSet rs = pstat.executeQuery();

                filteredRS = factory.createFilteredRowSet();
                filteredRS.populate(rs);
            }
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
        return filteredRS;
    }
    
    @Override
    public synchronized FilteredRowSet searchShow(String category, String name, String year)
            throws SQLException {        
        String query = "SELECT S.* " + 
                "FROM show_category AS SC " +
                "NATURAL JOIN shows AS S " +
                "WHERE SC.category_name LIKE ? " +
                "AND S.show_name LIKE ? " +
                "AND S.year_produced LIKE ? " +
                "GROUP BY show_code";

        ResultSet rs;
        PreparedStatement pstat = null;
        FilteredRowSet filteredRS;
        try {
            pstat = createPreparedStatment(query); 
            for(int i=1; i <= 3; i++) {
                pstat.setString(i, "%");
            }
            if((category != null))
                pstat.setString(1, category);
            if((name != null))
                pstat.setString(2, name);
            if((year != null))
                pstat.setString(3, year);
            
            rs = pstat.executeQuery();
            filteredRS = factory.createFilteredRowSet();
            filteredRS.populate(rs);   
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
        return filteredRS;              

    }
    
    
    
    
    
}
