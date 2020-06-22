package com.tongji.bi.service;

import com.tongji.bi.entity.NodeEntity;
import com.tongji.bi.entity.RelationEntity;
import com.tongji.bi.util.ConstantDefinition;
import com.tongji.bi.util.Neo4jDriverInitialize;
import com.tongji.bi.util.NodeUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GraphService {

    private Neo4jDriverInitialize neo4jDriverInitialize = new Neo4jDriverInitialize();
    private Session session;

    private Session getSession(){
        if (session == null || !session.isOpen()){
            session = neo4jDriverInitialize.getSession();
        }
        return session;
    }

    /**
     * 通过一个实体查询其关联的所有关系和实体(限定跳数和结果数量)
     * @param type 输入的实体种类
     * @param step 限定的跳数
     * @param id 实体ID
     * @param limit 限定结果数量
     * @return 查询的节点和关系集合
     */
    public HashMap<String, ArrayList<NodeEntity>> searchByTypeAndId(int type, int step, int limit, int id){
        // 获取节点种类及其已定的查询字段
//        Pair<String, String> nodeType = NodeUtils.getTypeFromKey(type);
        // 查询语句(使用parameters拼接字段会执行失败，原因待查)
        String query = "MATCH p=((n:"+ NodeUtils.getNeo4jLable(type)+")-[*"+step+"]-()) where id(n)="+id+" return p limit " + limit;
        return query(query);
    }

    /**
     * 通过一条语句查询并返回结果集（要求用p表示path，且结果只返回p）
     * @param query 语句
     * @return 结果集
     */
    public HashMap<String, ArrayList<NodeEntity>> query(String query){
        // 获取结果
        Result result = getSession().run(query);
        System.out.println(query);
        HashMap<String,ArrayList<NodeEntity>> hashMap = new HashMap<>();
        // 节点集合
        ArrayList<NodeEntity> nodeList = new ArrayList<>();
        // 关系集合
        ArrayList<NodeEntity> relationList = new ArrayList<>();
        while (result.hasNext()){
            Record record = result.next();
            // 一个path相当于一条结果，这“一条结果”就是在查询语句中定义的'p'对应的表达式
            Path path = record.get("p").asPath();
            // 遍历节点添加
            Iterable<Node> nodes = path.nodes();
            for (Node node: nodes){
                NodeEntity nodeEntity = nodeToEntity(node);
                if (!nodeList.contains(nodeEntity))
                    nodeList.add(nodeEntity);
            }
            // 遍历关系添加
            Iterable<Relationship> relationships = path.relationships();
            for (Relationship relationship : relationships){
                RelationEntity relationEntity = relationToEntity(relationship);
                if (!relationList.contains(relationEntity))
                    relationList.add(relationEntity);
            }
        }
        hashMap.put(ConstantDefinition.NODES, nodeList);
        hashMap.put(ConstantDefinition.RELATIONS, relationList);
        if (session.isOpen())
            session.close();
//        System.err.println(hashMap);
        return hashMap;
    }


    public HashMap<String, ArrayList<NodeEntity>> searchMinPath(int source, int target, int sourceType, int targetType){
        String query = "MATCH (source:"+NodeUtils.getNeo4jLable(sourceType)+"),(target:"+NodeUtils.getNeo4jLable(targetType)+") WHERE id(source) = "
                + source +" AND id(target) = " + target +
                " MATCH p = shortestPath((source)-[*]-(target)) return p";
        return query(query);
    }

    public HashMap<String, ArrayList<NodeEntity>> searchAllMinPaths(int source, int target, int sourceType, int targetType){
        String query = "MATCH (source:"+NodeUtils.getNeo4jLable(sourceType)+"),(target:"+NodeUtils.getNeo4jLable(targetType)+") WHERE id(source) = "
                + source +" AND id(target) = " + target +
                " MATCH p = allShortestPaths((source)-[*]-(target)) return p";
        return query(query);
    }



    /**
     * 将数据库中Node节点转换为NodeEntity实体类型
     * @param node Node对象
     * @return NodeEntity对象
     */
    private NodeEntity nodeToEntity(Node node){
        NodeEntity nodeEntity = null;
        if (node != null){
            nodeEntity = new NodeEntity();
            nodeEntity.put("id", node.id());
            // 过滤节点标签
            nodeEntity.put("label", labelFilter(node.labels()));
//            nodeEntity.putAll(node.asMap());
            Map map = node.asMap();
            String uri = "";
            String name = "";
            if (map.containsKey("uri")){
                uri = (String)map.get("uri");
                String[] uris = uri.split("/");
                name = uris[uris.length-1].replaceAll("_", " ");
            }
            nodeEntity.put("name", name);
            nodeEntity.put("uri", uri);
            return nodeEntity;
        }else {
            return null;
        }
    }

    /**
     * 将数据库中Relationship转换为RelationEntity实体类型
     * @param relationship Relationship对象
     * @return RelationEntity对象
     */
    private RelationEntity relationToEntity(Relationship relationship){
        RelationEntity relationEntity = null;
        if (relationship != null){
            relationEntity = new RelationEntity();
//            relationEntity.put("id", relationship.id());
            relationEntity.put("id", new Random().nextInt());
            relationEntity.put("source", relationship.startNodeId());
            relationEntity.put("target", relationship.endNodeId());
            String type = relationship.type();
            String[] types = type.split("__");
            relationEntity.put("label", types[types.length-1]);
//            relationEntity.putAll(relationship.asMap());
            return relationEntity;
        }else {
            return null;
        }
    }


    /**
     * 由于存在节点标签不唯一的情况，我们对节点标签进行了过滤，使其始终显示唯一一个标签；过滤遵循一个优先级规则：
     * 优先级顺序为Resource  < 其他。
     * 即优先选择其他标签，当不存在其他标签时，考虑Resource
     * @param ilabels 标签集合
     * @return 集合过滤后所剩的唯一标签
     */
    public String labelFilter(Iterable<String> ilabels){
        ArrayList<String> labels = new ArrayList<>();
        for (String label : ilabels){
            labels.add(label);
        }
        if (labels.size() == 1)
            return labels.get(0);
        else if (labels.size() == 2)
            return labels.get(0).equals("Resource") ? labels.get(1) : labels.get(0);
        else {
//            for (String label : labels){
//                if (!label.equals("Resource") && !label.equals("ns3__BusinessClassification"))
//                    return label;
//            }
            return "";
        }
    }

    /**
     * 通过两个实体查询两个实体间可能存在的所有多跳关系(限定跳数和结果数量)
     * @param sourceType 起始节点类型
     * @param targetType 目标节点类型
     * @param step 跳数
     * @param limit 结果数量限制
     * @param sourceId 起始节点id
     * @param targetId 目标节点id
     * @return 结果集
     */
    public HashMap<String, ArrayList<NodeEntity>> searchByTwoNodes(int sourceType, int targetType, int step, int limit, int sourceId, int targetId){
        StringBuilder query = new StringBuilder();
        String hear = " MATCH p=(source:"+NodeUtils.getNeo4jLable(sourceType)+")";
        String tail = "-[]-(target:"+NodeUtils.getNeo4jLable(targetType)+") WHERE id(source) = " + sourceId +" AND id(target) = " + targetId + " return p limit "+limit;
        for (int i = 0; i < step; i++) {
            query.append(hear);
            for (int j = 0; j < i; j++) {
                query.append("-[]-()");
            }
            query.append(tail);
            if (i != step-1){
                query.append(" UNION ");
            }
        }
        System.out.println(query);
//        return null;
        return query(query.toString());
    }

    public void importTtl(String typePath) {
        //初始化驱动器
        try (Session session = this.getSession()) {
            try (Transaction tx = session.beginTransaction()) {
                String ontology="CALL semantics.importRDF(\"file://" + typePath + "\", \"Turtle\",{})";
                Result result2 = tx.run(ontology);
                System.out.println(result2.toString());
                tx.commit();
            }
        }
    }

    public static void main(String[] args){
        GraphService gs = new GraphService();
        for (int i = 1; i < 5; i++) {
            gs.searchByTwoNodes(1,1,i,10,2222, 33333);
        }
    }
}
