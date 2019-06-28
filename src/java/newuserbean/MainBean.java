
package newuserbean;

import eventsinformation.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.sql.rowset.FilteredRowSet;
import recommender.GenaralSimilarity;

/**
* Main bean that's kept during the whole session of browsing with the connection
* to the database.
* Has information about the user -  if applicable, connection to the database 
* and the browsing state.
*/
@ManagedBean(name = "userBean")
@SessionScoped
public class MainBean  implements Serializable {
    
    //user infromtion
    private String userID;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String password2;
    private String address;
    private String city;    
    private boolean manager;
    
    //status parmters
    private String activTab;
    private String userDisplay;
    private String message;
    
    //getting the similerty indexs
    private GenaralSimilarity similer;

    //connction to the database
    private EventsInfoImpl dbInfo;
    //the users tickets
    private FilteredRowSet tickets;
        
    //the recommanded list for the user
    private List<String> imagesList = new ArrayList<>();
    
    /**
     * Constructor for the MainBean.
     * Initially capable of accessing non privileged (manager) info.
     * Pops up a message if couldn't connect to the database, therfore not 
     * starting the webapp.
     */
    public MainBean(){
        this.manager = false;
        this.activTab = "home";
        this.userDisplay = "log in";
        try {
            dbInfo = new EventsInfoImpl();
            similer = new GenaralSimilarity();
        } catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
    
    /**
     * Changes the active working tab, sets the parameters accordingly and
     * resets the previous information in the browser.
     * @param e 
     */
    public void changeActiveTab(ActionEvent e) {
        this.setMassege("");
        String lastTab = activTab;
        this.setActivTab(e.getComponent().getId());
        switch (lastTab){ 
            case "advanceSearch":
            case "SearchFast":
                if(this.activTab.equals("SearchFast")){
                    break;
                }
            case "shows1":
                FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("search");
                FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("show");
                break;
            case "theater":
                FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("theaterBean");
                break;
            case "login":
                if(this.userDisplay.equals("edit"))
                    this.userDisplay = "user infromtion";
                break;  
        }
        if(this.activTab.equals("login")&&this.userID != null){
            try {
                tickets = dbInfo.getUserTickets();
            } catch (SQLException ex) {
                this.setMassege("we had problems connecting the database please try again later "+ex.getMessage());
            } catch (NoAccessException ex) {
                this.setMassege(ex.getMessage());
            }
        }
    }
    
    public String displayManagment(){
        this.setActivTab("users");
        FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("search");
        FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("show");
        return "manager";  
    }
    
    /**
     * Sets the active tab for the fast search.
     */
    public void fastSearch(){
        this.setActivTab("SearchFast");
    }
    
    /**
     * To be called in order to log in a user.
     */
    public void findUser() {
        this.setMassege("");
        try {
            if(dbInfo.setUser(this)){
                userDisplay = "user infromtion";
                if(dbInfo.isManager()){
                    dbInfo = new EventsManagerInfoImpl(this);
                    FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("search");
                    FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("show");
                    this.manager = true;
                }
                this.setPassword(null);
                this.setPassword2(null);
                tickets = dbInfo.getUserTickets();
                this.similer.userRecommadsion(userID);
            }
            else{
                this.setMassege("user not found");
                this.userID = null;
            }
        } catch (SQLException ex) {
            this.setMassege("we had problems connecting the database please try again later "+ex.getMessage());
        } catch (NoAccessException ex) {
            this.setMassege(ex.getMessage());
        }
   }
    
   /**
    * To be called when adding editing a user in the web application.
    * @return the page name to render next. Stay in the same page if an 
    * empty string is returned.
    */
   public String insertUser() {
       this.setMassege("");
        try {
            if(!password.equals(password2)){
                this.setMassege("passwords dont match");
                return "";
            }
            if(this.userDisplay.equals("edit")){
                if(this.getPassword().equals(""))
                    this.setPassword(null);
                if(dbInfo.editUser(this)){
                    userDisplay = "user infromtion";
                    this.setPassword2(null);
                    return "";
                }else{
                    this.setMassege("we could not add the user");
                    return "";
                }
            }
            if(!dbInfo.validUserName(userID)) {
                this.setMassege("this user exits already");
                return "";
            }
            if(dbInfo.insertUser(this)){
                userDisplay = "user infromtion";
                this.setPassword(null);
                this.setPassword2(null);
                this.activTab = "home";
                return "index?faces-redirect=true";
            }else{
                this.setMassege("we could not add the user");
                return "";
            }
        } catch (SQLException ex) {
            this.setMassege("we had problems connecting the database please try again later "+ex.getMessage());
            return "";
        } catch (NoAccessException ex) {
            this.setMassege(ex.getMessage());
            return "";
        }
   }
    
   /**
    * To be called when cancelling a ticket in the application.
    * @param row the row of the ticket in the rowset table.
    */
   public void cancelTicket(int row) {
       this.setMassege("");
        try {
            tickets.absolute(row);
            if(dbInfo.cancelTicket(tickets.getInt("ticket_no"))){
                tickets.deleteRow();
            }else{
                this.setMassege("no changes made in the DB");
            }  
        } catch (SQLException ex) {
            this.setMassege("we had problems connecting the database please try again later "+ex.getMessage());
        } catch (NoAccessException ex) {
            this.setMassege(ex.getMessage());
        }
   }
   
   /**
    * Sets the active tab for the editing user information.
    */
   public void editPage(){
       this.userDisplay = "edit";
   }
   
   
   /**
    * To be called on logout.
    */
   public void logOut(){
        dbInfo.logOut();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession(); 
    }
   
   
    public List<String> getImagesList() {
        return this.similer.getImagesList();
    }

    public void setImagesList(List<String> imagesList) {
        this.imagesList = imagesList;
    }
   
   public String getActivTab(){
        return activTab;
    }
   
    public void setActivTab(String activTab){
        this.activTab = activTab;
    }
    
    public boolean isManager(){
        return manager;
    }
    
    public EventsInfoImpl getDbInfo(){
        return dbInfo;
    }
    
    public void setDbInfo(EventsInfoImpl aa){
        dbInfo = aa;
    }
    
    public FilteredRowSet getTickets() {
        return tickets;
    }

    public void setTickets(FilteredRowSet tickets) {
        this.tickets = tickets;
    }
   
    public String getName()
   {
      return name;
   } 

   public void setName( String name )
   {
      this.name = name;
   } 

   public String getEmail()
   {
      return email;
   } 
   
   public void setEmail(String email)
   {
      this.email = email;
   }
   
   public String getPhone()
   {
      return phone;
   } 
   
   public void setPhone( String phone )
   {
      this.phone = phone;
   } 
   
   public String getPassword()
   {
       return password;
   }
   
   public void setPassword( String password )
   {
      this.password = password;
   } 
   
   public String getPassword2()
   {
       return password2;
   }
   
   public void setPassword2( String password2 )
   {
      this.password2 = password2;
   } 
   
  public String getUserID()
   {
       return userID;
   }
   
   public void setUserID( String userID)
   {
      this.userID = userID;
   } 
   
   public String getAddress()
   {
       return address;
   }
   
   public void setAddress(String address)
   {
      this.address = address;
   } 
     
   public void setCity(String city)
   {
      this.city = city;
   } 
   
   public String getCity()
   {
       return city;
   }
   
   public String getMassege()
   {
       return message;
   }
   
   public void setMassege(String massege)
   {
       this.message = massege;
   }
   
    public String getUserDisplay()
    {
      return userDisplay;
    } 
    
    public GenaralSimilarity getSimiler() {
        return similer;
    }

    public void setSimiler(GenaralSimilarity similer) {
        this.similer = similer;
    }
    
    
    
    /**
     * Used for testing tolerance.
     * @throws NoAccessException
     * @throws SQLException 
     */
    public void findUserTest() throws NoAccessException, SQLException {
        try {
            if(dbInfo.setUser(this)){
                if(dbInfo.isManager()){
                    dbInfo = new EventsManagerInfoImpl(this);
                    this.manager = true;
                }
                this.setPassword(null);
                this.setPassword2(null);
                tickets = dbInfo.getUserTickets();
            }
            else{
                this.userID = null;
            }
        } catch (SQLException ex) {
            this.setMassege("we had problems connecting the database please try again later "+ex.getMessage());
            throw ex;
        } catch (NoAccessException ex) {
            this.setMassege(ex.getMessage());
            throw ex;
        }
   }


/**
    * Used for Checking tolerance.
    * @return
    * @throws NoAccessException
    * @throws SQLException 
    */
   public String insertUserTest() throws NoAccessException, SQLException {
        try {
            if(!password.equals(password2)){
                return "";
            }
            if(this.userDisplay.equals("edit")){
                if(this.getPassword().equals(""))
                    this.setPassword(null);
                if(dbInfo.editUser(this)){
                    this.setPassword2(null);
                    return "";
                }else{
                    return "";
                }
            }
            if(!dbInfo.validUserName(userID)) {
                return "";
            }
            if(dbInfo.insertUser(this)){
                this.setPassword(null);
                this.setPassword2(null);
                return "index";
            }else{
                this.setMassege("we could not add the user");
                return "";
            }
        } catch (SQLException ex) {
            this.setMassege("we had problems connecting the database please try again later "+ex.getMessage());
            throw ex;
        } catch (NoAccessException ex) {
            this.setMassege(ex.getMessage());
            throw ex;
        }
   }

/**
    * Used for testing.
    */
   public void logOutTest(){
        dbInfo.logOut();
    }
}
