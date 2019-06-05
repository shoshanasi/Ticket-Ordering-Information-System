
package eventsinformation;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import javax.sql.rowset.FilteredRowSet;
import newuserbean.EventBean;
import newuserbean.ShowBean;
import newuserbean.TheaterBean;
import newuserbean.MainBean;

/**
 * Represents an interface for a user to get non-privileged information about 
 * Events from the Events web site database. 
 * The information an object from this type can get, includes receiving 
 information about ShowBean categories, Shows,Theaters, Events etc. 
 Can also login as a user or subscribe new user to the database, as well as 
 reserving or canceling tickets for the logged in user.
 * @author Shani Shapiro
 */
public interface EventsInfo {
    
    /**
     *  Closes all Connections, ResultSets etc. that are used during this class
     *  deployment.
     */
    public void close();
    
    /**
     * Checks if this object has logged in with manager permission.
     * @return true if already provided login details of manager, false otherwise.
     */
    public boolean isManager();
    
    /**
     * Gets the user id associated with this object.
     * @return userID if exists, null otherwise.
     */
    public String getUserID();
    
    /**
     * Get a FilteredRowSet with the list of Cities registered in the DB Cities 
     * table.
     * @return FilteredRowSet the table rows with the city names.
     * @throws java.sql.SQLException
     */
    public FilteredRowSet getCities() throws SQLException;
    
    /**
     * Get the list of Cities registered in the DB Cities table,
     * that begin with the following initials.
     * @param initial the beginning of the city name.
     * @return List with the city names
     * @throws java.sql.SQLException
     */
    public List<String> getCitiesList(String initial) throws SQLException;
    
    /**
     * Get the list of Categories registered in the DB Categories table.
     * @return List with the category names
     * @throws java.sql.SQLException
     */
    public List<String> getCategoriesList() throws SQLException;
    
    /**
     * Get the list of Categories registered in the DB Categories table,
     * that begin with the following initials.
     * @param initial the beginning of the category name.
     * @return List with the category names
     * @throws java.sql.SQLException
     */
    public List<String> getCategoriesList(String initial) throws SQLException;
    
    /**
     * Get a FilteredRowSet with rows containing the Categories registered 
     * in the DB show_category table.
     * @return FilteredRowSet with the category names.
     * @throws java.sql.SQLException
     */
    public FilteredRowSet getCategories() throws SQLException;
    
    /**
     * Inserts a new user to users table
     * @param user the user to add.
     * @return true if successful, false otherwise.
     * @throws SQLException 
     * @throws eventsinformation.NoAccessException 
     */
    public boolean insertUser(MainBean user) throws SQLException, NoAccessException;
    
    /**
     * Edits this user's information, if a user is logged in.
     * @param user the class with user's edited information.
     * @return false if no such could not update user information.
     * @throws SQLException 
     * @throws NoAccessException 
     */
    public boolean editUser(MainBean user) throws SQLException, NoAccessException;
    /**
     * Checks if this user and password are correct and if so, is he a
     * manager or normal user and updates the object.
     * @param user the user_id / username
     * @param pass the password
     * @return false if no such user and password pair, true if it's a user or a manager.
     * @throws SQLException 
     * @throws NoAccessException 
     */
    public boolean setUser(String user, String pass) throws SQLException, NoAccessException;
    
    /**
     * Checks if this user with filled user_id and password is correct. 
     * If so, is he a user or admin and fills in the relevant information in
     * this object.
     * If user exists fills the all the user information from the database to the received user. 
     * @param user the class with user_id and password filled.
     * @return false if no such user and password pair, true if it's a user or a manager.
     * @throws SQLException 
     * @throws NoAccessException 
     */
    public boolean setUser(MainBean user) throws SQLException, NoAccessException;
    
    /**
     * Checks if the user id is available for use or already used.
     * @param userName the user id in question.
     * @return true if the user id is not yet used therfore available, false otherwise.
     * @throws SQLException 
     */
    public boolean validUserName(String userName) throws SQLException;
    
    /**
     * Returns the events that match the parameters given.
     * Any null value is just ignored.
     * Should call close() on this class after using the returned ResultSet.
     * @param category the show category.
     * @param name the name of show.
     * @param city the city the event is taking place.
     * @param year shows produced in given year.
     * @param date the date of event
     * @return a FilteredRowSet with all events in DB that match the search criteria.
     * @throws java.sql.SQLException
     */
    public FilteredRowSet searchShow(String category, String name, String year, 
            Date date, String city) throws SQLException;
    
