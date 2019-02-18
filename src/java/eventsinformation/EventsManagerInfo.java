
package eventsinformation;


import java.sql.SQLException;
import javax.naming.NamingException;
import javax.sql.rowset.FilteredRowSet;
import newuserbean.EventBean;
import newuserbean.Show;
import newuserbean.TheaterBean;
import newuserbean.UserBean;

/**
 * Represents an interface for communication with the database for a 
 * privileged user with manager permission.
 * Can Add, Edit, Delete and retrieve Show categories, Shows, Theaters, Events,
 * Tickets and all User information.
 * This interface extends EventsInfo, so it already has all capabilities of a
 * normal user.
 * @author Shani Shapiro
 */
public interface EventsManagerInfo extends EventsInfo {
    
    /**
     * Add a city to database cities table.
     * @param cityName the city to be added.
     * @return true if City added to DB, false otherwise.
     * @throws java.sql.SQLException
     */
    public boolean insertCity(String cityName) throws SQLException;
    
    /**
     * Delete a city from the database cities table.
     * @param cityName the city to be deleted.
     * @return true if City deleted successfully, false otherwise.
     * @throws java.sql.SQLException
     * @throws javax.naming.NamingException
     */
    public boolean deleteCity(String cityName) throws SQLException, NamingException;
    
    /**
     * Add a category to database categories table.
     * @param categoryName the category to be added.
     * @return true if Category added to DB, false otherwise.
     * @throws java.sql.SQLException
     */
    public boolean insertCategory(String categoryName) throws SQLException;
    
    /**
     * Delete a category from the database categories table 
     * with all corresponding shows.
     * @param categoryName the category to be deleted.
     * @return true if Category deleted successfully, false otherwise.
     * @throws java.sql.SQLException
     */
    public boolean deleteCategory(String categoryName) throws SQLException;
    
    /**
     * Updates the database's Theatres table with the theatre represented by the 
     * bean.
     * Also adds the corresponding seats in the theatre to the seats table.
     * @param theater the theatre bean with all information including: name, 
     * address, phone, website, city, amount of seat rows and seats in a row.
     * @return true if theatre is added, false otherwise.
     * @throws SQLException 
     */
    public boolean insertTheater(TheaterBean theater) throws SQLException;
    
    /**
     * Deletes the theatre and corresponding events and seats from the database.
     * @param theaterCode the theatre's identifier.
     * @return true if deleted successfully, false otherwise.
     * @throws SQLException 
     */
    public boolean deleteTheater(int theaterCode) throws SQLException;
    
     /**
     * Inserts a show in database
     * @param theater a Bean with all information about the show, 
     * including the category, name, description, year produced and show length.
     * @return true if show is added, false otherwise.
     * @throws SQLException 
     */
    public boolean insertShow(Show theater) throws SQLException;
    
    /**
     * Deletes the show and corresponding reviews and pictures from the database.
     * @param showCode the db identifier for the show.
     * @return true if deleted successfully, false otherwise.
     * @throws SQLException 
     */
    public boolean deleteShow(int showCode) throws SQLException;
    
    /**
     * Inserts an event in the database and corresponding tickets for the event.
     * @param event a Bean with all information about the event including 
     * ticket's price, including the date, time, show and theatre.
     * @return true if show is added, false otherwise.
     * @throws SQLException 
     */
    public boolean insertEvent(EventBean event) throws SQLException;
    
    /**
     * Deletes the event and corresponding tickets from the database.
     * @param eventCode the db identifier for the event.
     * @return true if deleted successfully, false otherwise.
     * @throws SQLException 
     */
    public boolean deleteEvent(int eventCode) throws SQLException;
    
    /**
     * Gets all the tickets info for a given event. Including the seat info.
     * @param eventCode the db identifier for the event.
     * @return FilteredRowSet with all fields in tickets table for tickets of 
     * the given event. Including the seat info.
     * @throws SQLException 
     */
    @Override
    public FilteredRowSet getEventTickets(int eventCode) throws SQLException;
    
    /**
     * Gets the tickets of the given user.
     * @param userID of user for whom to get tickets.
     * @return FilteredRowSet of the tickets for the given user. Including date, 
     * time, place, and show.
     * @throws SQLException 
     */
    public FilteredRowSet getUserTickets(String userID) throws SQLException;
    
    /**
     * Reserves the given ticket for the give user.
     * @param ticketNo the ticket to reserve.
     * @param userID the id of user.
     * @return true if successful, false if ticket if failed.
     * @throws SQLException
     */
    public boolean reserveTicket(int ticketNo, String userID) 
            throws SQLException;
    
    /**
     * Cancels reservation made for this ticket.
     * @param ticketNo the ticket to be cancelled.
     * @return true if successful, false if cancel ticket fails.
     * @throws SQLException
     */
    @Override
    public boolean cancelTicket(int ticketNo) throws SQLException, NoAccessException;
    
    /**
     * Changes the price of tickets for the given event.
     * @param eventCode event identifier.
     * @param price the new price to insert.
     * @return true if successful, false otherwise.
     * @throws SQLException
     */
    public boolean setTicketPriceByEvent(int eventCode, String price) 
            throws SQLException;
    
    /**
     * Changes the price of the given ticket.
     * @param ticketNo ticket identifier.
     * @param price the new price to insert.
     * @return true if successful, false otherwise.
     * @throws SQLException
     */
    public boolean setTicketPrice(int ticketNo, String price) 
            throws SQLException;
    
    /**
     * Changes the price of tickets for the given event and row.
     * @param eventCode event identifier.
     * @param row
     * @param price the new price to insert.
     * @return true if successful, false otherwise.
     * @throws SQLException
     */
    public boolean setTicketsPriceByRow(int eventCode, int row, String price) 
            throws SQLException;
    
    /**
     * Gets user information from the db, for the userID received.
     * @param userID the user for whom to get the information.
     * @return UserBean filled with the user information. 
     * @throws SQLException
     */
    public UserBean getUserInfo(String userID) throws SQLException;
    
    /**
     * Updates/Changes the received user info with the given information.
     * @param user UserBean filled with the user info to be updated.
     * @return true if updated successfully, false otherwise.
     * @throws SQLException
     */
    @Override
    public boolean setUserInfo(UserBean user) throws SQLException;
    
}
