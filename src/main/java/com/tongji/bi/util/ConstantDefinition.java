package com.tongji.bi.util;

public class ConstantDefinition {
    // 连接字符串

//    public final static String neo4jUrl = "bolt://192.168.1.212:7687";
    public final static String neo4jUrl = "bolt://172.20.10.2:7687";

    public static final String username = "neo4j";
    public static final String password = "1234";

    public static String path2SystemJson = "/home/upload/system/";

    static final String mongoDatabase = "neo4j";
    static final String mongoIp = "47.103.27.88";
    static final int mongoPort = 27017;
    static final String mongoSingleCollection = "Single";
    static final String mongoDoubleCollection = "Double";
    static final String mongoMinPathCollection = "Minpath";
    static final String mongoAllMinPathCollection = "Minpaths";
    public static final int mongoSingleCollectionType = 1;
    public static final int mongoDoubleCollectionType = 2;
    public static final int mongoMinPathCollectionType = 3;
    public static final int mongoAllMinPathsCollectionType = 4;

//    static final String mysqlUrl = "jdbc:mysql://192.168.1.212:3306/neo4j?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";

    static final String mysqlUrl = "jdbc:mysql://172.20.10.2:3306/neo4j?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";

    static final String mysqlUser = "root";
    static final String mysqlPassword = "123456";
    static final String mysqlDriver = "com.mysql.cj.jdbc.Driver";

    public static final String NODES = "nodes";
    public static final String RELATIONS = "relations";

    public static final int MaxLimit = 5000;
    public static final int MinLimit = 10;
    public static final int MaxStep = 15;
    public static final int MinStep = 2;

}
