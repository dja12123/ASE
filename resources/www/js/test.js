//테스트 스크립트

var eItem;

var structItem = {};

var item;

var key = 0; //key 수정

function creatItem() {
    item = new Object();
    item.num = 3032;
    item.slopX = 23.02;
    item.slopY = 32.6;
    item.accX = 7.45;
    item.accY = 5.34;
    item.accZ = 0.09;
    item.altiH = 73.23;
    structItem[key] = item;
}

// table -> div 수정
function addItem() {
    creatItem();
    eItem = document.createElement("items");
    eItem.innerHTML = [
        '<table class="item">',
        '<tbody><tr><td class="title">',
        structItem[key++].num, //key 수정
        '</td>',
        '<td></td>',
        '<td>',
        '<label class="switch">',
        '<input type="checkbox" >',
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
