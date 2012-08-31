package com.cdyweb.tc.jdbc;

import com.cdyweb.tc.domain.TemperatureStatus;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class ORM {

    protected transient static final Log logger = LogFactory.getLog(ORM.class);
    private JdbcTemplate tp;

    public ORM() {
    }

    /**
     * setter
     * @param dataSource the dataSource
     */
    public void setDataSource(DataSource dataSource) {
        try {
            dataSource.getConnection();
            this.tp = new JdbcTemplate(dataSource,true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage(),ex);
            System.exit(-1);
        }
    }

    private SqlRowSet querySql(String sql) {
        logger.info(sql);
        return this.tp.queryForRowSet(sql);
    }

    public void executeSql(String sql) {
        logger.info(sql);
        this.tp.execute(sql);
    }

    public Object getOneValue(String sql) {
        logger.info("getOneValue: "+sql);
        SqlRowSet q=querySql(sql);
        Object result=null;
        if (q.next()) {
            result=q.getObject(1);
        }
        return result;
    }
    
    public void insert(TemperatureStatus t) {
      Object[] args=new Object[2];
      args[0]=t.pv;
      args[1]="";
      if (t.cooling) args[1]="cooling";
      if (t.heating) args[1]="heating";
      if (t.heating && t.cooling) args[1]="heating,cooling";
      String sql="replace into pv set pv=?,output=?";
      tp.update(sql,args);
    }
    
    public void setSetPoint(Double sp) {
      String sql="replace into sp set sp=?";
      Object[] args=new Object[1];
      args[0]=sp;
      tp.update(sql,args);
    }

    public List<Object[]> hist(String what) {
      long t=System.currentTimeMillis() - (1000*60*60*24*2);
      return this.hist(what,new Date(t));
    }

    public List<Object[]> hist(String what, Date start) {
      String sql="select * from "+what+" where dt>=? order by dt desc";
      Object[] args=new Object[1];
      args[0]=start;
      SqlRowSet q = this.tp.queryForRowSet(sql,args);
      List<Object[]> result = new ArrayList<Object[]>();
      while (q.next()) {
        Object[] row=new Object[2];
        row[0]=q.getDate("dt").getTime();
        row[1]=q.getDouble(what);
        result.add(row);
      }
      return result;
    }

    /**
    public boolean getData(String tbl, PropertyBaseList dest, int max) throws Exception {
        if (!tbl.equals((currentTbl))) {
            String sql="select * from ["+tbl+"]";
            if (!first_run && "product".equals(tbl)) sql+=" where [reference]>0";
            if (!first_run && "maat".equals(tbl)) sql+=" where [productid] in (select [productid] from [product] where [reference]>0)";
            currentSet=querySql(sql);
            currentTbl=tbl;
        }
        return dest.fromRowSet(currentSet,max);
    }
    **/

}
