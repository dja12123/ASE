// ì—°ê²°(onopen), ì¢…ë£Œ(onclose), ë°œì‹ (send), ìˆ˜ì‹ (onmessage), ë¡œê·¸

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
        console.log("소켓이 닫힙니다.");
    }


    function doSend(message)
    {
        console.log("ë°œì‹ : " + message);
        webSocket.send(message);    //testMessage To Server
    };

   
    window.addEventListener("load", init, false);

}
