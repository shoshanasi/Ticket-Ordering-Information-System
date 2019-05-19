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
 * A class representing the Category editing page with with an optional added 
 * category and list of categories to show.
 * @author Shani
 */
@ManagedBean()
@ViewScoped
public class CategoryBean {

    private String category;
    private List<String> categoris;
    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;

    /**
     * Gets the category list from the database. Doing from a getter would be called 
     * twice by the xhtml loading.
     */
   @PostConstruct
    public void init() {
        try {
            categoris = dbInfo.getCategoriesList();
        } catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
    /**
     * Constructor for CategoryBean.
     */
    public CategoryBean() {
        categoris = null;
    }
    
    /**
     * Adds a new category that was set in setCategory getter to the db,
     * if it's a manager invoking it.
     */
    public void addCategory(){
        if((categoris.indexOf(this.category)) != -1){
            FacesMessage msg = new FacesMessage("we have this category already");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        try {
            if(dbInfo instanceof EventsManagerInfoImpl){
                if(((EventsManagerInfoImpl)dbInfo).insertCategory(category)){
                    categoris.add(category);
                    this.category = "";
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
     * Removes the given category from the db, if done by a manager.
     * @param category 
     */ 
    public void deleteCategory(String category){
        try {
            if(dbInfo instanceof EventsManagerInfoImpl){
                if(((EventsManagerInfoImpl)dbInfo).deleteCategory(category))
                    categoris.remove(category);
                else{
                    FacesMessage msg = new FacesMessage("no changes made to the DB");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                }
            }
        } catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
    public List<String> getCategoris() {
        return categoris;
    }

    public void setCategoris(List<String> categoris) {
        this.categoris = categoris;
    }    
    
    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }    
    
}