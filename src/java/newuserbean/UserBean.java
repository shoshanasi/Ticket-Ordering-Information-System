
package newuserbean;


import eventsinformation.*;
import java.io.Serializable;
import java.sql.SQLException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.sql.rowset.FilteredRowSet;


@ManagedBean(name = "userBean")
@SessionScoped
public class UserBean  implements Serializable {

    
    public static final String USER = "root";
    public static final String PASS = "root";
    
    private String userID;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String address;
    private String city;
    
    private String activTab;
    private String loginOrUser;
    private String message;
    private boolean manager;
    private EventsInfoImpl dbInfo;
    private FilteredRowSet rs;
    
    
   
    public UserBean() throws SQLException {
        this.manager = true;
        this.activTab = "home";
        this.loginOrUser = "log in";
        dbInfo = new EventsInfoImpl(UserBean.USER, UserBean.PASS);
    }
    
    
    public void setActiveTab(ActionEvent e){
        String befor = activTab;
        activTab = e.getComponent().getId();
        switch (befor){
            case "Search": 
            case "shows":
                FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("search");
                break;
            case "advanceSearch":
                if(!(this.activTab.equals("Search")))
                    FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("search");
                break;
            case "theater":
                FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("theaterBean");
                break;
            case "login":
                if(this.loginOrUser.equals("edit"))
                    this.loginOrUser = "user infromtion";
                break;
                
        }/*
        if(this.activTab.equals("shows")||this.activTab.equals("Search")){
            FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("search");
        }
        if(this.activTab.equals("theater")){
            FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("theaterBean");
        }
        
        if(!(this.activTab.equals("Search")) && befor.equals("advanceSearch")){
            FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("search");
        }*/
        if(this.activTab.equals("login")&&this.userID != null){
            try {
                rs = dbInfo.getUserTickets();
            } catch (SQLException ex) {
                this.setMassege("we had problems connecting the database please try again later");
            } catch (NoAccessException ex) {
                this.setMassege(ex.getMessage());
            }
        }
        System.out.println("asas");
    }
    
    
    
    
    
    public boolean isManager(){
        return manager;
    }
    
    public String goManager(){
        if(manager){
            activTab = "shows";
            return "manager";
        }
        else{
            this.setMassege("you arent a manager");
            return "";
        }     
    }
    
    public void fastSearch(){
        activTab = "Search";
    }
    
    public boolean findUser() {
        try {
            if(dbInfo.setUser(this)){
                loginOrUser = "user infromtion";
                if(dbInfo.isManager()){
                    dbInfo = new EventsManagerInfoImpl(dbInfo);
                    this.manager = true;
                }
                return true;
            }
            else{
                message = "user not found";
                return false;
            }
        } catch (SQLException ex) {
            this.setMassege("we had problems connecting the database please try again later");
            return false;
        } catch (NoAccessException ex) {
            this.setMassege(ex.getMessage());
            return false;
        }
   }
   
   
   public String insertUser() {
        try {
            if(this.loginOrUser.equals("edit")||dbInfo.validUserName(userID)) {
            } else{
                message = "this user exits already";
                return "";
            }
            if(dbInfo.insertUser(this)){
                loginOrUser = "user infromtion";
                activTab = "home";
                return "index";
            }
            else{
                message = "user not found";
                return "";
            }
        } catch (SQLException ex) {
            message = "we had problems connecting the database please try again later";
            return "";
        } catch (NoAccessException ex) {
            message = ex.getMessage();
            return "";
        }
   }
   
   public FilteredRowSet getTickits() {
       if(rs == null)
           try {
               rs = dbInfo.getUserTickets();
            } catch (SQLException ex) {
            message = "we had problems connecting the database please try again later";
        } catch (NoAccessException ex) {
            message = ex.getMessage();
        }
       return rs;
   }
   
   public void cancelTicket(int code) {
        try {
            rs.absolute(code);
            dbInfo.cancelTicket(rs.getInt("ticket_no"));
            rs.deleteRow();
        } catch (SQLException ex) {
            message = "we had problems connecting the database please try again later";
        } catch (NoAccessException ex) {
            message = ex.getMessage();
        }
   }
   
   public void editPage(){
       this.loginOrUser = "edit";
   }
   
   public void logOut(){
        dbInfo.close();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession(); 
    }
    
   
    
   
   
   
    public String getActivTab(){
        System.out.println(this.activTab);
        return activTab;
    }
    
    public EventsInfoImpl getDbInfo(){
        return dbInfo;
    }
    
    public void setDbInfo(EventsInfoImpl aa){
        dbInfo = aa;
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
   
   public void setEmail( String email )
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
   
   public void setAddress( String address)
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
   
    public String getLoginOrUser()
    {
      return loginOrUser;
    } 
    
   
}