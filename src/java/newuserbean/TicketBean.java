
package newuserbean;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
* A bean that represents a ticket of an event.
* @author Shani
*/
@ManagedBean()
@ViewScoped
public class TicketBean implements Serializable{
   // ticket id
   private int ticket;
   private int row;
   private int seat;
   private double price;
   private String available;
   
   
   /**
    * Empty Constructor
    */
   public TicketBean() {
       
   }
   
   /**
     * Constructor For the TicketBean.
     * @param row the row of the ticket.
     * @param seat the seat number in the row
     * @param price ticket price.
     * @param available 'yes' if already reserved, false otherwise.
     * @param ticket the ticket id.
    */
   public TicketBean(int row, int seat, double price, boolean available, int ticket) {
       this.row = row;
       this.seat = seat;
       this.price = price;
       if(available)
           this.available = "no";
       else
           this.available = "yes";
       this.ticket = ticket;
   }

   public String getAvailable() {
       return available;
   }

   public void setAvailable(String available) {
       this.available = available;
   }
   
   public double getPrice() {
       return price;
   }

   public void setPrice(double price) {
       this.price = price;
   }

   public int getTicket() {
       return ticket;
   }

   public void setTicket(int ticket) {
       this.ticket = ticket;
   }
   
   public int getRow() {
       return row;
   }

   public void setRow(int row) {
       this.row = row;
   }

   public int getSeat() {
       return seat;
   }

   public void setSeat(int seat) {
       this.seat = seat;
   }
   
}