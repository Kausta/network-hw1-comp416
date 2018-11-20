package com.llamas.networkhw2.database;

import java.nio.file.Paths;
import java.sql.*;

/**
 * used this tutorial http://www.sqlitetutorial.net/sqlite-java/create-database/
 */
public class SQLiteDatabase{
    private Connection connection;
    private String url, name;

    public SQLiteDatabase(String name){
        this.name = name;
        url = "jdbc:sqlite:" + Paths.get("").toAbsolutePath().toString() + "/db/test.db";
        connection = connectToDatabase();
        prepareTable();
    }

    public Connection connectToDatabase(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url);
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println(meta.getDriverName());
            System.out.println("Success");
        }catch (SQLException e){
            e.printStackTrace();
        }
        return conn;
    }

    public void prepareTable() {
        
        String sql = "CREATE TABLE IF NOT EXISTS '" + name + "' (\n"
                + "	key text PRIMARY KEY,\n"
                + "	value text NOT NULL\n"
                + ");";
        
        execute(sql);

    }

    public void create(String key, String value) {
        String val = retrieve(key);
        String sql;
        if(val == null){
            sql = "INSERT INTO '" + name + "'(key,value) VALUES('" + key + "','" + value + "')";
            execute(sql);
        }else{
            update(key, value);
        }
    }

    public String retrieve(String key){
        String value = null;
        String sql = "SELECT value FROM '" + name + "' WHERE key='" + key + "';";
        try (Statement stmt  = connection.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            if(!rs.isClosed())
                value = rs.getString("value");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    public void update(String key, String value) {
        String sql = "UPDATE '" + name + "' SET value = '" + value
                + "' WHERE key = '" + key + "';";
        
        execute(sql);
    }

    public void delete(String key) {
        String sql = "DELETE FROM '" + name + "' WHERE key = '" + key + "';";
        execute(sql);
    }

    public void execute(String sql){
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();;
        }
    }

    public void close() throws SQLException {
        connection.close();
    }
}