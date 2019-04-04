//날짜 출력형식
function dateForm(date, text) {
    for (let i = 0; i < 6; i++)
        text[i] = date.substring(i * 2, (i + 1) * 2) + text[i];
    return text;
}

//"로그" 삭제
//로그가 있으면 삭제
function delLog() {
    elem = document.getElementById("log" + logNum);
    if (elem != null) elem.remove();
}

// 페이지가 로드될 때 센서 키를 변수에 저장
window.onload = function() {
    tmp = location.href.split("?");
    infoKey = tmp[1];
    dataSetKey(infoKey);
}

//===============================================================

// * 가져온 info페이지 "센서 키"
var infoKey;

// 페이지 시작시 표시할 "센서 키"
// * dateSetKey(string) 센서 키
function dataSetKey(key) {
    //입력된 키 
    document.title = "sensor " + key;
    document.getElementById("state_name").innerHTML = key;
}

// 센서의 상태 표시 및 변경 "작동상태"
// ＊setState(boolean) 작동상태(on/off)
function setState(on) {
    document.getElementById("state").innerHTML = ((on) ? "작동중" : "중지");
}

// "센서 데이터 값" 설정
// ＊setElem(float(double)[7]) 기울기(2), 가속도(3), 고도(1), 온도(1) 7개 데이터
function setElem(pos) {
    var data = ["slopX", "slopY", "accX", "accY", "accZ", "alti", "temp"];
    for (let i = 0; i < pos.length; i++)
        document.getElementById(data[i]).innerHTML = pos[i].toFixed(2);
}

// "데이터 수집 시간" 설정
// * setDate(string[12]) string[12] = "YYMMDDHHMMSS"
function setDate(date) {
    var text = dateForm(date, ["년", "월", "일", "시", "분", "초"]);
    document.getElementById('uptime').innerHTML = 
    document.getElementById('uptime').innerHTML = "20" + text.join(' ');
}

var logNum = 0;
// "로그" 추가
// * addLog(string[12], String) string[12] = "YYMMDDHHMMSS", 메세지
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