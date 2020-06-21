package com.tongji.bi.util;

import com.tongji.bi.entity.CacheEntity;

import java.sql.*;
import java.util.ArrayList;

public class MysqlDriverInitialize {
    private static Connection connection;

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if (connection == null || connection.isClosed()){
            Class.forName(ConstantDefinition.mysqlDriver);
            connection = DriverManager.getConnection(ConstantDefinition.mysqlUrl, ConstantDefinition.mysqlUser, ConstantDefinition.mysqlPassword);
        }
        return connection;
    }

    public static ArrayList<CacheEntity> selectByTypeAndName(int type, String name) throws SQLException, ClassNotFoundException {
        System.out.println("开始从Mysql中查[type:" + type + ",name:" + name + "]");
        PreparedStatement preparedStatement = null;
        name = name.replaceAll(" ", "_");
        if (type == 0){
            String sql = "select * from node where name like ? limit 50";
            preparedStatement = getConnection().prepareStatement(sql);
            preparedStatement.setString(1, name+"%");
        }else {
            String sql = "select * from node where label = ? and name like ? limit 50";
            preparedStatement = getConnection().prepareStatement(sql);
            preparedStatement.setString(1, NodeUtils.getNeo4jLable(type));
            preparedStatement.setString(2, name+"%");
        }
        System.out.println(preparedStatement.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        System.out.println("从Mysql中query结束");
        ArrayList<CacheEntity> cacheEntities = new ArrayList<>();
        while (resultSet.next()){
             String typeMysql = resultSet.getString(3);
             if (typeMysql == null){
                 typeMysql = "Resource";
             }
             String uri = resultSet.getString(4);
             if (uri == null){
                 uri = "";
             }
            cacheEntities.add(new CacheEntity(resultSet.getInt(1), typeMysql, resultSet.getString(2).replaceAll("_", " "), uri));
        }
        resultSet.close();
        preparedStatement.close();
        System.out.println("结束从Mysql中查[type:" + type + ",name:" + name + "]");
        if (!connection.isClosed())
            connection.close();
        return cacheEntities;
    }

    public static void main(String[] args) throws Exception{
        selectByTypeAndName(0, "Al");
    }
}