    /**
     * Returns list of shows names that match the given initials.
     * @param initial the beginning of the show name.
     * @return list of matching show names.
     * @throws SQLException 
     */
    public List<String> getShowsList(String initial) throws SQLException;
    
    /**
     * Fills the given theatre bean with all the theatre info of the filled theatre code.
     * @param theater a Bean with theatre_code filled.
     * @return ture if successful, false otherwise.
     * @throws SQLException 
     */
    public boolean getTheater(TheaterBean theater) throws SQLException;
    
    /**
     * Gets a FilteredRowSet containing rows with all Theatres.
     * @return FilteredRowSet with all theatres in db.
     * @throws SQLException 
     */
    public FilteredRowSet getTheaters() throws SQLException;
    
    /**
     * Gets a FilteredRowSet with rows containing all Theatres that have the 
     * given name and city.
     * If one of the parameters are null then it's ignored.
     * @param name theatre name.
     * @param city the city of the theatres.
     * @return FilteredRowSet with the matching theatres.
     * @throws SQLException 
     */
    public FilteredRowSet getTheaters(String name, String city) 
            throws SQLException;
    
    /**
     * Fills the given show bean with all the show info of the filled show code.
     * @param show a Bean with show_code filled.
     * @return ture if successful, false otherwise.
     * @throws SQLException 
     */
    public boolean getShow(ShowBean show) throws SQLException;
    
    /**
     * Gets all the shows with all the fields from the db shows table.
     * @return a FilteredRowSet containing the whole shows table.
     * @throws SQLException 
     */
    public FilteredRowSet getShows() throws SQLException;
    
    /**
     * Returns a FilteredRowSet that contains a list of show names that match 
     * the given initials. 
     * It also contains a filter for further refinement of initials
     * @param initial the initial of the show name.
     * @return FilteredRowSet that contains a list of matching show names.
     * @throws SQLException 
     */
    public FilteredRowSet getShows(String initial) throws SQLException;
    
    /**
     * Gets all shows that match the given filtering information.
     * @param name the show name.
     * @param category the show category.
     * @param year the year the show was produced.
     * @return a FilteredRowSet with all fields for the matching shows.
     * @throws SQLException 
     */
    public FilteredRowSet getShows(String name, String category, String year) 
            throws SQLException;
    
    /**
     * Fills the event information for the given event bean that has the 
     * event_code.
     * @param event the event Bean with the code of the wanted event.
     * @return true if successful, false otherwise.
     * @throws SQLException 
     */
    public boolean getEvent(EventBean event) throws SQLException;
    
    /**
     * Gets all the events with all the fields from the db events table
     * and corresponding fields from the shows table.
     * @return a FilteredRowSet with the whole events table
     * including the corresponding code, name and category of the show.
     * @throws SQLException 
     */
    public FilteredRowSet getEvents() throws SQLException;
    
     /**
     * Gets all the events that match the search parameters ordered by time. 
     * Includes all the fields from the db events table 
     * and matching columns from the shows and theatres tables, for events that
     * have available tickets.
     * @param fromDate from what date of events, if null searches from current date onwards.
     * @param toDate to what date of events, if null searches all.
     * @param showCode the db identifier for the show, if null than ignored.
     * @param city the city of the event.
     * @return a FilteredRowSet with the whole events table
     * and corresponding fields from the shows and theatres table.
     * including the corresponding code, name and category of the show.
     * @throws SQLException 
     */
    public FilteredRowSet searchEvents(Date fromDate, Date toDate, 
            Integer showCode, String city) throws SQLException;
    
    /**
     * Inserts a review for a given show, corresponding to current user.
     * @param showCode the db identifier for the show.
     * @param review the full review
     * @return true if added successfully, false otherwise.
     * @throws SQLException 
     */
    public boolean insertReview(int showCode, String review) throws SQLException;
    
    /**
     * Inserts a picture for a given show, with credit to current user.
     * @param showCode the db identifier for the show.
     * @param pic the picture to add.
     * @return true if added successfully, false otherwise.
     * @throws SQLException 
     */
    public boolean insertPicture(int showCode, String pic) throws SQLException;
    
