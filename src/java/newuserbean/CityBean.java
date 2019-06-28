package newuserbean;

import eventsinformation.EventsInfoImpl;
import eventsinformation.EventsManagerInfoImpl;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

/**
 * A class representing the City editing page with with an optional added city 
 * and list of cities to show and edit.
 */
@ManagedBean()
@ViewScoped
public class CityBean {

    private String city;
    private List<String> cities;
    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;   
    
    /**
     * Gets the city list from the database. Doing from a getter would be called 
     * twice by the xhtml loading.
     */
    @PostConstruct
    public void init() {
        try {
            cities = dbInfo.getCitiesList();
        } catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
    /**
     * Adds a new city that was set in setSity getter to the db,
     * if it's a manager invoking it.
     */
    public void addCity(){
        if((cities.indexOf(this.city)) != -1){
            FacesMessage msg = new FacesMessage("we have already this city");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        try {
            if(dbInfo instanceof EventsManagerInfoImpl){
                if(((EventsManagerInfoImpl)dbInfo).insertCity(city)){
                    cities.add(city);
                    this.city = "";
                }else{
                    FacesMessage msg = new FacesMessage("no changes made to the DB");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                } 
            }
        } catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
    /**
     * Removes the given city from the db, if done by a manager.
     * @param city 
     */   
    public void deleteCity(String city){
        try {
            if(dbInfo instanceof EventsManagerInfoImpl){
                if(((EventsManagerInfoImpl)dbInfo).deleteCity(city)){
                    cities.remove(city);
                }else{
                    FacesMessage msg = new FacesMessage("no changes made to the DB");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                }
            }
        } catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }  
    }
    
    public List<String> getCities() {
        return cities;
    }

    public void setCities(List<String> cities) {
        this.cities = cities;
    }
    
    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
        
}