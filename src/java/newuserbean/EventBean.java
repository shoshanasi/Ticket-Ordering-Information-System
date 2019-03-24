/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newuserbean;

import eventsinformation.*;
import static eventsinformation.EventsInfoImpl.rowSetToList;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.sql.RowSet;
import javax.sql.rowset.FilteredRowSet;
import org.primefaces.context.RequestContext;

/**
 *
 * @author shani
 */
@ManagedBean()
@ViewScoped
public class EventBean implements Serializable{
    
    private int code;
    private Date date;
    private Time time;
    private int show;
    private int theater;
    private String city;
    private int emptySeats;

    private List<String> theaters;
    private boolean listOrNot;
    private FilteredRowSet rs;
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    private BigDecimal price;
    
   
    
    public EventBean() throws SQLException {
        city = null;
        listOrNot = true;
    }
    
    
    public EventBean(int code, Date date, Time time, int theater, String city, int emptySeats, int show) {
        this.code = code;
        this.city = city;
        this.date = date;
        this.time = time;
        this.theater = theater;
        this.emptySeats = emptySeats;
        this.show = show;
    }
    
    
    /*
    public static List<String> rowSetToList(RowSet rs) throws SQLException {
        List<String> columnList = new ArrayList<>();
        rs.beforeFirst();
        while(rs.next()) {
            columnList.add(rs);
        }
        return columnList;
    }*/
    
    public void setDbInfo(EventsInfoImpl aa){
        dbInfo = aa;
    }
    
    public Date getDate() {
        return date;
    }

    public boolean isListOrNot(){
        return listOrNot;
    }
    
    public void setListOrNot(boolean aa){
        listOrNot = !listOrNot;
    }

    public Time getTime() {
        return time;
    }
    
    public int getEmptySeats() {
        return emptySeats;
    }

    public void setEmptySeats(int emptySeats) {
        this.emptySeats = emptySeats;
    }

    public int getShow() {
        return show; 
    }

    public int getTheater() {
        return theater;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    
    public void setCity(String city){
        this.city = city;
    }
    
    public String getCity(){
        return city;
    }
    
    public String showEvents(int showCode) throws SQLException{
        show = showCode;
        rs = dbInfo.SearchEvents(null, null, showCode, null);
        return "event";
    }
    
    public FilteredRowSet getEvents() throws SQLException, NoAccessException{
        return rs;
    }
    
    

    public void setDate(Date date) {
        this.date = date; 
    }

    public void setTime(Time time) {
        this.time = time; 
    }

    public void setShow(int show) {
        this.show = show; 
    }

    public void setTheater(int theater) {
        this.theater = theater; 
    }

    
    
    public void setTheaters() throws SQLException{
        FilteredRowSet rr = dbInfo.getTheaters(null, city);
        theaters = rowSetToList(rr, "theater_name");
    }
    
    public List<String> getTheaters() {
        return theaters;     
    }
    
    
    public boolean eventOk() throws SQLException{
        return !(date == null||theater == 0||city == null||null == time);
    }

    public BigDecimal getPrice() {
        return price;
    }
}
