/*
서버 -> 클라이언트 -> 웹
클라이언트 단에서 데이터 형식을 보내면 웹 형식 작성

실시간 수치 변화 -> 데이터를 지속적으로 받음 -> roadData 
데이터를 지속적으로 표시 -> js, DOM
*/

var eItem;

var structItem = {};

var item;

function creatArr(key, num) {
    item = new Object();
    creatItem(structItem[key].num);
}

//센서 속성값
/*
function roadData(key, sX, sY, aX, aY, aZ, aH) {
    structItem[key].num = num;
    structItem[key].slopX = sX;
    structItem[key].slopY = sY;
    structItem[key].accX = aX;
    structItem[key].accY = aY;
    structItem[key].accZ = aZ;
    structItem[key].altiH = aH;
}
*/

/*
key값으로 graph에 띄울 데이터 구분 -> 어떤 key값의 버튼으로 눌러졌는지 확인 ->
버튼 onclink을 함수호출로 수정 -> 페이지전환 함수 작성
*/
function addItem(num) {
    eItem = document.createElement("items");
    eItem.innerHTML = [
        '<table class="item">',
        '<tbody><tr><td class="title">',
        num,
        '</td>',
        '<td></td>',
        '<td>',
        '<label class="switch">',
        '<input type="checkbox">',
        '<div class="slider round"></div>',
        '</label>',
        '</td>',
        '<td>',
        '<button class="item-btn" onclick="location.href=`sensor.html`">VIEW</button>',
        '</td>',
        ' </tr></tbody></table>',
    ].join("");
    document.getElementById('items').appendChild(eItem);
}
