var reloadButton = document.createElement("button");
var socket = null

initUI();
startSocket()

function dlog(msg) {
    console.log(msg)
}

function initUI() {
    reloadButton.style = "position:absolute;right:10px;bottom:10px";
    reloadButton.innerHTML = "Reload";
    document.body.appendChild(reloadButton);
    reloadButton.style.display = "none"
    document.body.append(reloadButton)
    reloadButton.onclick = function (ev) {
        reloadButton.innerHTML = "Rebuilding...";
        reloadButton.disabled = true;
        socket.send("rebuild")
    }
}

function startSocket() {
    socket = new WebSocket("ws://127.0.0.1:8080/wss/files");
    // var socket = new WebSocket("/wss/files", "dev");

    socket.onmessage = function (ev) {
        if (ev.data.toString().startsWith("reloaded")) {
            dlog("reloading");
            location.reload()
        } else if (ev.data.toString().startsWith("reload")) {
            reloadButton.style.display = null
        }
        dlog(ev.data)
    };

    socket.onopen = function (e) {
        dlog("[open] Connection established");
    };

    socket.onclose = function (event) {
        if (event.wasClean) {
            dlog("[close] Connection closed cleanly, code=" + event.code + " reason=" + event.reason);
        } else {
            // e.g. server process killed or network down
            // event.code is usually 1006 in this case
            dlog("[close] Connection died");
        }
        startSocket()
    };

    socket.onerror = function (error) {
        dlog("[error] " + error.message);
    };
}