
package newuserbean;

import eventsinformation.*;
import static eventsinformation.EventsInfoImpl.rowSetToList;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.sql.rowset.FilteredRowSet;
import javax.sql.rowset.RowSetProvider;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.ScheduleEvent;

/**
 * Represents the different search options and states in the application.
 * Searches Shows, Events out of Shows, Tickets for Events and reserves the Tickets.
 * @author Shani
 */
@ManagedBean(name = "search")
@ViewScoped
public class Search implements Serializable {

    //for show names search
    private FilteredRowSet initialList;
    
    //for the show result
    private List<ShowBean> showList = new ArrayList<>();
    //needed for prime faces data table.
    private ShowBean slectedRowShow;
   
    //for the event result
    private List<EventBean> eventList = new ArrayList<>();
    //needed for prime faces data table.
    private EventBean slectedRowEvent;
    
    //the tickets of the event
    private List<TicketBean> seats  = new ArrayList<>();
    //the unaasiagend tickets of the event
    private List<Integer> unassiagendSeats = new ArrayList<>();
    //the tickets selected by the user
    private List<Integer> selectedSeats = new ArrayList<>();
    //if unassianged seats choosed
    private int numOfUnssiend;
    //number of seats in a row. needed for the xhtml
    private int numRowSeats;

    
    //for messeges
    private String message;
    
    //general, shows or events search.
    private String aSearchOrResult;
    
    //search parmaters
    private String showNames;
    private String categoryName;
    private String yearProduced;
    private String cityName;
    private Date dateShow;    
    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    
    //the sessions show for reuse.
    @ManagedProperty(value = "#{showBean}")
    private ShowBean show;
    
     //the sessions event for reuse.
    @ManagedProperty(value = "#{eventBean}")
    private EventBean event;

    //nedded for calander
    @ManagedProperty(value = "#{userBean}")
    private MainBean userBean;
    
    /**
     * Constructor representing the initial general search.
     */
    public Search(){
        aSearchOrResult = "search";
        try {
            //empty filteredrowset.
            initialList = RowSetProvider.newFactory().createFilteredRowSet();
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
        }
    }

    
    /**
     * Called by the calendar. Initiates a show search by name and date.
     * @param event a SelectEvent
     */
    public void selectedShowAndDate(SelectEvent event){
        ScheduleEvent theInstance = (ScheduleEvent)event.getObject();
        showNames = theInstance.getTitle();
        dateShow = theInstance.getStartDate();
        cityName = categoryName = yearProduced = null;
        this.showSearch();
        this.userBean.fastSearch();
    }
    
    /**
     * Called by the calendar. Initiates a show search by date.
     * @param event a SelectEvent
     */
    public void onDateSelect(SelectEvent event) {
        dateShow = (Date)event.getObject();
        showNames = cityName = categoryName = yearProduced = null;
        this.showSearch();
        this.userBean.fastSearch();
    }
    
    /**
     * Called for a fast search by show name.
     */
    public void showFastSearch(){
        cityName = categoryName = yearProduced = null;
        dateShow = null;
        this.showSearch();
    }
    
