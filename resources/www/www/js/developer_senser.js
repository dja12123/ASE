var pos = [];

window.onload = function() {
    setState();

    for (let i = 0; i < 6; i++) {
        pos[i] = Math.random()*100;
    }
    setElem(pos);
    setDate("190222133235");
    for(let i=0; i<150; i++)
        addLog("190222133235", "센서 온라인");
}

setInterval(function() {
    setElem(pos);
}, 300)

setInterval(function() {
    for (let i = 0; i < 6; i++) {
        pos[i] += Math.random()/10;
    }
}, 150)

