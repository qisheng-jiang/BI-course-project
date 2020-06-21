//定义所需数据类型和图例颜色
var types = ['Resource', 'Book', 'Movie', 'Music']
var colors = ['#6ca46c', '#4e88af', '#ca635f', '#d2907c']
var requestURL = 'http://u18d6h18.xiaomy.net:53465'

var type2color = {
    'Resource': '#6ca46c',
    'Book': '#4e88af',
    'Movie': '#ca635f',
    'Music': '#d2907c'
}

var typeMap = {
    'Resource': 1,
    'Book': 2,
    'Movie': 3,
    'Music': 4
}

var graph = {
    nodes: [],
    links: []
}

var relatedId = -1;


/***************************弹出框事件******************************/

var searchOneVue = new Vue({
    el: '#searchOneWindow',
    data: {
        show: false,

        type: 0, //类型
        curInput: '', //当前用户的输入

        sendInterv: '',

        idFront: "srOne",
        resShow: false,


        searchRes: [

        ], //模糊匹配结果



        step: 2, //步长

        id: -1, //

        limit: 10,

        finalType: 1,

    },

    methods: {

        handleInputChange: function () {

            if (new Date() - this.lastInputTime > 2000) {

                var params = new URLSearchParams();
                params.append('type', this.type);
                params.append('name', this.curInput);

                axios
                    .post(requestURL + '/selectByTypeAndName', params)
                    .then(res => {
                        this.searchRes = res.data;
                    })

                this.lastInputTime = new Date();
                this.resShow = true;

            }


        },

        startFocus: function () {

            var p_this = this;
            this.sendInterv = setInterval(function () {

                var params = new URLSearchParams();
                params.append('type', p_this.type);
                params.append('name', p_this.curInput);

                if (p_this.curInput != '') {
                    axios
                        .post(requestURL + '/selectByTypeAndName', params)
                        .then(res => {
                            p_this.searchRes = res.data;
                        });
                }

                p_this.resShow = true;

            }, 4000);
        },

        stopFocus: function () {
            clearInterval(this.sendInterv);
        },

        handleSelectRes: function (event) {

            var index = parseInt(event.target.id.slice(5));

            this.curInput = this.searchRes[index]['name'];
            this.id = this.searchRes[index]['id'];
            this.finalType = typeMap[this.searchRes[index]['label']];

            this.resShow = false;

        },
        sendSearchOneReq: function () {

            if (this.id == -1) {
                alert("请先选择一个查询结果");
                return;
            }

            var params = new URLSearchParams();

            params.append('type', 0);
            params.append('step', this.step);
            params.append('id', this.id);
            params.append('limit', this.limit);

            axios.post(requestURL + '/searchANode', params)
                .then(res => {
                    console.log(res);

                    p_nodes = res.data.nodes;
                    p_relations = res.data.relations;

                    graph.nodes = [];
                    graph.links = [];

                    for (let i = 0; i < p_nodes.length; i++) {
                        var newNode = p_nodes[i]['properties'];
                        graph.nodes.push(newNode);
                    }

                    for (let i = 0; i < p_relations.length; i++) {
                        var newLink = p_relations[i]['properties'];
                        graph.links.push(newLink);
                    }

                    initSvg();

                    closeAll();

                })

        }

    }

});

