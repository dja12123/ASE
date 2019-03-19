// 연결(onopen), 종료(onclose), 발신(send), 수신(onmessage), 로그

var wsUrl =  "ws://localhost:8080"; // webSocket Address

var output;

function init()
{
    output = document.getElementById("output");
    testWebSocket();
}

function testWebSocket()
{
    webSocket = new WebSocket(wsUrl);
    webSocket.onopen = fucntion(evt)
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
        writeToScreen("connect sucessful!");
        doSend("testMessage");
    }

    function onClose(evt)
    {
        webSocket.send("final send Message");
        writeToScreen("연결해제");
    }

    function onMessage(evt)
    {
        writeToScreen('<span style="color : red">error:</span>'+evt.data);
    }

    function doSend(message)
    {
        wruteToScreen("발신: " + message);
        webSocket.send(message);    //testMessage To Server
    };

    function writeToScreen(message)
    {
        var pre = document.createElement("p");
        pre.style.wordwrap = "break-word";
        pre.innerHTML = message;
        output.appendChild(pre);
    }

    window.addEventListener("load", init, false);

}