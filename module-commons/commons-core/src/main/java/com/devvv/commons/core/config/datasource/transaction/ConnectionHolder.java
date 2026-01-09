package com.devvv.commons.core.config.datasource.transaction;

import lombok.Data;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Create by WangSJ on 2023/07/03
 */
@Data
public class ConnectionHolder {

    private int referenceCount = 0;

    private String user;
    private String password;
    private Connection conn;


    public ConnectionHolder(Connection conn){
        this.conn = conn;
    }
    public ConnectionHolder(Connection conn, String user, String pwd){
        this.conn = conn;
        this.user = user;
        this.password = pwd;
    }


    public boolean equalUserAndPwd(String user, String pwd){
        return (this.user == user && this.password == pwd);
    }

    public void close()throws SQLException {
        if(conn != null && !conn.isClosed()){
            if(this.conn instanceof MyProxyConnection){
                ((MyProxyConnection) this.conn).forceClose();
            }else{
                this.conn.close();
            }
        }
    }

    public void rollback()throws SQLException{
        if(conn != null){
            this.conn.rollback();
        }
    }

    public void commit()throws SQLException{
        if(conn != null){
            this.conn.commit();
        }
    }

    public void requested() {
        this.referenceCount++;
    }

    public void released() {
        this.referenceCount--;
    }

    public int getReferenceCount() {
        return referenceCount;
    }

    @Override
    public String toString() {
        return conn != null ? conn.toString() : null;
    }
}
