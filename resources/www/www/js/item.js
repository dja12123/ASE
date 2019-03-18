//key가 4자리 이하일때 앞에 0을 추가
function keyForm(num) {
    num = num + '';
    return num.length >= 4 ? num : new Array(5 - num.length).join('0') + num;
}

var total = 0;

//(add, del)함수 실행시 total체크
function setTotal() {
    var msg;
    if(total>0) msg = total + "개의 센서 확인";
    else msg = "확인된 센서가 없습니다"
    document.getElementById("total").innerHTML = msg;
}

//table -> div 수정
// *addItem(int, boolean) 추가할 센서 키, 작동여부(on, off)
function addItem(key, on) {
    total++;
    setTotal();
    var state = on? "checked": "";
    var eItem = document.createElement("table");
    eItem.id = key;
    eItem.className = 'item';
    eItem.innerHTML = [
        '<tbody><tr><td class="title">',
        keyForm(key),
        '</td>',
        '<td></td>',
        '<td>',
        '<label class="switch">',
        '<input id="',
        key+"stat",
        '" type="checkbox"',
        state,
        '>',
        '<div class="slider round"></div>',
        '</label>',
        '</td>',
        '<td>',
        '<button class="item-btn" onclick="location.href=`sensor.html`">VIEW</button>',
        '</td>',
        '</tr></tbody>',
    ].join("");
    document.getElementById('items').append(eItem);
}

// ＊delItem(int) 삭제할 센서 키
function delItem(key) {
    total--;
    setTotal();
    document.getElementById(key).remove();
}

// ＊state(int, boolean) 센서 키, 작동상태(켜짐, 꺼짐) 
function state(key, on) {
    var sencer = document.getElementById(key+"stat");
    if(sencer.checked != on) sencer.checked = on;
}
