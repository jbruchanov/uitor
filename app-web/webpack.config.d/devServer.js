//https://webpack.js.org/configuration/dev-server/
if (config.devServer) {
    //listen on every iface
    //config.devServer.host = "0.0.0.0"
    //config.devServer.open = false
    //config.devServer.overlay = true
    let androidDevice = {
        host: "127.0.0.1",
        protocol: 'http:',
        port: 8081
    }
    config.devServer.proxy = {
        '/api': { target: androidDevice }
    }
}
//to show a config, there is probably better way, but console.log prints somewhere what gradlew doesn't show
//throw Error(JSON.stringify(config))