    /**
     * Gets all the seats info for a given theatre. 
     * Including seat_id, row and seat fields.
     * Should call close() after using the FilteredRowSet
     * @param theaterCode the db identifier for the theatre.
     * @return FilteredRowSet with seat_id, row and seat fields in seats table
     * for seats in the given theatre.
     * @throws java.sql.SQLException 
     */
    public FilteredRowSet getTheaterSeats(int theaterCode) throws SQLException;
    
    /**
     * Gets all the reviews for a given show. 
     * Including review_id, the review and user_id.
     * Should call close() after using the ResultSet
     * @param showCode the db identifier for the show.
     * @return FilteredRowSet with review_id, the review and user_id. fields 
     * in reviews table about this show.
     * @throws java.sql.SQLException 
     */
    public FilteredRowSet getShowReviews(int showCode) throws SQLException;
    
    /**
     * Gets all the pictures for a given show. 
     * Including pic_id, the picture and user_id.
     * Should call close() after using the ResultSet
     * @param showCode the db identifier for the show.
     * @return FilteredRowSet with pic_id, the picture and user_id. fields 
     * in pictures corresponding to this show.
     * @throws java.sql.SQLException 
     */
    public FilteredRowSet getShowPictures(int showCode) throws SQLException;
    
    /**
     * Gets all tickets reserved by the user kept in this class when calling 
     * setUser for the last time.
     * @return FilteredRowSet if the user of this class is set. All ticket info,
     * including date, time, place, and show.
     * @throws SQLException 
     * @throws NoAccessException if setUser was never called.
     */
    public FilteredRowSet getUserTickets() throws SQLException, NoAccessException;
    
    /**
     * Gets the tickets of the given user if the password is correct.
     * @param userID of user for whom to get tickets.
     * @param password user's password.
     * @return FilteredRowSet of the tickets for the given user. Including date, 
     * time, place, and show.
     * @throws SQLException 
     * @throws NoAccessException when password is wrong.
     */
    public FilteredRowSet getUserTickets(String userID, String password) 
            throws SQLException, NoAccessException;
    
    /**
     * Reserves the given ticket for the user associated with this object.
     * @param ticketNo the ticket to reserve.
     * @return true if successful, false if ticket if failed or ticket already 
     * reserved.
     * @throws SQLException 
     * @throws NoAccessException if no user associated with this object.
     */
    public boolean reserveTicket(int ticketNo) throws SQLException, 
            NoAccessException;
    
    
    /**
     * Reserves the given list of tickets for the user. Either books all or none.
     * @param tickets list of ticket id's of tickets to book.
     * @return true if successful. 
     * @throws SQLException
     * @throws NoAccessException
     */
    public boolean reserveTickets(List<Integer> tickets) 
             throws SQLException, NoAccessException;
    
    
    /**
     * Cancels reservation made for this ticket, if the ticket belongs to this
     * object.
     * @param ticketNo the ticket to be cancelled.
     * @return true if successful, false if cancel ticket fails.
     * @throws SQLException
     * @throws eventsinformation.NoAccessException if the ticket owner is not 
     * associated with this object.
     */
    public boolean cancelTicket(int ticketNo) throws SQLException, 
            NoAccessException;
    
    /**
     * Gets user information from the db, for the user associated with this object.
     * @return MainBean filled with current user information, null if nut successful. 
     * @throws SQLException
     * @throws NoAccessException 
     */
    public MainBean getUserInfo() throws SQLException, NoAccessException;
    
    /**
     * Updates/Changes the current user info with the given information.
     * @param user MainBean filled with the user info to be updated.
     * @return true if updated successfully, false otherwise.
     * @throws SQLException
     * @throws NoAccessException if no user associated with this object or if 
 the given user is not the same as in the MainBean object.
     */
    public boolean setUserInfo(MainBean user) throws SQLException, NoAccessException;
    
    /**
     * Gets all the tickets info for a given event. Including the seat info,
     * not including the user_id of buyers.
     * @param eventCode the db identifier for the event.
     * @return FilteredRowSet with all fields of available tickets table for tickets of 
     * the given event. Including the seat info, not including the user_id of buyers.
     * @throws SQLException 
     */
    public FilteredRowSet getEventTickets(int eventCode) throws SQLException;
    
    public void logOut();
}
