package com.tongji.bi.controller;

import com.alibaba.fastjson.JSONObject;
import com.tongji.bi.entity.CacheEntity;
import com.tongji.bi.entity.NodeEntity;
import com.tongji.bi.service.GraphService;
import com.tongji.bi.util.ConstantDefinition;
import com.tongji.bi.util.MongoDriverInitialize;
import com.tongji.bi.util.MysqlDriverInitialize;
import com.tongji.bi.util.ParamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.*;

import static com.tongji.bi.util.ConstantDefinition.path2SystemJson;

@RestController
@CrossOrigin(maxAge = 3600, origins = "*")
public class IndexController {

    @RequestMapping(value = "index")
    @CrossOrigin(maxAge = 3600, origins = "*")
    public String index(){
        return "index";
    }

    /**
     * 通过mysql模糊匹配获取实体列表返回前端，供用户选择
     * @param type 实体种类
     * @param name 名称匹配
     * @return 查到的实体列表
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @RequestMapping("selectByTypeAndName")
    @ResponseBody
    @CrossOrigin(maxAge = 3600, origins = "*")
    public ArrayList<CacheEntity> selectByTypeAndName(@RequestParam("type") int type, @RequestParam("name") String name) throws SQLException, ClassNotFoundException {
        return MysqlDriverInitialize.selectByTypeAndName(type, name);
    }

    /**
     * 通过一个实体查询其关联的所有关系和实体(限定跳数和结果数量)
     * @param type 输入的实体种类
     * @param step 限定的跳数
     * @param id 实体ID
     * @param limit 限定结果数量
     * @return 结果集
     */
    @RequestMapping(value = "searchANode", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin(maxAge = 3600, origins = "*")
    public String searchANode(
            @RequestParam("type") int type, @RequestParam("step") int step, @RequestParam("id") int id, @RequestParam("limit") int limit){
        step = ParamUtils.checkStep(step);
        limit = ParamUtils.checkLimit(limit);
        String param = ParamUtils.paramsToString(type, step, id, limit);
        String results = MongoDriverInitialize.findOne(param, ConstantDefinition.mongoSingleCollectionType);
        if (results == null){
            HashMap<String, ArrayList<NodeEntity>> hashMap = new GraphService().searchByTypeAndId(type, step, limit, id);
            MongoDriverInitialize.addOne(param, hashMap, ConstantDefinition.mongoSingleCollectionType);
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(hashMap);
            results = jsonObject.toJSONString();
        }
        return results;
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
    @RequestMapping(value = "searchByTwoNodes", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin(maxAge = 3600, origins = "*")
    public String searchByTwoNodes(
            @RequestParam("sourceType") int sourceType, @RequestParam("targetType") int targetType, @RequestParam("step") int step,
            @RequestParam("limit") int limit, @RequestParam("sourceId") int sourceId, @RequestParam("targetId") int targetId){
        step = ParamUtils.checkStep(step);
        limit = ParamUtils.checkLimit(limit);
        String param = ParamUtils.paramsToString(sourceType, targetType, step, limit, sourceId, targetId);
        String results = MongoDriverInitialize.findOne(param, ConstantDefinition.mongoDoubleCollectionType);
        if (results == null){
            HashMap<String, ArrayList<NodeEntity>> hashMap = new GraphService().searchByTwoNodes(sourceType, targetType, step, limit, sourceId, targetId);
            if (hashMap != null){
                MongoDriverInitialize.addOne(param, hashMap, ConstantDefinition.mongoDoubleCollectionType);
                JSONObject jsonObject = new JSONObject();
                jsonObject.putAll(hashMap);
                results = jsonObject.toJSONString();
            }
        }
        return results;
    }

    /**
     * 通过给定两个实体，返回两节点间最小路径
     * @param source 起始点
     * @param target 目标点
     * @param sourceType 起始节点种类
     * @param targetType 目标节点种类
     * @return 返回一条路径
     */
    @RequestMapping(value = "searchMinPath", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin(maxAge = 3600, origins = "*")
    public String searchMinPath(@RequestParam("source") int source, @RequestParam("target") int target,
                                                                @RequestParam("sourceType") int sourceType, @RequestParam("targetType") int targetType){
        String param = ParamUtils.paramsToString(source, target, sourceType, targetType);
        String results = MongoDriverInitialize.findOne(param,ConstantDefinition.mongoMinPathCollectionType);
        if (results == null){
            HashMap<String, ArrayList<NodeEntity>> hashMap = new GraphService().searchMinPath(source, target, sourceType, targetType);
            MongoDriverInitialize.addOne(param, hashMap, ConstantDefinition.mongoMinPathCollectionType);
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(hashMap);
            results = jsonObject.toJSONString();
        }
        return results;
    }

    @RequestMapping(value = "searchAllMinPaths", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin(maxAge = 3600, origins = "*")
    public String searchAllMinPaths(@RequestParam("source") int source, @RequestParam("target") int target,
                                    @RequestParam("sourceType") int sourceType, @RequestParam("targetType") int targetType) {
        String param = ParamUtils.paramsToString(source, target, sourceType, targetType);
        String results = MongoDriverInitialize.findOne(param,ConstantDefinition.mongoAllMinPathsCollectionType);
        if (results == null){
            HashMap<String, ArrayList<NodeEntity>> hashMap = new GraphService().searchAllMinPaths(source, target, sourceType, targetType);
            MongoDriverInitialize.addOne(param, hashMap, ConstantDefinition.mongoAllMinPathsCollectionType);
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(hashMap);
            results = jsonObject.toJSONString();
        }
        return results;
    }

    //上传系统的system文件
    @RequestMapping(value = "/uploadSystemFile",method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin(maxAge = 3600, origins = "*")
    public Map<String, Object> postSystem(HttpServletRequest request,
                                          @RequestParam("name") String name){
//        String savePath = FileController.class.getResource("/").getPath().replace("classes","upload/system");
        String savePath = path2SystemJson;
        Map<String, Object> res = new HashMap<>();
        try{
            if (springUpload(request, savePath, name)) {
                res.put("succees",1);
            }
        }catch (Exception e) {
            e.printStackTrace();
            res.put("succees", 0);
            res.put("Reason",e.toString());
        }
        new GraphService().importTtl(path2SystemJson+name+".ttl");
        return res;
    }

    private boolean springUpload(HttpServletRequest request, String savePath, String fileName) throws IllegalStateException, IOException
    {
        //将当前上下文初始化给  CommonsMutipartResolver （多部分解析器）
        CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(
                request.getSession().getServletContext());
        //检查form中是否有enctype="multipart/form-data"
        if(multipartResolver.isMultipart(request))
        {
            //将request变成多部分request
            MultipartHttpServletRequest multiRequest=(MultipartHttpServletRequest)request;
            //获取multiRequest 中所有的文件名
            Iterator iter=multiRequest.getFileNames();

            while(iter.hasNext())
            {

                //一次遍历所有文件
                MultipartFile file=multiRequest.getFile(iter.next().toString());
                if(file!=null)
                {
                    String oldName = file.getOriginalFilename();
                    String path = savePath + fileName + oldName.substring(oldName.lastIndexOf("."));
                    System.out.println(path);
                    File folder = new File(savePath);
                    //文件夹路径不存在
                    if (!folder.exists() && !folder.isDirectory()) {
                        folder.mkdirs();
                    }
                    File newFile = new File(path);
                    //判断路径是否存在，如果不存在就创建一个
                    if(!newFile.exists()){
                        newFile.mkdir();
                    }
                    //上传
                    file.transferTo(newFile);

                }

            }

        }
        return true;
    }

    /**
     * query一条语句，测试用
     * @param query 查询语句
     * @return 结果集
     */
    @RequestMapping(value = "query", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin(maxAge = 3600, origins = "*")
    public HashMap<String, ArrayList<NodeEntity>> query(@RequestParam("query") String query){
        return new GraphService().query(query);
    }


    /**
     * 获得某类型电影预测某年数量
     */
    @RequestMapping(value = "predictMovieGenreAmount", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin(maxAge = 3600, origins = "*")
    public String predictMovieGenreAmount(@RequestParam("year") String year,
                                          @RequestParam("genre") String genre) throws IOException, InterruptedException {
        String exe = "python";
        String command = "python/genrePredict.py";

        String[] cmdArr = new String[] {exe, command, genre, year};
        Process process = Runtime.getRuntime().exec(cmdArr);
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
//        while ((line = in.readLine()) != null) {
//            System.out.println(line);
//        }
        line = in.readLine();
        in.close();
        process.waitFor();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("amount", line);
//        System.out.println(hashMap);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(hashMap);
//        System.out.println(jsonObject);

        return jsonObject.toJSONString();
    }

    /**
     * 为某用户uid推荐n个电影
     * @return
     */
    @RequestMapping(value = "get_similar_users_recommendations", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin(maxAge = 3600, origins = "*")
    public String get_similar_users_recommendations(@RequestParam("uid") String uid,
                                                    @RequestParam("n") String n) throws IOException, InterruptedException {
        String exe = "python";
        String command = "python/movie_recommend.py";

        String[] cmdArr = new String[] {exe, command, uid, n};
        Process process = Runtime.getRuntime().exec(cmdArr);
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        List<String> resultList = new ArrayList();

        while ((line = in.readLine()) != null) {
//            System.out.println(line);
            if (line.contains("Estimating biases")) continue;
            if (line.contains("Computing the msd")) continue;
            resultList.add(line);
        }
        in.close();
        process.waitFor();

        HashMap<String, List<String>> hashMap = new HashMap<>();
        hashMap.put("list", resultList);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(hashMap);
        return jsonObject.toJSONString();
    }

    /**
     * 某电影iid的相似n个电影
     * @return
     */
    @RequestMapping(value = "get_similar_items", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin(maxAge = 3600, origins = "*")
    public String get_similar_items(@RequestParam("iid") String iid,
                                    @RequestParam("n") String n) throws IOException, InterruptedException {
        String exe = "python";
        String command = "python/movie_recommend2.py";

        String[] cmdArr = new String[] {exe, command, iid, n};
        Process process = Runtime.getRuntime().exec(cmdArr);
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        List<String> resultList = new ArrayList();

        while ((line = in.readLine()) != null) {
//            System.out.println(line);
            if (line.contains("Estimating biases")) continue;
            if (line.contains("Computing the msd")) continue;
            if (line.contains("Done computing similarity")) continue;
            resultList.add(line);
        }
        in.close();
        process.waitFor();

        HashMap<String, List<String>> hashMap = new HashMap<>();
        hashMap.put("list", resultList);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(hashMap);
        return jsonObject.toJSONString();
    }

//    /**
//     * 两个电影的相似度（图数据）
//     */
//    @RequestMapping(value = "get_similarity", method = RequestMethod.POST)
//    @ResponseBody
//    @CrossOrigin(maxAge = 3600, origins = "*")
//    public String get_similarity(@RequestParam("name1") String name1,
//                                  @RequestParam("name2") String name2) throws IOException, InterruptedException {
//        String exe = "python";
//        String command = "python/movie_sim.py";
//
//        String[] cmdArr = new String[] {exe, command, name1, name2};
//        Process process = Runtime.getRuntime().exec(cmdArr);
//        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line = null;
//        while ((line = in.readLine()) != null) {
//            System.out.println(line);
//        }
//        in.close();
//        process.waitFor();
//        return line;
//    }

}
