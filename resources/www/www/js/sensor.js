//날짜 출력형식
function dateForm(date, text) {
    for (let i = 0; i < 6; i++)
        text[i] = date.substring(i * 2, (i + 1) * 2) + text[i];
    return text;
}

// 페이지 시작시 표시할 "센서 키", "작동상태"
// ＊setState(int, boolean) 센서 키, 작동상태(on/off)
function setState(key, on) {
    if (typeof key == "undefined") { key = "####"; console.log("key: undefined"); }  //삭제
    if (typeof on == "undefined") { on = false; console.log("on: undefined"); } //삭제
    document.title = "sensor " + key;
    document.getElementById("state").innerHTML = keyForm(key) + " " + ((on) ? "작동중" : "중지");
}

// "센서 데이터 값" 설정
// ＊setElem(float arr[6]) 기울기2, 가속도3, 고도1 6개 데이터
function setElem(pos) {
    var data = ["slopX", "slopY", "accX", "accY", "accZ", "alti", "temp"];
    for (let i = 0; i < pos.length; i++)
        document.getElementById(data[i]).innerHTML = pos[i].toFixed(2);
}

// "데이터 수집 시간" 설정
// *setDate(string[12]) string[12] = "YYMMDDHHMMSS"
function setDate(date) {
    var text = dateForm(date, ["년", "월", "일", "시", "분", "초"]);

    document.getElementById('uptime').innerHTML = "20" + text.join(' ');
}

var logNum = 0;
// "로그" 추가
// *addLog(string[12], String) string[12] = "YYMMDDHHMMSS", 메세지
function addLog(date, msg) { //100개 제한, 원형큐 / 위부터 쌓이게 변경
    delLog();

    var text = dateForm(date, ["/", "/", " ", ":", ":", ""]);
    var eLog = document.createElement("div");
    eLog.id = "log" + logNum;
    eLog.className = "log";
    eLog.innerHTML = ["[NFO][" + text.join('') + "] " + msg + "(" + logNum + ")"].join("");
    document.getElementById('log').append(eLog);

    logNum++;
    if (logNum > 99) logNum = 0; //로그갯수 제한
}

// "로그" 삭제
//로그가 있으면 삭제
function delLog() {
    elem = document.getElementById("log" + logNum);
    if (elem != null) elem.remove();
}z