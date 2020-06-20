//定义所需数据类型和图例颜色
var types = ['Resource','Book','Movie','Music']
var colors = ['#6ca46c', '#4e88af', '#ca635f', '#d2907c']

var type2color = {
    'Resource':'#6ca46c',
    'Book':'#4e88af',
    'Movie':'#ca635f',
    'Music' : '#d2907c'
}

var graph = {
    nodes:[{
        
            "name": "Perichoresis",
            "id": 100,
            "label": "Resource",
            "uri": "http://dbpedia.org/resource/Perichoresis"
        
    },
    {
        
            "name": "Filioque",
            "id": 3942,
            "label": "Resource",
            "uri": "http://dbpedia.org/resource/Filioque"
        
    },
    {
        
            "name": "History of the Orthodox Church",
            "id": 211162,
            "label": "Resource",
            "uri": "http://dbpedia.org/resource/History_of_the_Orthodox_Church"
        
    },
    {
        
            "name": "East–West Schism",
            "id": 11,
            "label": "Resource",
            "uri": "http://dbpedia.org/resource/East–West_Schism"
        
    },
    {
        
            "name": "Western Christianity",
            "id": 8347,
            "label": "Resource",
            "uri": "http://dbpedia.org/resource/Western_Christianity"
        
    },
    {
        
            "name": "Cappadocian Fathers",
            "id": 12317,
            "label": "Resource",
            "uri": "http://dbpedia.org/resource/Cappadocian_Fathers"
        
    }],
    links:[
        {
            
                "id": 437438063,
                "source": 3942,
                "label": "seeAlso",
                "target": 100
            
        },
        {
            
                "id": -140463653,
                "source": 211162,
                "label": "seeAlso",
                "target": 3942
            
        },
        {
            
                "id": -2052433072,
                "source": 11,
                "label": "seeAlso",
                "target": 3942
            
        },
        {
            
                "id": -1917746947,
                "source": 8347,
                "label": "seeAlso",
                "target": 3942
            
        },
        {
            
                "id": -1568485539,
                "source": 3942,
                "label": "seeAlso",
                "target": 12317
            
        }
    ]
}


curInput1 = {
    'id':-1,
    'type':-1,
}

curInput2 = {
    'id':-1,
    'type':-1,
}

/***************************图例******************************/

function createIndicator(){

    for (var i=0; i < types.length; i++) {
        $('#indicator').append("<div><span style='background-color:" + colors[i] + "'></span>" + types[i] + "</div>");
    }

}

/***************************窗体显示******************************/

function showWindow(window){

    var shadow = document.getElementById("shadow");

    window.style.display = "block";
    window.style.opacity = 0;
    window.style.top = "30px";
    setTimeout(function () {
    window.style.opacity = 1;
    window.style.top = "0px";
    }, 100);


    shadow.style.display = "block";
    shadow.style.opacity = 0;
    setTimeout(function () {
    shadow.style.opacity = 0.4;
    }, 100);
}

function closeWindow(window){

    window.style.opacity = 0;
   setTimeout(function () {
    window.style.display = 'none';
   }, 100);

   var shadow = document.getElementById("shadow");
   shadow.style.opacity = 0;
   setTimeout(function () {
      shadow.style.display = "none";
   }, 100);

}

function showSearchOne(){
    showWindow(document.getElementById('searchOneWindow'));
}

function showSearchTwo(){
    showWindow(document.getElementById('searchTwoWindow'));
}

function closeAll(){
   closeWindow(document.getElementById("searchOneWindow"));
   closeWindow(document.getElementById("searchTwoWindow"));
}

/***************************创建力图******************************/

var simulation;

