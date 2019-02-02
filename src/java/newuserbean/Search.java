/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newuserbean;

import eventsinformation.*;
import static eventsinformation.EventsInfoImpl.rowSetToList;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.sql.rowset.FilteredRowSet;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.ScheduleEvent;

/**
 *
 * @author Shani
 */
@ManagedBean()
@ViewScoped
public class Search implements Serializable{

    
    private String aSearchOrResult;
    //for showNames
    private List<String> textList;
    //for the show result
    private List<Show> showList = new ArrayList<>();
    //the show selected
    private Show slectedRowShow;
    
    //for the event result
    private List<EventBean> eventList = new ArrayList<>();
    //the event selected
    private EventBean slectedRowEvent;
    
    //for messeges
    private String message;
    
    private List<SeatBean> seats  = new ArrayList<>();
    
    private String showNames;
    private String categoryName;
    private String yearProduced;
    private String cityName;
    private Date dateShow;
    private int showCode;
    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    
    @ManagedProperty(value = "#{show}")
    private Show show;
    
    @ManagedProperty(value = "#{eventBean}")
    private EventBean event;

    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;
    
    

    

    
    
    
    
    
    

    
    @PostConstruct
    public void init() {
        if(aSearchOrResult.equals("shows")){
            
        }
        
    }
    

   
    
