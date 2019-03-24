/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newuserbean;

import eventsinformation.EventsInfoImpl;
import eventsinformation.EventsManagerInfoImpl;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.faces.bean.ManagedProperty;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import javax.mail.Part;
import javax.sql.rowset.FilteredRowSet;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import sun.misc.IOUtils;

/**
 *
 * @author shani
 */
@ViewScoped
@Named
public class Show implements Serializable{

    /**
     * Creates a new instance of Show
     */
    
    private String name;
    private String category;
    private String year;
    private String length;
    private String description;
    private int showCode;
    
    
    private List<String> picsRs = new ArrayList<>();
    private FilteredRowSet reviewRs;

    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    
    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    
    

   

    
   private UploadedFile file;
 
    public UploadedFile getFile() {
        return file;
    }
 
    public void setFile(UploadedFile file) {
        this.file = file;
    }
     
    public void upload() {
        if(file != null) {
            FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
     
    public void handleFileUpload(FileUploadEvent event) {
        System.out.println("sucssaaaaaa");
        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
   
    
    

    public Show(int code, String name, String category, String descrption, String year, String length){
        this.showCode = code;
        this.name = name;
        this.category = category;
        this.description = descrption;
        this.year = year;
        this.length = length;
        this.picsRs.add("/aass/1000.jpg");
        this.picsRs.add("/aass/1001.jpg");
        this.picsRs.add("/aass/1002.jpg");
        this.picsRs.add("/aass/1003.jpg");
    }
    
    
    
    
    public List<String> getPicsRs() {
        return picsRs;
    }

    public void setPicsRs(List<String> picsRs) {
        this.picsRs = picsRs;
    }
    
    public FilteredRowSet getReviewRs() {
        return reviewRs;
    }

    public void setReviewRs(FilteredRowSet reviewRs) {
        this.reviewRs = reviewRs;
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