var searchTwoVue = new Vue({
    el: '#searchTwoWindow',

    data: {
        type1: 0,
        type2: 0,

        curInput1: 'hello',
        curInput2: 'hi',

        idFront1: 'ssrOne',
        idFront2: 'ssrTwo',

        resOneShow: false,
        resTwoShow: false,

        searchResOne: [

        ],
        searchResTwo: [

        ],

        sendInterv1: '',
        sendInterv2: '',

        finalTypeOne: 0,
        finalTypeTwo: 0,

        id1: -1,
        id2: -1,

        step: 2,
        limit: 10
    },

    methods: {

        startFocusOne: function () {
            var p_this = this;
            this.sendInterv1 = setInterval(function () {

                var params = new URLSearchParams();
                params.append('type', p_this.type1);
                params.append('name', p_this.curInput1);

                if (p_this.curInput != '') {
                    axios
                        .post(requestURL + '/selectByTypeAndName', params)
                        .then(res => {
                            p_this.searchResOne = res.data;
                        });
                }

                p_this.resOneShow = true;

            }, 4000);
        },

        stopFocusOne: function () {
            clearInterval(this.sendInterv1);
        },
        startFocusTwo: function () {
            var p_this = this;
            this.sendInterv2 = setInterval(function () {

                var params = new URLSearchParams();
                params.append('type', p_this.type2);
                params.append('name', p_this.curInput2);

                if (p_this.curInput != '') {
                    axios
                        .post(requestURL + '/selectByTypeAndName', params)
                        .then(res => {
                            p_this.searchResTwo = res.data;
                        });
                }

                p_this.resTwoShow = true;

            }, 4000);

        },
        stopFocusTwo: function () {
            clearInterval(this.sendInterv2);
        },
        handleSelectResOne: function () {
            console.log(event.target.id)
            var index = parseInt(event.target.id.slice(6));

            this.curInput1 = this.searchResOne[index]['name'];
            this.id1 = this.searchResOne[index]['id'];
            this.finalType1 = typeMap[this.searchResOne[index]['label']];

            this.resOneShow = false;
        },
        handleSelectResTwo: function () {
            var index = parseInt(event.target.id.slice(6));

            this.curInput2 = this.searchResTwo[index]['name'];
            this.id2 = this.searchResTwo[index]['id'];
            this.finalType2 = typeMap[this.searchResTwo[index]['label']];

            this.resTwoShow = false;

        },

        sendMinPath: function () {
            if (this.id1 == -1 || this.id2 == -1) {
                alert("请先选择一个查询结果");
                return;
            }

            var params = new URLSearchParams();

            params.append('sourceType', 0);
            params.append('targetType', 0);
            params.append('source', this.id1);
            params.append('target', this.id2);

            axios.post(requestURL + '/searchMinPath', params)
                .then(res => {
                    console.log(res);

                    p_nodes = res.data.nodes;
                    p_relations = res.data.relations;

                    graph.nodes = [];
                    graph.links = [];

                    for (let i = 0; i < p_nodes.length; i++) {
                        var newNode = p_nodes[i]['properties'];
                        graph.nodes.push(newNode);
                    }

                    for (let i = 0; i < p_relations.length; i++) {
                        var newLink = p_relations[i]['properties'];
                        graph.links.push(newLink);
                    }
                    initSvg();
                    closeAll();
                })
        },

        sendTwoNodes: function () {

            if (this.id1 == -1 || this.id2 == -1) {
                alert("请先选择一个查询结果");
                return;
            }

            var params = new URLSearchParams();

            params.append('sourceType', 0);
            params.append('targetType', 0);
            params.append('sourceId', this.id1);
            params.append('targetId', this.id2);
            params.append('step', this.step);
            params.append('limit', this.limit);

            axios.post(requestURL + '/searchByTwoNodes', params)
                .then(res => {
                    console.log(res);

                    p_nodes = res.data.nodes;
                    p_relations = res.data.relations;

                    graph.nodes = [];
                    graph.links = [];

                    for (let i = 0; i < p_nodes.length; i++) {
                        var newNode = p_nodes[i]['properties'];
                        graph.nodes.push(newNode);
                    }

                    for (let i = 0; i < p_relations.length; i++) {
                        var newLink = p_relations[i]['properties'];
                        graph.links.push(newLink);
                    }
                    initSvg();
                    closeAll();
                })
        },

        sendAllPath: function () {
            if (this.id1 == -1 || this.id2 == -1) {
                alert("请先选择一个查询结果");
                return;
            }

            var params = new URLSearchParams();

            params.append('sourceType', 0);
            params.append('targetType', 0);
            params.append('source', this.id1);
            params.append('target', this.id2);

            axios.post(requestURL + '/searchAllMinPaths', params)
                .then(res => {
                    console.log(res);

                    p_nodes = res.data.nodes;
                    p_relations = res.data.relations;

                    graph.nodes = [];
                    graph.links = [];

                    for (let i = 0; i < p_nodes.length; i++) {
                        var newNode = p_nodes[i]['properties'];
                        graph.nodes.push(newNode);
                    }

                    for (let i = 0; i < p_relations.length; i++) {
                        var newLink = p_relations[i]['properties'];
                        graph.links.push(newLink);
                    }
                    initSvg();
                    closeAll();
                })
        }
    }

});

