package newuserbean;

import eventsinformation.EventsInfoImpl;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.sql.rowset.FilteredRowSet;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.LazyScheduleModel;

/**
 * Represents a Calendar with events on the matching dates.
 */
@ManagedBean()
@ViewScoped
public class CalendarBean {

    private LazyScheduleModel lazyEventModel;    
    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    
    /**
     * Loads the monthly calendar and displays it's events.
     */
    @PostConstruct
    public void init() {
        lazyEventModel = new LazyScheduleModel() {
            @Override
            public void loadEvents(Date start, Date end) {
                FilteredRowSet rs;
                LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if(startDate.isBefore(LocalDate.now()))
                    startDate = LocalDate.now();
                LocalDate finishDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                try {
                    rs = dbInfo.searchEvents(java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(finishDate), null, null);
                    rs.beforeFirst();
                    while(rs.next()) {
                        Date date = aDayAndTime(rs.getDate("event_date"),rs.getTime("event_time"));
                        addEvent(new DefaultScheduleEvent(rs.getString("show_name"), date, date));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(EventBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }   
        };
    }    
    
    /**
     * Gets the date information in Date format.
     * @param base
     * @param time 
     * @return Date object
     */
    private Date aDayAndTime(Date base, Time time) {
        Calendar date = Calendar.getInstance();
        date.setTime(base);
        date.set(Calendar.DATE, base.getDate());
        date.set(Calendar.HOUR, time.getHours());
        date.set(Calendar.MINUTE, time.getMinutes());
        return date.getTime();
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