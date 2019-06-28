package newuserbean;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 * A class representing a user with it's details with getters and setters.
 * Used in UserView class.
 */
@ManagedBean(name = "user")
@ApplicationScoped
public class User {
    
    private String userID;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String address;
    private String city;

    /**
     * Empty Constructor.
     */    
    public User() {
    }    

    /**
     * Constructor For User.
     * @param userID
     * @param name
     * @param email
     * @param phone
     * @param address
     * @param city 
     */
    public User(String userID, String name, String email, String phone, 
            String address, String city) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

   
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}