function initSvg(){

    $('svg').empty();

    var svg = d3.select("#svg");
    var width = svg.attr("width");
    var height = svg.attr("height");

    simulation = d3.forceSimulation()
	        .force("link", d3.forceLink().id(function(d) {
	            return d.id;
	        }).distance(100))
	        .force("charge", d3.forceManyBody())
            .force("center", d3.forceCenter(width / 2, height / 2));
            

    var link = svg.append("g").attr("class","links").selectAll("line").data(graph.links)
    .enter().append("line").attr(
        'name',function(d){
            return d.label;
        }
    ).attr("stroke-width", function(d) {
        //return Math.sqrt(d.value);
        return 1; //所有线宽度均为1
    });

    var node = svg.append("g").attr("class", "nodes").selectAll("circle").data(graph.nodes)
	    	.enter().append("circle").attr(
                "id",function(d){
                    return d.id
                }
            ).attr("r", function(d) {
	    		return 10;
	    	}).attr("fill", function(d) {
	    		return type2color[d.label];
	    	}).attr("stroke", "none").attr("name", function(d) {
	    		return d.name;
	    	}).call(d3.drag()
	    		.on("start", dragstarted)
	    		.on("drag", dragged)
	    		.on("end", dragended)
            );

    node.append("title").text(function(d) {
	    		return d.name;
            });

    link.append("title").text(function(d) {
        return d.label;
    });

            
    //simulation中ticked数据初始化，并生成图形
    simulation
    .nodes(graph.nodes)
    .on("tick", ticked);

    simulation.force("link")
    .links(graph.links);

    function ticked() {
        link
            .attr("x1", function(d) {
                return d.source.x;
            })
            .attr("y1", function(d) {
                return d.source.y;
            })
            .attr("x2", function(d) {
                return d.target.x;
            })
            .attr("y2", function(d) {
                return d.target.y;
            });
    
        node
            .attr("cx", function(d) {
                return d.x;
            })
            .attr("cy", function(d) {
                return d.y;
            });        
    }


}

/***************************力图事件******************************/


 //该变量保证拖动鼠标时，不会影响图形变换，默认为false未选中鼠标
 var dragging = false;

 //开始拖动并更新相应的点
 function dragstarted(d) {
     if (!d3.event.active) simulation.alphaTarget(0.3).restart();
     d.fx = d.x;
     d.fy = d.y;
     dragging = true;
 }

 //拖动进行中
 function dragged(d) {
     d.fx = d3.event.x;
     d.fy = d3.event.y;
 }

 //拖动结束
 function dragended(d) {
     if (!d3.event.active) simulation.alphaTarget(0);
     d.fx = null;
     d.fy = null;
     dragging = false;
 }

function handleMouseEnter(event){

    if(!dragging){

        var name = $(this).attr('name');
        var id = $(this).attr('id');
        var sel_node;

        $('#info h4').css('color', $(this).attr('fill')).text(name);
        $('#info p').remove();

        for(var i = 0;i < graph.nodes.length;i++){
            var node = graph.nodes[i];
            if(node['id'] == id){
                sel_node = node;
                break;
            }
        }

        for(var key in sel_node){

            if(key == 'x' || key == 'y' || key == 'vx' ||key == 'vy' ||key == 'index' || key == 'fx' || key == 'fy')continue;
            if(key == 'uri'){
            $('#info').append('<p><span>' + key + '</span>' + '<a href = \''+sel_node[key]+'\'>' +sel_node[key] + '<a>' + '</p>');
            continue;
            }
            $('#info').append('<p><span>' + key + '</span>' + sel_node[key] + '</p>');
        }
    }
}

/***************************弹出框事件******************************/

var searchOneVue = new Vue({
    el:'#searchOneWindow',
    data:{
        show:false,


        type:1, //类型
        curInput:'', //当前用户的输入

        idFront:"srOne",
        resShow:false,

        searchRes:[{
            "id": 1,
            "label": "Resource",
            "name": "Anaïs Nin",
            "uri": "http://dbpedia.org/resource/Anaïs_Nin"
        },
        {
            "id": 2,
            "label": "Resource",
            "name": "Andersonville, Georgia",
            "uri": "http://dbpedia.org/resource/Andersonville,_Georgia"
        }
    ], //模糊匹配结果

        

        step:2, //步长

        id:-1, //

        limit:10

        },

    methods:{

        handleInputChange:function(){
            this.resShow = true;
        },

        sendSearchOneReq:function(){

        },
        handleSelectRes:function(event){
            var index = parseInt(event.target.id.slice(5));
            this.curInput = this.searchRes[index]['name'];
            this.resShow = false;
        }

    }
    
});






/***************************页面初始化******************************/


window.onload = function(){

    createIndicator();

    initSvg();

    document.getElementById('showSearchOne').addEventListener('click',showSearchOne);
    document.getElementById('showSearchTwo').addEventListener('click',showSearchTwo);
    document.getElementById('shadow').addEventListener('click',closeAll);

   

    $('#svg').on('mouseenter','.nodes circle',handleMouseEnter);
    
}