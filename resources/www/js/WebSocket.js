// 연결(onopen), 종료(onclose), 발신(send), 수신(onmessage), 로그

var wsUrl =  "ws://localhost:8080"; // webSocket Address

var output;

init();

function init()
{
    console.log("in function");
    //output = document.getElementById("output");
    testWebSocket();
}

function testWebSocket()
{
    console.log("in function");
    webSocket = new WebSocket(wsUrl);
    webSocket.onopen = function(evt)
    {
        onOpen(evt);
        
        //왜 안 될까....?
    };
    
    webSocket.onclose = function(evt)
    {
        onClose(evt);
    };

    webSocket.onmessage = function(evt)
    {
        onmessage(evt);
    };

    webSocket.onerror = function(evt)
    {
        onError(evt);
    };

    function onOpen(evt)
    {
        console.log("connect sucessful!");
        doSend("testMessage");
    }

    function onClose(evt)
    {
        webSocket.send("final send Message");
        console.log("연결해제");
    }


    function doSend(message)
    {
        console.log("발신: " + message);
        webSocket.send(message);    //testMessage To Server
    };

   
    window.addEventListener("load", init, false);

}