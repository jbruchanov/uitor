//script injected to "/" file to see "reload" button if there was a file change
var reloadButton = document.createElement("button");
var reloadSocket = null;

initUI();
startSocket();

function dlog(msg) {console.log(msg)}

function initUI() {
    reloadButton.style = "position:absolute;right:10px;bottom:10px";
    reloadButton.innerHTML = "Reload";
    document.body.appendChild(reloadButton);
    reloadButton.style.display = "none";
    document.body.append(reloadButton);
    reloadButton.onclick = function (ev) {
        reloadButton.innerHTML = "Rebuilding...";
        reloadButton.disabled = true;
        reloadSocket.send("rebuild")
    }
}

function startSocket() {
    var port = "%PORT%";//has to be replaced by server
    reloadSocket = new WebSocket("ws://127.0.0.1:" + port + "/wss/files");
    reloadSocket.onmessage = function (ev) {
        if (ev.data.toString().startsWith("reloaded")) {
            dlog("reloading");
            location.reload()
        } else if (ev.data.toString().startsWith("failed")) {
            dlog("reloading failed");
            reloadButton.innerHTML = "Failed";
            reloadButton.disabled = false;
        } else if (ev.data.toString().startsWith("reload")) {
            reloadButton.style.display = null
        }
        dlog(ev.data)
    };

    reloadSocket.onopen = function (e) {
        dlog("[ReloadScript] Connection established");
    };

    reloadSocket.onclose = function (event) {
        if (event.wasClean) {
            dlog("[ReloadScript] Connection closed cleanly, code=" + event.code + " reason=" + event.reason);
        } else {
            // e.g. server process killed or network down
            // event.code is usually 1006 in this case
            dlog("[ReloadScript] Connection died");
        }
        startSocket()
    };

    reloadSocket.onerror = function (error) {
        dlog("[ReloadScript] " + error.message);
    };
}