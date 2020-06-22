package com.tongji.bi.util;

import java.util.HashMap;

public class NodeUtils {

    private static HashMap<Integer, String> lables = null;

    private static void initLabel() {
        lables.put(0, "Resource");
        lables.put(1, "Book");
        lables.put(2, "Movie");
        lables.put(3, "Music");
    }

    private static HashMap<Integer, String> getLables() {
        if (lables == null){
            lables = new HashMap<>();
            initLabel();
        }
        return lables;
    }

    public static String getNeo4jLable(int key){
        return getLables().get(key);
    }
}
