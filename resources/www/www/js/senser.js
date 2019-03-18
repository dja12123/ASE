//날짜 출력형식
function dateForm(date, text) {
    for (let i = 0; i < 6; i++)
        text[i] = date.substring(i * 2, (i + 1) * 2) + text[i];
    return text;
}

// ＊setElem(float arr[6]) 기울기, 가속도, 고도 값 6개
function setElem(pos) {
    var data = ["slopX", "slopY", "accX", "accY", "accZ", "altiH"];
    for (let i = 0; i < pos.length; i++)
        document.getElementById(data[i]).innerHTML = pos[i].toFixed(2);
}

// ＊setState(int, boolean) 센서 키, 작동 여부
function setState(key, on) {
    if (typeof key == "undefined") key = "####"; //테스트
    if (typeof on == "undefined") on = false; //테스트
    document.getElementById("state").innerHTML = keyForm(key) + " " + ((on) ? "작동중" : "중지");
}

// *setDate(string[12]) string[12] = "YYMMDDHHMMSS"
function setDate(date) {
    var text = dateForm(date, ["년", "월", "일", "시", "분", "초"]);

    document.getElementById('uptime').innerHTML = "20" + text.join(' ');
}

var logNum = 0;
//위부터 쌓이게 변경
//100개 제한, 원형큐
// *addLog 센서로그(string[12], String) string[12] = "YYMMDDHHMMSS", 메세지
function addLog(date, msg) {
    delLog();

    var text = dateForm(date, ["/", "/", "/", ":", ":", ""]);
    var eLog = document.createElement("div");
    eLog.id = "log" + logNum;
    eLog.className = "log";
    eLog.innerHTML = ["[NFO][" + text.join(' ') + "] " + msg + "(" + logNum + ")"].join("");
    document.getElementById('log').append(eLog);

    logNum++;
    if (logNum > 99) logNum = 0; //로그수 제한
}

//로그가 있으면 삭제
function delLog() {
    elem = document.getElementById("log" + logNum);
    if (elem != null) elem.remove();
}