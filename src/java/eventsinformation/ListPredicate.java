/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eventsinformation;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.Predicate;

/**
 *
 * @author Admin
 */
public class ListPredicate implements Predicate {
    
    private String prefix;
    private final String columnLable;
    
    public ListPredicate(String prefix, String columnLable) {
        this.prefix = prefix.toLowerCase();
        this.columnLable = columnLable;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix.toLowerCase();
    }

    @Override
    public boolean evaluate(RowSet rs) {
        CachedRowSet frs = (CachedRowSet)rs;
        boolean evaluation = false;
        
        String columnValue;        
        try {
            columnValue = frs.getString(this.columnLable).toLowerCase();
        } catch (SQLException ex) {
            Logger.getLogger(ListPredicate.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        if(columnValue.startsWith(prefix)) {
            evaluation = true;
        }
        return evaluation;
        
    }

    @Override
    public boolean evaluate(Object value, int column) throws SQLException {
        return true;
    }

    @Override
    public boolean evaluate(Object value, String columnName) throws SQLException {
        return true;
    }
    
}