/***************************图例******************************/

function createIndicator() {

    for (var i = 0; i < types.length; i++) {
        $('#indicator').append("<div><span style='background-color:" + colors[i] + "'></span>" + types[i] + "</div>");
    }

}

/***************************窗体显示******************************/

function showWindow(window) {

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

function closeWindow(window) {

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

function showSearchOne() {
    showWindow(document.getElementById('searchOneWindow'));
}

function showSearchTwo() {
    showWindow(document.getElementById('searchTwoWindow'));
}

function showGraphWindow(){
    showWindow(document.getElementById('graphWindow'));
}

function closeAll() {
    closeWindow(document.getElementById("searchOneWindow"));
    closeWindow(document.getElementById("searchTwoWindow"));
    closeWindow(document.getElementById("graphWindow"));
}

/***************************创建力图******************************/

var simulation;

function initSvg() {

    $('svg').empty();

    var svg = d3.select("#svg");
    var width = svg.attr("width");
    var height = svg.attr("height");

    simulation = d3.forceSimulation()
        .force("link", d3.forceLink().id(function (d) {
            return d.id;
        }).distance(100))
        .force("charge", d3.forceManyBody())
        .force("center", d3.forceCenter(width / 2, height / 2));


    var link = svg.append("g").attr("class", "links").selectAll("line").data(graph.links)
        .enter().append("line").attr(
            'name',
            function (d) {
                return d.label;
            }
        ).attr("stroke-width", function (d) {
            //return Math.sqrt(d.value);
            return 1; //所有线宽度均为1
        }).attr(
            'id',
            function(d){
                return d.id;
            }
        ).style("stroke", function (d) {
            return 'grey';
        });

    var node = svg.append("g").attr("class", "nodes").selectAll("circle").data(graph.nodes)
        .enter().append("circle").attr(
            "id",
            function (d) {
                return d.id
            }
        ).attr("r", function (d) {
            if (d.id == searchOneVue.id || d.id == searchTwoVue.id1 || d.id == searchTwoVue.id2 || d.id == relatedId) return 20;
            else return 10;
        }).attr("fill", function (d) {
            return type2color[d.label];
        }).attr("stroke", "none").attr("name", function (d) {
            return d.name;
        }).call(d3.drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended)
        );

    node.append("title").text(function (d) {
        return d.name;
    });

    link.append("title").text(function (d) {
        return d.label;
    });


    //simulation中ticked数据初始化，并生成图形
    simulation
        .nodes(graph.nodes);

    simulation.force("link")
        .links(graph.links);

    var linkText = svg.append('g').selectAll("line")
        .data(graph.links)
        .enter()
        .append("text")
        .attr("font-family", "Arial, Helvetica, sans-serif")
        .attr("x", function (d) {
            if (d.target.x > d.source.x) {
                return (d.source.x + (d.target.x - d.source.x) / 2);
            } else {
                return (d.target.x + (d.source.x - d.target.x) / 2);
            }
        })
        .attr("y", function (d) {
            if (d.target.y > d.source.y) {
                return (d.source.y + (d.target.y - d.source.y) / 2);
            } else {
                return (d.target.y + (d.source.y - d.target.y) / 2);
            }
        })
        .attr("fill", "white")
        .style("font", "normal 12px Arial")
        .attr("dy", ".35em")
        .text(function (d) {
            return d.label;
        });

    simulation.on("tick", ticked);

    function ticked() {
        link
            .attr("x1", function (d) {
                return d.source.x;
            })
            .attr("y1", function (d) {
                return d.source.y;
            })
            .attr("x2", function (d) {
                return d.target.x;
            })
            .attr("y2", function (d) {
                return d.target.y;
            });

        node
            .attr("cx", function (d) {
                return d.x;
            })
            .attr("cy", function (d) {
                return d.y;
            });
        linkText
            .attr("x", function (d) {
                if (d.target.x > d.source.x) {
                    return (d.source.x + (d.target.x - d.source.x) / 2);
                } else {
                    return (d.target.x + (d.source.x - d.target.x) / 2);
                }
            })
            .attr("y", function (d) {
                if (d.target.y > d.source.y) {
                    return (d.source.y + (d.target.y - d.source.y) / 2);
                } else {
                    return (d.target.y + (d.source.y - d.target.y) / 2);
                }

            })

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

function handleMouseEnter(event) {

    if (!dragging) {

        var name = $(this).attr('name');
        var id = $(this).attr('id');
        var sel_node;

        $('#info h4').css('color', $(this).attr('fill')).text(name);
        $('#info p').remove();

        for (var i = 0; i < graph.nodes.length; i++) {
            var node = graph.nodes[i];
            if (node['id'] == id) {
                sel_node = node;
                break;
            }
        }

        for (var key in sel_node) {

            if (key == 'x' || key == 'y' || key == 'vx' || key == 'vy' || key == 'index' || key == 'fx' || key == 'fy') continue;
            if (key == 'uri') {
                $('#info').append('<p><span>' + key + '</span>' + '<a href = \'' + sel_node[key] + '\'>' + sel_node[key] + '<a>' + '</p>');
                continue;
            }
            $('#info').append('<p><span>' + key + '</span>' + sel_node[key] + '</p>');
        }
    }
}

function handleMouseClick(event) {

    var id = $(this).attr('id');

    relatedId = id;

    var params = new URLSearchParams();

    params.append('type', 0);
    params.append('step', 2);
    params.append('id', id);
    params.append('limit', 10);

    axios.post(requestURL + '/searchANode', params)
        .then(res => {
            

            p_nodes = res.data.nodes;
            p_relations = res.data.relations;

            new_nodes = [];
            new_links = [];

            for(let i =0;i < graph.nodes.length;i++){
                new_nodes.push({
                    'name': graph.nodes[i].name,
                    'id': graph.nodes[i].id,
                    'label': graph.nodes[i].label,
                    'uri': graph.nodes[i].uri
                }
                );
            }

            

            for(let i =0;i < graph.links.length;i++){
                console.log(graph.links[i].source);
                new_links.push( {
                    'id': graph.links[i].id,
                    'source': graph.links[i].source.id,
                    'label': graph.links[i].label,
                    'target': graph.links[i].target.id
                }
                );
            }

            for (let i = 0; i < p_nodes.length; i++) {

                var newNode = p_nodes[i]['properties'];
                /*判断存在*/
                var judge = false;
                for (let j = 0; j < new_nodes.length; j++) {
                    
                    if(newNode.id == new_nodes[j].id){
                        judge = true;
                        break;
                    }
                }

                if(!judge)new_nodes.push(newNode);
            }

            graph.nodes = new_nodes;

            for (let i = 0; i < p_relations.length; i++) {
                var newLink = p_relations[i]['properties'];

                /*判断存在*/
                var judge = false;
                for (let j = 0; j < new_links.length; j++) {
                    
                    if(newLink.id == new_links[j].id){
                        judge = true;
                        break;
                    }
                }
                if(!judge)new_links.push(newLink);
            }
            graph.links = new_links;

            console.log(new_links);
            console.log(new_nodes);
            
            initSvg();
            closeAll();

        });

}








/***************************页面初始化******************************/


window.onload = function () {

    createIndicator();

    initSvg();

    document.getElementById('showSearchOne').addEventListener('click', showSearchOne);
    document.getElementById('showSearchTwo').addEventListener('click', showSearchTwo);
    document.getElementById('showgraph').addEventListener('click', showGraphWindow);
    document.getElementById('shadow').addEventListener('click', closeAll);



    $('#svg').on('mouseenter', '.nodes circle', handleMouseEnter);
    $('#svg').on('click', '.nodes circle', handleMouseClick);

}