
package eventsinformation;

import newuserbean.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.*;
import javax.sql.*;
import javax.sql.rowset.*;

/**
 * Implementation for EventsInfo interface.
 * This class establishes the connection and fetches the information of the 
 * various methods from the Events web site database. It also remembers the 
 * user logged in if provided this information. 
 * After a user was registered to this class it can't be changed.
 * @author Shani Shapiro
 */
public class EventsInfoImpl implements EventsInfo {
          
    Connection conn;
    final RowSetFactory factory;
    private DataSource ds;
    
    private String clientUserID;
    private boolean isManager;
    
    /**
     * Constructor for a new object getting information from the db, but not
     * associated with any user.
     * @throws SQLException 
     */
    public EventsInfoImpl() throws SQLException {
                      
        conn = null;
        factory = RowSetProvider.newFactory();
        clientUserID = null;
        isManager = false;
        try {
            Context ctx = new InitialContext();
            ds = (DataSource)ctx.lookup("jdbc/myDatasource");
        }
        catch (NamingException ex) {
            Logger.getLogger(EventsInfoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    public static List<String> rowSetToList(RowSet rs, String columnLable) throws SQLException {
        List<String> columnList = new ArrayList<>();
        rs.beforeFirst();
        while(rs.next()) {
            columnList.add(rs.getString(columnLable));
        }
        return columnList;
    }
           
    /**
     * Get a PreparedStatment with the given query 
     * by establishing a connection to database if needed. 
     * @param query the query we want to send to the database.
     * @param generateKey true if the statement needs to return the db 
     * generated key.
     * @return PreparedStatement
     * @throws SQLException 
     */
    synchronized PreparedStatement createPreparedStatment(String query, 
            boolean generateKey) throws SQLException {
        
        try {
            if(conn == null) {                
                conn = ds.getConnection();                
            }
            if(generateKey) {
                return conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            }                        
            return conn.prepareStatement(query);
        }
        catch (SQLException e) {
            close();
            throw e;
        }
    }
    
    /*
    * Calling createPreparedStatment(query, true).
    */
    synchronized PreparedStatement createPreparedStatmentKey(String query) 
            throws SQLException {
        return createPreparedStatment(query, true);
    }
    
    /*
    * Calling createPreparedStatment(query, false).
    */
    synchronized PreparedStatement createPreparedStatment(String query) 
            throws SQLException {
        return createPreparedStatment(query, false);
    }  
    
    @Override
    public synchronized void close() {
        try {
            if(conn != null)
                conn.close();            
        } catch (SQLException ex) {
            Logger.getLogger(EventsInfoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            conn = null;       
        }
    }
    
    
    
    @Override
    public boolean isManager() {
        return isManager;
    }
    
    @Override
    public String getUserID() {
        return clientUserID;
    }
    
    /**
     * Gets an entire table information from the database.
     * @param tableName name of table to receive.
     * @return a FilteredRowSet with all table rows.
     * @throws SQLException 
     */
    private FilteredRowSet getTable(String tableName) throws SQLException {
        
        return getTableColumns("*", tableName);
    }
    
    /**
     * Gets all the entries from a chosen table with the specified columns 
     * from the database.
     * @param columns table columns to receive.
     * @param tableName name of table to receive.
     * @return a FilteredRowSet with all table rows and chosen fields.
     * @throws SQLException 
     */
    FilteredRowSet getTableColumns(String columns, String tableName) throws SQLException {
        String query = "SELECT " + columns + " FROM " + tableName;
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
    
    public List<String> getCitiesList() throws SQLException {
        return getCitiesList("");
    }
    
    @Override
    public FilteredRowSet getCities() throws SQLException {
        return getTable("cities");
    }

    @Override
    public List<String> getCitiesList(String initial) throws SQLException {
        return getTableList("cities", "city_name", initial);
    }
    
    /**
     * Gets a list of the column values from the table that start with the given initial.
     * @param tableName the table to query.
     * @param colomnName the column for which to check and get the values.
     * @param initial the beginning of the word required.
     * @return a List of matching column values.
     * @throws SQLException 
     */
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
    
    /**
     * Gets a FilteredRowSet with rows containing column values from the table
     * that start with the given initial.
     * @param tableName the table to query.
     * @param colomnName the column for which to check and get the values.
     * @param initial the beginning of the word required.
     * @return
     * @throws SQLException 
     */
    private FilteredRowSet getColumnFilteredRS(String tableName, 
            String colomnName, String initial) throws SQLException {
        FilteredRowSet filteredRS;
        PreparedStatement pstat = null;
        try {
            filteredRS = factory.createFilteredRowSet();
            String query = "SELECT " + colomnName + " FROM " 
                    + tableName + " WHERE " + colomnName + " LIKE ?";
            
            synchronized(this) {
                pstat = createPreparedStatment(query);     
                pstat.setString(1, initial + "%");
                ResultSet rs = pstat.executeQuery();
                filteredRS.populate(rs); 
            }
            
            ListPredicate pr = new ListPredicate(initial, colomnName);
            filteredRS.setFilter(pr);
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
    public List<String> getCategoriesList() throws SQLException {
        return getCategoriesList("");
    }
    
    @Override
    public FilteredRowSet getCategories() throws SQLException {
        return getTable("show_category");
    }
    
    @Override
    public List<String> getCategoriesList(String initial) throws SQLException {
        return getTableList("show_category", "category_name", initial);
    }

    @Override
    public synchronized boolean insertUser(MainBean user) throws SQLException, NoAccessException {
        if(clientUserID != null) {
            throw new NoAccessException("User already logedd in.");
        }
        String query = "INSERT INTO users (user_id, password, user_name, "
                + "permission, email, user_address, user_phone, city_name) values (?, ?, ?, "
                + "?, ?, ?, ?, ?)";
        PreparedStatement pstat = null;
        int update = 0;
        try {
            
            pstat = createPreparedStatment(query);             
            pstat.setString(1, user.getUserID());
            pstat.setString(2, user.getPassword());
            pstat.setString(3, user.getName());
            pstat.setString(4, "user");
            pstat.setString(5, user.getEmail());
            pstat.setString(6, user.getAddress());
            pstat.setString(7, user.getPhone());
            pstat.setString(8, user.getCity());

            update = pstat.executeUpdate();
            
            
            if(update != 0) {
                clientUserID = user.getUserID();
                return true;
            }
            else {
                return false;
            }
        } 
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    
    @Override
    public boolean editUser(MainBean user) throws SQLException, NoAccessException {
        if(clientUserID == null) {
            throw new NoAccessException("User not logedd in.");
        }
        String updateUser = "UPDATE users SET "
                + " email = ?, user_address = ?, user_phone = ? ";
        if(user.getPassword() != null) {
            updateUser += ", password = ? ";
        }
        if(user.getCity() != null) {
            updateUser += ", city_name = ? ";
        }
        if(user.getName() != null) {
            updateUser += ", user_name = ? ";
        }
        updateUser  += " WHERE user_id = ? ";
        PreparedStatement pstat = null;        
        try {            
            pstat = createPreparedStatment(updateUser);            
            pstat.setString(1, user.getEmail());
            pstat.setString(2, user.getAddress());
            pstat.setString(3, user.getPhone());
            int placeHolder = 4;
            if(user.getPassword() != null) {
                pstat.setString(placeHolder++, user.getPassword());
            }
            if(user.getCity() != null) {
                pstat.setString(placeHolder++, user.getCity());
            }
            if(user.getName() != null) {
                pstat.setString(placeHolder++, user.getName());
                updateUser += ", user_name = ? ";
            }
            pstat.setString(placeHolder, clientUserID);
            
            return pstat.executeUpdate() != 0;            
        } 
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }
    
    
    @Override
    public synchronized boolean setUser(String user, String pass) throws SQLException, NoAccessException {
        if(clientUserID != null) {
            throw new NoAccessException("User already logedd in.");
        }
        String query = "SELECT user_id, permission FROM users WHERE user_id = ? " +
                "AND password = ? ";
        ResultSet rs = null;
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query); 
            pstat.setString(1, user);
            pstat.setString(2, pass);
            rs = pstat.executeQuery();
            if(rs.next()) {         
                clientUserID = user;
                isManager = rs.getString("permission").equals("admin");
                return true;
            }
            else {
                return false;
            }
        }
        finally {
            if(rs != null)
                rs.close();
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public boolean validUserName(String userName) throws SQLException {
        String query = "SELECT * FROM users WHERE user_id = ? ";
        PreparedStatement pstat = null;
        try {
            synchronized(this) {
                pstat = createPreparedStatment(query); 
                pstat.setString(1, userName);
                return !pstat.executeQuery().next();
            }
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public synchronized boolean setUser(MainBean user) throws SQLException, NoAccessException {
        if(clientUserID != null) {
            throw new NoAccessException("User already logedd in.");
        }
        String userID = user.getUserID();
        String pass = user.getPassword();
        String query = "SELECT * FROM users WHERE user_id = ? " +
                "AND password = ? ";
        ResultSet rs = null;
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query); 
            pstat.setString(1, userID);
            pstat.setString(2, pass);
            rs = pstat.executeQuery();
            if(rs.next()) {
                clientUserID = userID;
                user.setName(rs.getString("user_name"));
                user.setEmail(rs.getString("email"));
                user.setAddress(rs.getString("user_address"));
                user.setPhone(rs.getString("user_phone"));
                user.setCity(rs.getString("city_name"));
                
                clientUserID = userID;
                isManager = rs.getString("permission").equals("admin");
                return true;
            }
            else {
                return false;
            }
        } 
        finally {
            if(rs != null)
                rs.close();
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public synchronized FilteredRowSet searchShow(String category, String name, String year, Date date, String city)
            throws SQLException {        
        String query = "SELECT S.* " + 
                "FROM show_category AS SC " +
                "NATURAL JOIN shows AS S " +
                "NATURAL JOIN events AS E " +
                "NATURAL JOIN theaters AS T " +
                "NATURAL JOIN cities AS C " +
                "WHERE SC.category_name LIKE ? " +
                "AND S.show_name LIKE ? " +
                "AND S.year_produced LIKE ? " +                
                "AND C.city_name LIKE ? ";
        if((date != null)) {
            query += "AND E.event_date LIKE ? ";
        }
        else {
            query += "AND E.event_date >= ? ";
        }        
        query += " GROUP BY show_code ";

        ResultSet rs;
        PreparedStatement pstat = null;
        FilteredRowSet filteredRS;
        try {
            pstat = createPreparedStatment(query); 
            for(int i=1; i <= 5; i++) {
                pstat.setString(i, "%");
            }
            if((category != null))
                pstat.setString(1, category);
            if((name != null))
                pstat.setString(2, name);
            if((year != null))
                pstat.setString(3, year);            
            if((city != null))
                pstat.setString(4, city);
            if((date != null)) {
                pstat.setDate(5, date);
            }
            else {
                pstat.setDate(5, new java.sql.Date((new java.util.Date()).getTime()));
            }

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

    @Override
    public List<String> getShowsList(String initial) throws SQLException {
        return getTableList("shows", "show_name", initial);
    }

    @Override
    public synchronized boolean getTheater(TheaterBean theater) throws SQLException {
        
        String query = "SELECT * FROM theaters "
                + "WHERE theater_code = ?";
        ResultSet rs = null;
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);
            pstat.setInt(1, theater.getCode());
            rs = pstat.executeQuery();
            if(rs.next()) {
                theater.setCode(rs.getInt("theater_code"));
                theater.setName(rs.getString("theater_name"));
                theater.setAddress(rs.getString("theater_address"));
                theater.setPhone(rs.getString("phone"));
                theater.setWebsite(rs.getString("website"));
                theater.setCity(rs.getString("city_name"));   
                return true;
            }
            else {
                return false;
            }
        }
         finally {
            if(rs != null)
                rs.close();
            if(pstat != null)
                pstat.close();
            close();
        }   
    }
    
    public String getTheaterCode(String theater) throws SQLException {
        String query = "SELECT theater_code FROM theaters "
                + "WHERE theater_name LIKE ?";
        PreparedStatement pstat = createPreparedStatment(query);        
        pstat.setString(1, theater);
        ResultSet rs = pstat.executeQuery();        
        rs.next();
        return rs.getString("theater_code");
        
    }

    @Override
    public FilteredRowSet getTheaters() throws SQLException {
        
        return getTable("theaters");        
    }

    @Override
    public synchronized FilteredRowSet getTheaters(String name, String city) throws SQLException {
        String query = "SELECT * FROM theaters "
                + "WHERE theater_name LIKE ? AND city_name LIKE ?";
        PreparedStatement pstat = null;
        FilteredRowSet filteredRS;
        try {
            pstat = createPreparedStatment(query); 
            if(name == null)
                name = "%";
            if(city == null)
                city = "%";
            pstat.setString(1, name);
            pstat.setString(2, city);
            ResultSet rs = pstat.executeQuery();

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

    @Override
    public synchronized boolean getShow(ShowBean show) throws SQLException {
        String query = "SELECT * FROM shows "
                + " WHERE show_code = ?";
        ResultSet rs = null;
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);
            pstat.setInt(1, show.getShowCode());
            rs = pstat.executeQuery();
            if(rs.next()) {
                show.setShowCode(rs.getInt("show_code"));
                show.setName(rs.getString("show_name"));
                show.setCategory(rs.getString("category_name"));
                show.setDescription(rs.getString("description"));
                show.setYear(rs.getString("year_produced"));
                show.setLength(rs.getString("show_length"));     
                return true;                
            }
            else {
                return false;
            }
        }
         finally {
            if(rs != null)
                rs.close();
            if(pstat != null)
                pstat.close();
            close();
        }       
    }

    @Override
    public FilteredRowSet getShows() throws SQLException {
                
        return getTable("shows");
    }

    @Override
    public FilteredRowSet getShows(String name, String category, String year) throws SQLException {
        String query = "SELECT * FROM shows WHERE ";
        if(name != null) {
            query += " show_name = ? ";
        }  
        else {
            query += " ? ";
        }
        if(category != null) {
            query += " AND category_name = ? ";
        }
        else {
            query += " AND ? ";
        }
        if(year != null) {
            query += " AND year_produced = ? ";
        }
        else {
            query += " AND ? ";
        }
        
        PreparedStatement pstat = null;
        FilteredRowSet filteredRS;
        try {
            synchronized(this) {
                pstat = createPreparedStatment(query); 
                if(name != null) {
                    pstat.setString(1, name);
                }  
                else {
                    pstat.setBoolean(1, true);
                }
                if(category != null) {
                    pstat.setString(2, category);
                }
                else {
                    pstat.setBoolean(2, true);
                }
                if(year != null) {
                    pstat.setString(3, year);
                }
                else {
                    pstat.setBoolean(3, true);
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
    public synchronized boolean getEvent(EventBean event) throws SQLException {
        String query = "SELECT * FROM events"
                + "WHERE event_code = ?";
        ResultSet rs = null;
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);
            pstat.setInt(1, event.getCode());
            rs = pstat.executeQuery();
            if(rs.next()) {
                event.setCode(rs.getInt("event_code"));
                event.setDate(rs.getDate("event_date"));
                event.setTime(rs.getTime("event_time"));
                event.setShow(rs.getInt("show_code"));
                event.setTheater(rs.getString("theater_name"));
                
                return true;
            }
            else {
                return false;
            }
        }
         finally {
            if(rs != null)
                rs.close();
            if(pstat != null)
                pstat.close();
            close();
        }   
    }

    @Override
    public FilteredRowSet getEvents() throws SQLException {
               
        return getTable(" events NATURAL JOIN shows ");
    }

    @Override
    public FilteredRowSet searchEvents(Date fromDate, Date toDate, 
            Integer showCode, String city) throws SQLException {
        String query = "SELECT * FROM events "
                + " NATURAL JOIN shows "
                + " NATURAL JOIN theaters "
                + " NATURAL JOIN (SELECT event_code, COUNT(*) AS available "
                + " FROM tickets WHERE user_id IS NULL GROUP BY event_code) "
                + " AS available_tickets "
                + " WHERE ";
        if(city != null) {
            query += " city_name = ? ";
        }
        else {
            query += " ? ";
        }
        
        if(fromDate != null) {
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
    public synchronized boolean insertReview(int showCode, String review) throws SQLException {
        String query = "INSERT INTO reviews (show_code, review, user_id) "
                + "VALUES (?, ?, ?)";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);
            pstat.setInt(1, showCode);
            pstat.setString(2, review);
            pstat.setString(3, clientUserID);
            
            return pstat.executeUpdate() != 0;
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public synchronized boolean insertPicture(int showCode, String pic) throws SQLException {
        String query = "INSERT INTO pictures (show_code, pic, user_id) "
                + "VALUES (?, ?, ?)";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);
            pstat.setInt(1, showCode);
            pstat.setString(2, pic);
            pstat.setString(3, clientUserID);
            
            return pstat.executeUpdate() != 0;
        }
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public synchronized FilteredRowSet getTheaterSeats(int theaterCode) throws SQLException {
        String query = "SELECT * FROM seats WHERE theater_code = ?";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query); 
            pstat.setInt(1, theaterCode);

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

    @Override
    public synchronized FilteredRowSet getShowReviews(int showCode) throws SQLException {
        String query = "SELECT * FROM reviews "
                + "WHERE show_code = ?";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);
            pstat.setInt(1, showCode);
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

    @Override
    public FilteredRowSet getShowPictures(int showCode) throws SQLException {
        String query = "SELECT * FROM pictures "
                + "WHERE show_code = ?";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);
            pstat.setInt(1, showCode);
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

    @Override
    public synchronized FilteredRowSet getUserTickets() throws SQLException, NoAccessException {
        if(clientUserID == null)
            throw new NoAccessException("No user associated with this class.");
        return getUserTickets(clientUserID);
    }

    @Override
    public synchronized FilteredRowSet getUserTickets(String userID, String password) 
            throws SQLException, NoAccessException {
        if(clientUserID == null && setUser(userID, password)) {
            return getUserTickets(userID);
        }
        else if(clientUserID != null && clientUserID.equals(userID)) {
            return getUserTickets();
        }
        else {
            throw new NoAccessException("UserID/Password wrong or not current user.");
        }        
    }

    /**
     * Gets tickets reserved by the given user.
     * @param userID the user's identifier.
     * @return FilteredRowSet of all ticket information including date, time, 
     * place, and show.
     * @throws SQLException 
     */
    synchronized FilteredRowSet getUserTickets(String userID) throws SQLException {
        String query = " SELECT T.*, E.event_date, E.event_time, S.show_name, " +
            " H.theater_name, H.theater_address, H.city_name, A.seat, A.row " +
            " FROM (select * from tickets WHERE user_id = ?) AS T " +
            " NATURAL JOIN events AS E " +
            " NATURAL JOIN shows AS S " +
            " NATURAL JOIN theaters AS H " +
            " NATURAL JOIN seats  AS A " +
            " ORDER BY E.event_date, E.event_time ";
        PreparedStatement pstat = null;
        try {
            pstat = createPreparedStatment(query);
            pstat.setString(1, userID);
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

    @Override
    public synchronized boolean reserveTicket(int ticketNo) throws SQLException, NoAccessException {
        
        if(clientUserID == null) {
            throw new NoAccessException("No user loged in to reserve ticket.");
        }
        
        String reserveTicket = "UPDATE tickets " +
            " SET user_id = ? " +
            " WHERE ticket_no = ? AND user_id IS NULL ";
        PreparedStatement pstat = null;  
        
        try {            
            pstat = createPreparedStatment(reserveTicket);
            pstat.setString(1, clientUserID);
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
    public synchronized boolean cancelTicket(int ticketNo) throws SQLException, NoAccessException {
        if(!(this instanceof EventsManagerInfoImpl) && clientUserID == null) {
            throw new NoAccessException("No user loged in to cancel ticket.");
        }
        String cancelTicket = "UPDATE tickets " +
            " SET user_id = null " +
            " WHERE ticket_no = ? ";
        if(!isManager) {
            cancelTicket += " AND user_id = ? ";
        }
                
        PreparedStatement pstat = null;        
        try {            
            pstat = createPreparedStatment(cancelTicket);
            pstat.setInt(1, ticketNo);
            if(!isManager) {
                pstat.setString(2, clientUserID);
            }                        
            return pstat.executeUpdate() != 0;            
        }       
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }
    
    @Override
    public FilteredRowSet getShows(String initial) throws SQLException {
        return getColumnFilteredRS("shows", "show_name", initial);
    }

    @Override
    public synchronized MainBean getUserInfo() throws SQLException, NoAccessException {
        if(clientUserID == null)
            throw new NoAccessException("No user associated with this class.");
                
        MainBean user = new MainBean();
        user.setUserID(clientUserID);
        getUserInfo(user);
        return user;
        
    }
    
    /**
     * 
     * @param user
     * @throws SQLException 
     */
    synchronized void getUserInfo(MainBean user) throws SQLException {
        
        String query = "SELECT * FROM users WHERE user_id = ? ";
        ResultSet rs = null;
        PreparedStatement pstat = null;   
        try {
            pstat = createPreparedStatment(query); 
            pstat.setString(1, user.getUserID());
            rs = pstat.executeQuery();
            if(rs.next()) {
                user.setEmail(rs.getString("email"));
                user.setAddress(rs.getString("user_address"));
                user.setPhone(rs.getString("user_phone"));
                user.setCity(rs.getString("city_name"));                
            }            
        } 
        finally {
            if(rs != null)
                rs.close();
            if(pstat != null)
                pstat.close();
            close();
        }
    }

    @Override
    public synchronized boolean setUserInfo(MainBean user) throws SQLException, NoAccessException {
        if(clientUserID == null)
            throw new NoAccessException("No user associated with this class.");
        return setAnyUserInfo(clientUserID, user);
    }
    
    /**
     * Updates given user's mane, email, address, phone and city.
     * @param userID of the user who's information should be changed.
     * @param user a MainBean with all fields filled. If a filled is null the 
 database will lose the previous information.
     * @return true if updated successful.
     * @throws SQLException
     */
    synchronized boolean setAnyUserInfo(String userID, MainBean user) throws SQLException {
        String query = "UPDATE users "
                + "SET user_name = ? , email = ? , user_address = ? , "
                + "user_phone = ? , city_name = ? "
                + "WHERE user_id = ? ";
        PreparedStatement pstat = null;  
        try {
            pstat = createPreparedStatment(query); 
            pstat.setString(1, user.getName());
            pstat.setString(2, user.getEmail());
            pstat.setString(3, user.getAddress());
            pstat.setString(4, user.getPhone());
            pstat.setString(5, user.getCity());
            pstat.setString(6, userID);
            return pstat.executeUpdate() != 0;            
        } 
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }
    
    synchronized boolean setAnyUserInfo(String userID, User user) throws SQLException {
        String query = "UPDATE users "
                + "SET user_name = ? , email = ? , user_address = ? , "
                + "user_phone = ? , city_name = ? "
                + "WHERE user_id = ? ";
        PreparedStatement pstat = null;  
        try {
            pstat = createPreparedStatment(query); 
            pstat.setString(1, user.getName());
            pstat.setString(2, user.getEmail());
            pstat.setString(3, user.getAddress());
            pstat.setString(4, user.getPhone());
            pstat.setString(5, user.getCity());
            pstat.setString(6, userID);
            return pstat.executeUpdate() != 0;            
        } 
        finally {
            if(pstat != null)
                pstat.close();
            close();
        }
    }
    
    @Override
    public synchronized FilteredRowSet getEventTickets(int eventCode) throws SQLException {
        String query = "SELECT S.*, T.ticket_no, T.assigned, T.price, "
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
    
    @Override
    public synchronized boolean reserveTickets(List<Integer> tickets) throws SQLException, NoAccessException {
        
        if(clientUserID == null) {
            throw new NoAccessException("No user loged in to reserve ticket.");
        }
        
        String reserveTicket = "UPDATE tickets " +
            " SET user_id = ? " +
            " WHERE ticket_no = ? AND user_id IS NULL ";
        PreparedStatement pstat = null;  
        
        try {            
            pstat = createPreparedStatment(reserveTicket);
            conn.setAutoCommit(false);
            pstat.setString(1, clientUserID);
            
            for(int ticketNo: tickets) {
                pstat.setInt(2, ticketNo);
                pstat.addBatch();
            }
            pstat.executeBatch();
            conn.commit();
            return true;            
        } 
        catch(SQLException E) {
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
    public void logOut(){
        this.clientUserID = null;
        this.isManager = false;
        close();
    }
}
