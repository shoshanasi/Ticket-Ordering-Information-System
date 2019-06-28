package test;


import eventsinformation.NoAccessException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import newuserbean.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Admin
 */
public class Tester implements Runnable {

    private final int index;

    public Tester(int index) {
        this.index = index;
    }
    
    @Override
    public void run() {
        MainBean user = new MainBean();         
        user.setUserID("user" + index);
        user.setPassword("password" + index);
        user.setPassword2("password" + index);
        user.setName("name" + index);
        boolean doWhile = true;
        while(doWhile) {
            try {
                user.insertUserTest();
                doWhile = false;
            } catch (NoAccessException ex) {
                doWhile = false;
                Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);                               
            } catch (SQLException ex) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex1) {} 
            }
        }
        user.logOutTest();
        doWhile = true;
        while(doWhile) {
            try {
                user.findUserTest();
                doWhile = false;
            } catch (NoAccessException ex) {
                doWhile = false;
                Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);                             
            } catch (SQLException ex) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex1) {}   
            }
        }
       
        System.out.println(index + ": " + user.getName() + ", " + user.getUserID());
        System.out.println("Thread " + index + " finished running succesfully");
    }
    
    
    
        
}
