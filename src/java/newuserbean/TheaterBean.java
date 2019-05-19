package newuserbean;

import eventsinformation.EventsInfoImpl;
import eventsinformation.EventsManagerInfoImpl;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.sql.rowset.FilteredRowSet;
import org.primefaces.context.RequestContext;

/**
 * A bean that contains the least of theatres and a current 'highlighted' 
 * working theatre we are editing or adding.
 * @author Shani
 */
@ManagedBean()
@ViewScoped
public class TheaterBean implements Serializable{
    
    //edited/added theater infromtion
    private int code;
    private String name;
    private String address;
    private String phone;
    private String website;
    private String city;
    private int rows;
    private int seats;
    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    
    //rowset of theaters table
    private FilteredRowSet theaters;
    
    /**
     * Constructor for TheaterBean.
     */
    public TheaterBean() {
        theaters = null;
    }
    
    /**
     * Removes a theatre from the database and the display.
     * @param row the row number in the theatres table.
     */
    public void deleteTheater(int row) {
        try {
            theaters.absolute(row);
            if(dbInfo instanceof EventsManagerInfoImpl){
                boolean temp = ((EventsManagerInfoImpl)dbInfo).deleteTheater(theaters.getInt("theater_code"));
                theaters.deleteRow();
            }
        } catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
    /**
     * Prepares the current theatre's information for the edit form.
     * @param row the row number in the theatres table.
     * @throws java.sql.SQLException
     */    
    public void editTheater(int row) throws SQLException{
        theaters.absolute(row);
        this.setCode(theaters.getInt("theater_code"));
        this.setName(theaters.getString("theater_name"));
        this.setCity(theaters.getString("city_name"));
        this.setAddress(theaters.getString("theater_address"));
        this.setPhone(theaters.getString("theater_phone"));
        this.setWebsite(theaters.getString("website"));
        this.setRows(theaters.getInt("theater_rows"));
        this.setSeats(theaters.getInt("row_seats"));
    }
    
    /**
     * Invoked for adding or editing a theatre in the database, if done by a manager.
     * Also refreshes the theatres list display.
     * @throws SQLException 
     */
    public void addTheater() throws SQLException{
        if(this.code == 0){
            if(dbInfo instanceof EventsManagerInfoImpl){
                if(((EventsManagerInfoImpl)dbInfo).insertTheater(this)){
                    theaters = dbInfo.getTheaters();
                }else{
                    FacesMessage msg = new FacesMessage("no changes made to the DB");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    return;
                }
            }else{
                return;
            }
        }else{
            if(dbInfo instanceof EventsManagerInfoImpl){
                if(((EventsManagerInfoImpl)dbInfo).editTheater(this)){
                    theaters = dbInfo.getTheaters();
                }else{
                    FacesMessage msg = new FacesMessage("no changes made to the DB");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    return;
                }
            }else{
                return;
            }
        }  
        this.setAddress("");
        this.setCity("");
        this.setCode(0);
        this.setName("");
        this.setPhone("");
        this.setRows(0);
        this.setSeats(0);
        this.setWebsite("");
        RequestContext.getCurrentInstance().execute("PF('theaterDialog').hide()"); 
    }
    
    /**
     * Empties the fields for the add theatre form.
     */
    public void addSetup(){
        this.setAddress("");
        this.setCity("");
        this.setCode(0);
        this.setName("");
        this.setPhone("");
        this.setRows(0);
        this.setSeats(0);
        this.setWebsite("");
    }
    
    /**
     * List of cities for the form drop down.
     * @return List of cities from the db.
     */
    public List<String> completeCity(){
        try{
            List<String> cityList = dbInfo.getCitiesList();
            dbInfo.close();
            return cityList;
        }catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
        
    }
    
    public void setDbInfo(EventsInfoImpl dbInfo){
        this.dbInfo = dbInfo;
    }

    /**
     * gets the list of theatres from the db if not received yet.
     * @return list of theatres.
     */
    public FilteredRowSet getTheaters(){
        if(theaters == null){
            try {
                theaters = dbInfo.getTheaters();
            } catch (SQLException ex) {
                FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        }
        return theaters;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }
    
     public void setWebsite(String website) {
        this.website = website;
    }

    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getRows() {
        return rows;
    }
    
    public void setRows(int rows){
        this.rows = rows;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats){
        this.seats = seats;
    }
    
}