    /**
     * Called when searching for a show.
     */
    public void showSearch() {
        this.setMessage("");
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
            java.sql.Date date;
            if(dateShow == null)
                date = null;
            else
                date = new java.sql.Date(dateShow.getTime());
            if((dbInfo instanceof EventsManagerInfoImpl) && dateShow == null && this.cityName == null){
                rs = ((EventsManagerInfoImpl)dbInfo).searchShow(categoryName, showNames, yearProduced);
            }else{
                rs = dbInfo.searchShow(categoryName, showNames, yearProduced, date, cityName);
            }
            showList.clear();
            while(rs.next()) {
                showList.add(new ShowBean(rs.getInt("show_code"), rs.getString("show_name"), rs.getString("category_name"), rs.getString("description"), rs.getString("year_produced"), rs.getString("show_length")));
            }
            
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
            return;
        }
        if(showList.isEmpty()){
            this.setMessage("no results found for your search please try again");
            return ;
        }  
        aSearchOrResult = "shows";
        if(showList.size() == 1){
            this.setSlectedRowShow(showList.get(0));
        }
    }
    
    /**
     * @return the current selected show if any.
     */
    public ShowBean getSlectedRowShow(){
        return slectedRowShow;
    }

    /**
     * Selects a show for search of it's events.
     * called when a show is selected in the application.
     * @param show the selected show.
     */
    public void setSlectedRowShow(ShowBean show){
        this.setMessage("");
        if(cityName == null|| cityName.equals(""))
            cityName = null;
        this.slectedRowShow = show;
        try {
            if(show !=null){
                this.show.setShowCode(show.getShowCode());
                this.show.setCategory(show.getCategory());
                this.show.setName(show.getName());
                this.show.setYear(show.getYear());
                this.show.setLength(show.getLength());
                this.show.setDescription(show.getDescription());
                this.show.setImagesList();
                this.show.setReviewList();
            }else{
                return;
            }
            FilteredRowSet rs;
            java.sql.Date date;
            java.sql.Date startDate;
            if(dateShow != null)
                startDate = date = new java.sql.Date(dateShow.getTime());
            else{
                date = null;
                startDate = (new java.sql.Date((new Date()).getTime()));
            }
            rs = dbInfo.searchEvents(startDate, date, this.show.getShowCode(), cityName);
            eventList.clear();
            while(rs.next()) {
                eventList.add(new EventBean(rs.getInt("event_code"), rs.getDate("event_date"), rs.getTime("event_time"), rs.getString("theater_name"), rs.getInt("theater_code"), rs.getString("city_name"), rs.getInt("available"), this.show.getShowCode()));
            }
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
            return;
        }
        aSearchOrResult = "events";
    }
    
    /**
     * @return the current selected event, if any.
     */
    public EventBean getSlectedRowEvent(){
        return slectedRowEvent;
    }

    /**
     * Selects an event for search of it's tickets.
     * called when a event is selected in the application.
     * @param event the selected event.
     */
    public void setSlectedRowEvent(EventBean event) {
        this.setMessage("");
        this.slectedRowEvent = event;
        if(event == null){
            return;
        }
        try {
            FilteredRowSet rs = dbInfo.getEventTickets(event.getCode());
            seats.clear();
            unassiagendSeats.clear();
            this.numRowSeats = 0;
            while(rs.next()) {
                if(rs.getString("assigned").equals("yes")){
                    if(numRowSeats < rs.getInt("seat"))
                        numRowSeats = rs.getInt("seat");
                    seats.add(new TicketBean(rs.getInt("row"),rs.getInt("seat"), rs.getDouble("price"), rs.getBoolean("available"), rs.getInt("ticket_no")));
                }else if(rs.getBoolean("available")){
                    unassiagendSeats.add(rs.getInt("ticket_no"));
                } 
            }
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
        }
    }
    
    /**
     * Called for toggling a ticket when choosing tickets. 
     * Selects or unselects the ticket.
     * @param ticket the ticket no.
     */
    public void chooseTicket(int ticket){
        if(selectedSeats.contains(ticket)){
            selectedSeats.remove((Integer)ticket);
            for(TicketBean seat : seats)
                if(seat.getTicket() == ticket)
                    seat.setAvailable("no");
        }else{
            selectedSeats.add(ticket);
            for(TicketBean seat : seats)
                if(seat.getTicket() == ticket)
                    seat.setAvailable("yours");
        }
    }
    
    /**
     * Reserves the selected cities.
     */
    public void reserveTickets(){
        try {
            if(selectedSeats.isEmpty()&&this.numOfUnssiend==0){
                FacesMessage msg = new FacesMessage("You didnt select tickets");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            if(numOfUnssiend > unassiagendSeats.size()){
                FacesMessage msg = new FacesMessage("we dont have that much tickets avalibel");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            for(int i=0; i<numOfUnssiend; i++)
                selectedSeats.add(unassiagendSeats.get(i));
            dbInfo.reserveTickets(selectedSeats);
            for(int ticket: selectedSeats) {
                if(!unassiagendSeats.isEmpty() && unassiagendSeats.contains(ticket)){
                    unassiagendSeats.remove((Integer)ticket);
                }
                for(TicketBean seat : seats) {
                    if(seat.getTicket() == ticket)
                        seat.setAvailable("yes");
                }
            }
            String tickets = " tickets ";
            for(int ticket: selectedSeats) {
                tickets += "& "+ticket;
            }
            selectedSeats.clear();
            RequestContext.getCurrentInstance().execute("PF('seatDialog').hide()");
            FacesMessage msg = new FacesMessage("Tickets Selected for "+this.show.toString()+this.getSlectedRowEvent().toString()+tickets);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
        } catch (NoAccessException ex) {
            this.setMessage(ex.getMessage());
        }
        selectedSeats.clear();
        this.setSlectedRowEvent(this.slectedRowEvent);
    }
    
    

    /**
     * Invoked when manager adds a show.
     */
    public void addShow(){
        this.setMessage("");
        try {
            if(show.showOk()){
                if(dbInfo instanceof EventsManagerInfoImpl){
                    boolean worked;
                    if(show.getShowCode() != 0){
                        worked = ((EventsManagerInfoImpl)dbInfo).editShow(show);
                        int thisshow = show.getShowCode();
                        for(int i=0; i<showList.size(); i++) {
                            if(showList.get(i).getShowCode() == thisshow)
                                showList.remove(i);
                        }
                    }else{
                        worked = ((EventsManagerInfoImpl)dbInfo).insertShow(show);
                            }
                    if(worked){
                        this.showList.add(new ShowBean(show.getShowCode(), show.getName(), show.getCategory(), show.getDescription(), show.getYear(), show.getLength()));
                        RequestContext.getCurrentInstance().execute("PF('showDialog').hide()");
                    }else{
                        this.setMessage("no changes made in the DB");
                    }
                }else{
                    this.setMessage("you arent a manager");
                }
            }else
                this.setMessage("you need to fill all fileds");
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
        }
        System.out.println("add show 2");
    }
    
    /**
     * Invoked when manager deletes a show
     * @param show ShowBean that contains the show code.
     */
    public void deleteShow(ShowBean show) {
        this.setMessage("");
        if(dbInfo instanceof EventsManagerInfoImpl){
            try {
                if(((EventsManagerInfoImpl)dbInfo).deleteShow(show.getShowCode())){
                    showList.remove(show);
                    return ;
                }
                this.setMessage("no changes made in the DB");
                return ;
            } catch (SQLException ex) {
                this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
                return;
            }
        }
        this.setMessage("you arent a manager");
    }
    
    /**
     * Invoked when manager adds an event.
     */
    public void addEvent(){
        this.setMessage("");
        System.out.println("add event 1");
        try {
            if(event.eventOk()){
                if(dbInfo instanceof EventsManagerInfoImpl){
                    this.event.setShow(this.show.getShowCode());
                    boolean worked;
                    if(event.getCode() != 0){
                        worked = ((EventsManagerInfoImpl)dbInfo).editEvent(event);
                        int thisshow = event.getCode();
                        for(int i=0; i<eventList.size(); i++) {
                            if(eventList.get(i).getCode() == thisshow)
                                eventList.remove(i);
                        }
                    }else{
                        worked = ((EventsManagerInfoImpl)dbInfo).insertEvent(event);
                    } 
                    if(worked){
                        this.eventList.add(new EventBean(event.getCode(), event.getSqlDate(), event.getSqlTime(), event.getTheater(),event.getTheaterCode(), event.getCity(),event.getEmptySeats(), event.getShow()));
                        RequestContext.getCurrentInstance().execute("PF('eventDialog').hide()");
                    }else{
                        this.setMessage("no changes made in the DB");
                    }
                }else{
                    this.setMessage("you arent a manager");
                }
            }else{
                this.setMessage("you need to fill all fileds");
            }
        } catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
        } 
    }
    
    /**
     * Invoked when manager deletes an event.
     * @param event EventBean that contains the event code.
     */
    public void deleteEvent(EventBean event){
        this.setMessage("");
        System.out.println("dleate event 1"+this.getMessage());
        if(dbInfo instanceof EventsManagerInfoImpl){
            try {
                if(((EventsManagerInfoImpl)dbInfo).deleteEvent(event.getCode())){
                    eventList.remove(event);
                    return ;
                }
                this.setMessage("no changes made in the DB");
                return ;
            } catch (SQLException ex) {
                this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
                return;
            }
        }
        this.setMessage("you arent a manager");
    }
    
    /**
     * Gets a list of show who's name start with the given initial.
     * @param initial the name initial.
     * @return List of matching shows.
     * @throws SQLException 
     */
    public List<String> completeText(String initial) {
        try{
            if(1!=initial.length()){
                ListPredicate pr = new ListPredicate(initial,"show_name");
                initialList.setFilter(pr);
                return EventsInfoImpl.rowSetToList(initialList, "show_name");
            }
            else{
                initialList = dbInfo.getShows(initial);
                dbInfo.close();
                return EventsInfoImpl.rowSetToList(initialList, "show_name");
            }
        }catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
            return null;
        } 
    }
    
    /**
     * List of all categories - used for the drop down lists.
     * @return List of categories.
     * @throws SQLException 
     */
    public List<String> completeCategory(){
        try{
            List<String> temp = dbInfo.getCategoriesList();
            dbInfo.close();
            return temp;
        }catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
            return null;
        }
    }
    
    /**
     * List of all cities - used for the drop down lists.
     * @return List of cities.
     * @throws SQLException 
     */
    public List<String> completeCity(){
        try{
            List<String> temp = dbInfo.getCitiesList();
            dbInfo.close();
            return temp;
        }catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
            return null;
        }
        
    }
    
    /**
     * List of all theatres - used for the drop down lists.
     * @return List of theatres.
     * @throws SQLException 
     */
    public List<String> completeTheater(){
        try{
            List<String> temp = rowSetToList(dbInfo.getTheaters(), "theater_name");
            dbInfo.close();
            return temp;
        }catch (SQLException ex) {
            this.setMessage("we had problems connecting the database please try again later "+ex.getMessage());
            return null;
        }
    }
    
    
    
    //for managed propty
    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    
    public int getUnassiagendSeats() {
        return unassiagendSeats.size();
    }
    
    public void setShow(ShowBean show) {
        this.show = show;
    }
     
    public void setUserBean(MainBean userBean) {
        this.userBean = userBean;
    }
    
    public void setEvent(EventBean event) {
        this.event = event;
    }
    
    //normal gets and sets
    public String getASearchOrResult() {
        return aSearchOrResult;
    }

    public void setASearchOrResult(String aSearchOrResult) {
        this.aSearchOrResult = aSearchOrResult;
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
    
    public List<ShowBean> getShowList() {
        return showList;
    }

    public void setShowList(List<ShowBean> showList) {
        this.showList = showList;
    }

    public List<EventBean> getEventList() {
        return eventList;
    }

    public void setEventList(List<EventBean> eventList) {
        this.eventList = eventList;
    }
    
    public List<TicketBean> getSeats() {
        return seats;
    }

    public void setSeats(List<TicketBean> seats) {
        this.seats = seats;
    }
    
    public int getNumOfUnssiend() {
        return numOfUnssiend;
    }

    public void setNumOfUnssiend(int numOfUnssiend) {
        this.numOfUnssiend = numOfUnssiend;
    }
    
    public int getNumRowSeats() {
        return numRowSeats;
    }

    public void setNumRowSeats(int numRowSeats) {
        this.numRowSeats = numRowSeats;
    }
    
}
