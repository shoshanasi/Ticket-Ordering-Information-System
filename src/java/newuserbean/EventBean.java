package newuserbean;

import eventsinformation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.sql.rowset.FilteredRowSet;

/**
 * A Bean class representing an event's information.
 * @author Shani
 */
@ManagedBean()
@ViewScoped
public class EventBean implements Serializable{
    
    private int code;
    private int show;
    private int theaterCode;
    private String city;
    
    private BigDecimal price;
    private int emptySeats;
    private int unassignedSeats;
    
    //for storing in the DB
    private java.sql.Date sqlDate;
    private java.sql.Time sqlTime;
    
    
    //for the xhtml
    private String theater;
    private java.util.Date date;
    private java.util.Date time;
    
    
    //for choosing a theater
    private FilteredRowSet theaters;
    //for choosing a theater
    private List<Theater> theaterList;
    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    
    /**
     * Constructor for EventBean
     * @throws SQLException 
     */
    public EventBean() {
        city = null;        
    }
    
    /**
     * Constructor for EventBean
     * @param code the events code.
     * @param date the event date.
     * @param time event's time.
     * @param theater the name of the theatre where the event will take place.
     * @param theaterCode the theatre code.
     * @param city the theatre's city.
     * @param emptySeats number of available tickets for the event.
     * @param show the show of the event.
     */    
    public EventBean(int code, java.sql.Date date, java.sql.Time time, 
            String theater, int theaterCode, String city, int emptySeats, int show) {
        this.code = code;
        this.city = city;
        this.sqlDate = date;
        this.date = date;
        this.sqlTime = time;
        this.time = time;
        this.theater = theater;
        this.theaterCode = theaterCode;
        this.emptySeats = emptySeats;
        this.show = show;
    }
    
    /**
     * Edits this events information according to the given event.
     * @param event an EventBean to copy information from.
     */
    public void editEvent(EventBean event){
        this.setCode(event.getCode());
        this.setDate(event.getDate());
        this.setShow(event.getShow());
        this.setTheater(event.getTheater());
        this.setEmptySeats(event.getEmptySeats());
        this.setCity(event.getCity());
        this.setTime(event.getTime());
        this.setTheatersName();
    }
    
    
    /**
     * Checks that all the fields of the event are not null.
     * @return true if all fields are filled, false otherwise.
     * @throws SQLException (this exception is caught in the search bean.)
     */
    public boolean eventOk() throws SQLException{
        return !(sqlDate == null||theaterCode == 0||city == null||null == sqlTime);
    }
    
    /**
     * Sets the list of theatres that fit the city already chosen for the event.
     */
    public void setTheatersName(){
        try{
            this.theaters = dbInfo.getTheaters(null, city);
            System.out.println(this.city);
            
                
            this.theaterList = new ArrayList<>();
            theaters.beforeFirst();
            while(theaters.next())
                theaterList.add(new Theater(theaters.getString("theater_name"), theaters.getInt("theater_code")));
            theaters.beforeFirst();
            System.out.println(theaters.size());
        }catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later "+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }  
    
    public void cityChoosen(){
        this.setTheatersName();
    }
    
    /**
     * Empties the fields for the add event form.
     */
    public void addSetup(){
        this.setCity(null);
        this.setCode(0);
        this.setDate(new java.util.Date());
        this.setTime(new java.util.Date());
        this.setTheater("");
        this.setTheatersName();
    }
    
    @Override
    public String toString(){
        return "in "+this.getTheater()+" "+this.getCity()+" "+this.getSqlDate()+" "+this.getSqlTime();
    }
    
    public int getTheaterCode() {
        return theaterCode;
    }
    
    public void setCity(String city){
        this.city = city;
    }
    
    public String getCity(){
        return city;
    }

    public void setTheaterCode(int theaterCode) {
        for(Theater theater : this.theaterList) {
            if(theater.getCode() == theaterCode){
                this.setTheater(theater.getName());
                break;
            }
        }
        this.theaterCode = theaterCode;
    }
    
    public void setDbInfo(EventsInfoImpl aa){
        dbInfo = aa;
    }
    
    public java.sql.Date getSqlDate() {
        return sqlDate;
    }

    public void setSqlDate(java.sql.Date sqlDate) {
        this.sqlDate = sqlDate;
    }

    public java.sql.Time getSqlTime() {
        return sqlTime;
    }

    public void setSqlTime(java.sql.Time sqlTime) {
        this.sqlTime = sqlTime;
    }
    
    public void setDate(java.util.Date date) {
        this.date = date;
        this.sqlDate = new java.sql.Date(date.getTime());
    }
    
    public java.util.Date getDate() {
        return date;
    }
    
    public void setTime(java.util.Date time) {
        this.time = time;
        this.sqlTime = new java.sql.Time(time.getTime());
    }
    
    public java.util.Date getTime() {
        return time;
    }
    
    public int getEmptySeats() {
        return emptySeats;
    }

    public void setEmptySeats(int emptySeats) {
        this.emptySeats = emptySeats;
    }
    
    public int getUnassignedSeats() {
        return unassignedSeats;
    }

    public void setUnassignedSeats(int unassignedSeats) {
        this.unassignedSeats = unassignedSeats;
    }

    public int getShow() {
        return show; 
    }
    
    public void setShow(int show) {
        this.show = show; 
    }

    public String getTheater() {
        return theater;
    }
    
    public void setTheater(String theater) {
        this.theater = theater;
    }
    
    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public List<Theater> getTheaterList() {
        return theaterList;
    }

    public void setTheaterList(List<Theater> theaterList) {
        this.theaterList = theaterList;
    }
    
    /**
     * Inner class for a theatre that only has name and code information.
     * Used when for creating a list of theatres to choose from.
     */
    public class Theater{

        
        private String name;
        private int code;

        public Theater(String name, int code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
        
    }
}