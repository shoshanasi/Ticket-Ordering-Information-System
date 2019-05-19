
package newuserbean;


import eventsinformation.EventsInfoImpl;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.servlet.http.Part;
import javax.sql.rowset.FilteredRowSet;

/**
 * A Bean class representing a Show information.
 * @author Shani
 */
@ManagedBean(name = "showBean")
@ViewScoped
public class ShowBean implements Serializable{
    
    private final String IMAGE_PATH = "C:\\Users\\isral\\Documents\\NetBeansProjects\\ReservationApp\\web";
    
    private String name;
    private String category;
    private String year;
    private String length;
    private String description;
    private int showCode;
    
    private String review;
    
    private Part file;
    
    //images for the show
    private List<String> imagesList = new ArrayList<>();
    //reviews for the show.
    private FilteredRowSet reviewList;

    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    
    /**
     * Empty Constructor.
     */
    public ShowBean() {
        
    }
    
    /**
     * Constructor For the ShowBean.
     * @param code show code.
     * @param name show name
     * @param category the category the show belongs to.
     * @param descrption a description of the show.
     * @param year the year produced.
     * @param length the length of the show.
     */
    public ShowBean(int code, String name, String category, String descrption, String year, String length) {
        this.showCode = code;
        this.name = name;
        this.category = category;
        this.description = descrption;
        this.year = year;
        this.length = length;
    }    
     
    /**
     * Uploads and adds a Picture (file) to the show.
     */
    public void upload(){
        /* its only if we change to blobs
        if(file != null) {
            InputStream input = file.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[10240];
            for(int length = 0; (length = input.read(buffer))>0; output.write(buffer ,0, length));
            Blob blob = new javax.sql.rowset.serial.SerialBlob(output.toByteArray());
            dbInfo.insertPicture(showCode, blob);
                
            //FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded.");
            //FacesContext.getCurrentInstance().addMessage(null, message);
        }*/
        try{
            if(file != null) {
                InputStream input = file.getInputStream();
                String fileaddres = file.getSubmittedFileName();
                int place = fileaddres.lastIndexOf("\\");
                fileaddres = fileaddres.substring(place+1, fileaddres.length());
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[10240];
                for(int length = 0; (length = input.read(buffer))>0; output.write(buffer ,0, length));
                InputStream bis = new ByteArrayInputStream(output.toByteArray());
                BufferedImage bImageFromConvert = ImageIO.read(bis);
                File path;
                path = new File(IMAGE_PATH+"\\images\\"+showCode+fileaddres);
                ImageIO.write(bImageFromConvert, "JPG", path);
                dbInfo.insertPicture(showCode, showCode+fileaddres);
                imagesList.add(showCode+fileaddres);
                System.out.println("upload file showbean sucsess");
                FacesMessage msg = new FacesMessage("your photo was added sucessfuly");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        }catch (SQLException | IOException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later "+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        
    }
    
    /**
     * Update this bean's show information.
     * @param show 
     */
    public void editShow(ShowBean show){
        if(show !=null){
            this.setShowCode(show.getShowCode());
            this.setCategory(show.getCategory());
            this.setName(show.getName());
            this.setYear(show.getYear());
            this.setLength(show.getLength());
            this.setDescription(show.getDescription());
        }
    }
    
    /**
     * Checks that all the fields of the show are not null.
     * @return true if all fields are filled, false otherwise.
     * @throws SQLException 
     */
    public boolean showOk() throws SQLException{
        return !(name == null || category == null || description == null || 
                year == null || length == null);
    }
    
    /**
     * Adds a review to the show.
     */
    public void addReview(){
        try {
            dbInfo.insertReview(showCode, review);
            this.setReview("");
            this.setReviewList();
        } catch (SQLException ex) {
            FacesMessage msg = new FacesMessage("we had problems connecting the database please try again later"+ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
    /**
     * Checks if there are any reviews for the show.
     * @return true if there are reviews, true otherwise.
     */
    public boolean isRSNotEmpty(){
        try {
            this.reviewList.beforeFirst();
            if(this.reviewList.next()){
                this.reviewList.beforeFirst();
                return true;
            }
        } catch (SQLException ex) {
            //would not happan
        }
        return false;
    }
    
    /**
     * Empties the fields for the add show form.
     */
    public void addSetup(){
        this.setShowCode(0);
        this.setName("");
        this.setCategory("");
        this.setDescription("");
        this.setYear("");
        this.setLength("");
    }
    
    @Override
    public String toString(){
        return this.getName()+" ";
    }
    
    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    

    public List<String> getImagesList() {
        return imagesList;
    }

    /**
     * Sets the Show's image list, getting it from the db.
     * @throws SQLException the error is caught by search.
     */
    public void setImagesList() throws SQLException {
        imagesList.clear();
        FilteredRowSet rs = null;
        rs = dbInfo.getShowPictures(showCode);
        rs.beforeFirst();
        while(rs.next()){
            this.imagesList.add(rs.getString("pic"));
        }
    }
    
    public FilteredRowSet getReviewList() {
        return reviewList;
    }

    //the error goes to search
    public void setReviewList() throws SQLException {
        this.reviewList = dbInfo.getShowReviews(showCode);
        reviewList.beforeFirst();
        while(reviewList.next()){
            if(reviewList.getString("user_id") == null)
                reviewList.updateString("user_id", "Anonymus");
        }
            
    }
    
    public Part getFile() {
        return file;
    }
 
    public void setFile(Part file) {
        this.file = file;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
    
    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getShowCode() {
        return showCode;
    }

    public void setShowCode(int showCode) {
        this.showCode = showCode;
    }

    
}
