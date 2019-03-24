/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newuserbean;

import eventsinformation.EventsInfoImpl;
import java.sql.SQLException;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.sql.rowset.FilteredRowSet;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

/**
 *
 * @author shani
 */
@ManagedBean()
@ViewScoped
public class CalendarBean {

    private LazyScheduleModel lazyEventModel;
    
    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    /**
     * Creates a new instance of CalendarBean
     */
    @PostConstruct
    public void init() {
        
         
        lazyEventModel = new LazyScheduleModel() {
            
            @Override
            public void loadEvents(Date start, Date end) {
                FilteredRowSet rs;
                LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate finishDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                try {
                    rs = dbInfo.SearchEvents(java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(finishDate), null, null);
                    rs.beforeFirst();
                    while(rs.next()) {
                        System.out.println(startDate);
                        Date date = aDayAndTime(rs.getDate("event_date"),rs.getTime("event_time"));
                        addEvent(new DefaultScheduleEvent(rs.getString("show_name"), date, date));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(EventBean.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                addEvent(new DefaultScheduleEvent("Lazy Event 2", getDate(start), getDate(start)));
            }   
        };
    }
    
    public Date getDate(Date base) {
        Calendar date = Calendar.getInstance();
        date.setTime(base);
        
        return date.getTime();
    }
    
    
    private Date aDayAndTime(Date base, Time time) {
        Calendar date = Calendar.getInstance();
        date.setTime(base);
        date.set(Calendar.DATE, base.getDate());
        date.set(Calendar.HOUR, time.getHours());
         
        return date.getTime();
    }
    
    public void slectedDate(){
        
    }
    
    

    
    public LazyScheduleModel getLazyEventModel() {
        return lazyEventModel;
    }

    public void setLazyEventModel(LazyScheduleModel lazyEventModel) {
        this.lazyEventModel = lazyEventModel;
    }

    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    
}