    public Search() {
        aSearchOrResult = "search";
    }

    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    
    public void setShow(Show show) {
        this.show = show;
    }
     
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }
    
    public void setEvent(EventBean event) {
        this.event = event;
    }
    
    public String getShowNames() {
        return showNames;
    }

    public void setShowNames(String showNames) {
        this.showNames = showNames;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getYearProduced() {
        return yearProduced;
    }

    public void setYearProduced(String yearProduced) {
        this.yearProduced = yearProduced;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Date getDateShow() {
        return dateShow;
    }

    public void setDateShow(Date dateShow) {
        this.dateShow = dateShow;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<Show> getShowList() {
        return showList;
    }

    public void setShowList(List<Show> showList) {
        this.showList = showList;
    }

    public List<EventBean> getEventList() {
        return eventList;
    }

    public void setEventList(List<EventBean> eventList) {
        this.eventList = eventList;
    }
    
    public List<SeatBean> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatBean> seats) {
        this.seats = seats;
    }
    
    public String getASearchOrResult() {
        return aSearchOrResult;
    }

    public void setASearchOrResult(String aSearchOrResult) {
        this.aSearchOrResult = aSearchOrResult;
    }
    
    
    
   
    
    
    public List<String> completeText(String quary) throws SQLException {
        if(1!=quary.length()){
            return textList;
        }
        else{
            textList = dbInfo.getShowsList(quary);
            return textList;
        }
    }
    
    public List<String> completeCategory() throws SQLException{
        return dbInfo.getCategoriesList();
    }
    
    public List<String> completeCity() throws SQLException {
        return dbInfo.getCitiesList();
    }
    
    public List<String> completeTheater() throws SQLException {
        return rowSetToList(dbInfo.getTheaters(), "theater_name");        
    }
    
    
    
    
    public void slectedShowAndDate(SelectEvent event){
        ScheduleEvent theInstance = (ScheduleEvent)event.getObject();
        showNames = theInstance.getTitle();
        dateShow = theInstance.getStartDate();
        cityName = categoryName = yearProduced = null;
        try {
            java.sql.Date sd = new java.sql.Date(dateShow.getTime());
            FilteredRowSet rs = dbInfo.SearchShow(null, showNames, null, sd, null);
            if(rs.size() != 1){
                this.showSearch();
            }
            else{
                rs.beforeFirst();
                rs.next();
                this.setSlectedRowShow(new Show(rs.getInt("show_code"), rs.getString("show_name"), rs.getString("category_name"), rs.getString("description"), rs.getString("year_produced"), rs.getString("show_length")));
            }
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later");
        }
        this.userBean.fastSearch();
    }
    
    public void onDateSelect(SelectEvent event) {
        dateShow = (Date)event.getObject();
        showNames = cityName = categoryName = yearProduced = null;
        this.showSearch();
        this.userBean.fastSearch();
    }
    
    public void showFastSearch(){
        cityName = categoryName = yearProduced = null;
        dateShow = null;
        this.showSearch();
    }
    
    public void showSearch() {
        if(cityName == null|| cityName.equals(""))
            cityName = null;
        if(categoryName == null || categoryName.equals(""))
            categoryName = null;
        if(yearProduced == null || yearProduced.equals(""))
            yearProduced = null;
        if(showNames == null || showNames.equals(""))
            showNames = null;
        FilteredRowSet rs;
        try {
            if(dateShow==null)
                rs = dbInfo.SearchShow(categoryName, showNames, yearProduced, null, cityName);
            else{
                java.sql.Date sd = new java.sql.Date(dateShow.getTime());
                rs = dbInfo.SearchShow(categoryName, showNames, yearProduced, sd, cityName);
            }
            rs.beforeFirst();
            showList = new ArrayList<>();
            while(rs.next()) {
                showList.add(new Show(rs.getInt("show_code"), rs.getString("show_name"), rs.getString("category_name"), rs.getString("description"), rs.getString("year_produced"), rs.getString("show_length")));
            }
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later");
        }
        if(showList.isEmpty()){
            this.setMessage("no results found for your search please try again");
            return ;
        }
        aSearchOrResult = "shows";
    }
    
    public Show getSlectedRowShow(){
        return slectedRowShow;
    }

    
    public void setSlectedRowShow(Show show){
        slectedRowShow = show;
        if(show !=null){
            this.show.setShowCode(show.getShowCode());
            this.show.setCategory(show.getCategory());
            this.show.setName(show.getName());
            this.show.setYear(show.getYear());
            this.show.setLength(show.getLength());
            showCode = show.getShowCode();
        }
        else{
            this.setMessage("we had some problem, please try again");
            return;
        }
        FilteredRowSet rs;
        try {
            if(dateShow==null)
                rs = dbInfo.SearchEvents(null, null, showCode, cityName);
            else{
                java.sql.Date sd = new java.sql.Date(dateShow.getTime());
                rs = dbInfo.SearchEvents(sd, sd, showCode, cityName);
            }
            rs.beforeFirst();
            eventList = new ArrayList<>();
            while(rs.next()) {
                eventList.add(new EventBean(rs.getInt("event_code"), rs.getDate("event_date"), rs.getTime("event_time"), rs.getInt("theater_code"), rs.getString("city_name"), rs.getInt("available"), this.showCode));
            }
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later");
        }
        aSearchOrResult = "events";
    }
    
    public EventBean getSlectedRowEvent(){
        return slectedRowEvent;
    }

    public void setSlectedRowEvent(EventBean event) {
        slectedRowEvent = event;
        try {
            FilteredRowSet rs = dbInfo.getEventTickets(event.getCode());
            
            rs.beforeFirst();
            seats = new ArrayList<>();
            while(rs.next()) {
                seats.add(new SeatBean(rs.getInt("row"),rs.getInt("seat"), rs.getDouble("price"), rs.getString("assigned"), rs.getInt("ticket_no")));
                }
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later");
        }
        RequestContext.getCurrentInstance().execute("PF('seatDialog').show()");
    }
    
    public boolean bookTicket(SeatBean ticket){
        int count = seats.indexOf(ticket);
        try {
            if(ticket.bookTicket()) {
                seats.set(count, ticket);
                return true;
            }
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later");
        } catch (NoAccessException ex) {
            this.setMessage(ex.getMessage());
        }
        this.setMessage("ticket unavalible");
        return false;
    }

    public boolean deleteShow(Show show) {
        if(dbInfo instanceof EventsManagerInfoImpl){
            try {
                ((EventsManagerInfoImpl)dbInfo).deleteShow(show.getShowCode());
            } catch (SQLException ex) {
                this.setMessage("we had problems connecting the database please try again later");
            }
            showList.remove(show);
            return true;
        }
        this.setMessage("you arent a manager");
        return false;
    }
    
    
    public void addShow(){
        try {
            if(show.showOk()){
                if(dbInfo instanceof EventsManagerInfoImpl){
                    ((EventsManagerInfoImpl)dbInfo).insertShow(show);
                    for(int i = 0; i<showList.size(); i++){
                        if(showList.get(i).getShowCode() == show.getShowCode())
                            showList.remove(i);
                    }
                    showList.add(show);
                    RequestContext.getCurrentInstance().execute("PF('eventDialog').hide()");
                }
                else{
                    this.setMessage("you arent a manager");
                }
            }
            else
                this.setMessage("you need to fill all fileds");
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later");
        }
    }
    
    public boolean deleteEvent(EventBean event){
        if(dbInfo instanceof EventsManagerInfoImpl){
            try {
                ((EventsManagerInfoImpl)dbInfo).deleteEvent(event.getCode());
            } catch (SQLException ex) {
                this.setMessage("we had problems connecting the database please try again later");
            }
            eventList.remove(event);
            return true;
        }
        this.setMessage("you arent a manager");
        return false;
    }
    
    
    public void addEvent(){
        this.event.setShow(showCode);
        try {
            if(event.eventOk()){
                if(dbInfo instanceof EventsManagerInfoImpl){
                    ((EventsManagerInfoImpl)dbInfo).insertEvent(event);
                    for(int i = 0; i<eventList.size(); i++){
                        if(eventList.get(i).getCode() == event.getCode())
                            eventList.remove(i);
                    }
                    eventList.add(event);
                    RequestContext.getCurrentInstance().execute("PF('eventDialog').hide()");
                }
                else{
                    this.message = "you arent a manager";
                }
            }
            else
                this.message = "you need to fill all fileds";
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later");
        }
    }
    
    
    
    
    
    
}
