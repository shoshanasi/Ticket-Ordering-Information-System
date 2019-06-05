package newuserbean;

import eventsinformation.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.sql.rowset.FilteredRowSet;
import org.primefaces.event.CellEditEvent;

/**
 * Bean representing an editable list of users.
 * @author Shani
 */
@ManagedBean(name = "users")
@ViewScoped
public class UsersView implements Serializable{
    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;

    //the tickets of a spcifice user
    private FilteredRowSet tickits;
    
    private List<User> usersList = new ArrayList<>();;

    //used for drop down list to edit user's city.
    private List<String> citis;       
    
    /**
     * Gets and displays the users editable table.
     */
    @PostConstruct
    public void init() {
        try {
            citis = dbInfo.getCitiesList();
            if(dbInfo instanceof EventsManagerInfoImpl){
                FilteredRowSet rs = ((EventsManagerInfoImpl)dbInfo).getUsers();
                rs.beforeFirst();
                while(rs.next()) {
                    usersList.add(new User(rs.getString("user_id"), 
                            rs.getString("user_name"), rs.getString("email"), 
                            rs.getString("user_phone"), rs.getString("user_address"), 
                            rs.getString("city_name")));
                }
            }else{
                FacesMessage msg = new FacesMessage("you arent a manager therefor acsses denied");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        }catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
    /**
     * Edits the cell display and underling user information in the db.
     * @param event 
     */
    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        User temp = usersList.get(event.getRowIndex());
        if(dbInfo instanceof EventsManagerInfoImpl){
            try {
                ((EventsManagerInfoImpl)dbInfo).setUserInfo(temp);
                if(newValue != null && !newValue.equals(oldValue)) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cell Changed", "Old: " + oldValue + ", New:" + newValue);
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                }
            } catch (SQLException ex) {
                FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        }
        
    }
    
    /**
     * gets the reserved tickets of the given user.
     * @param userId 
     */
    public void setTickits(String userId) {
        try {
            if(dbInfo instanceof EventsManagerInfoImpl){
                tickits = ((EventsManagerInfoImpl)dbInfo).getUserTickets(userId);
            }
         } catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } 
    }
    
    public void cancelTicket(int row) {
        try {
            tickits.absolute(row);
            if(dbInfo.cancelTicket(tickits.getInt("ticket_no"))){
                tickits.deleteRow();
            }else{
                FacesMessage msg = new FacesMessage("no changes made in the DB");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }  
        } catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (NoAccessException ex) {
            FacesMessage msg = new FacesMessage("you are not primetied to cancal this tiket "+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
   }
    
    public FilteredRowSet getTickits() {
        return tickits;
    }

    public List<String> getCitis() {
        return citis;
    }
    
    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    
    public List<User> getUsersList() {
        return usersList;
    }
}