methodRegistry = {};

registerApp = function(appName) {
    window.top.postMessage({
        'type': '_register',
        'name': appName
    }, "*");
};

unregisterApp = function(appName) {
    window.top.postMessage({
        'type': '_unregister',
        'name': appName
    }, "*");
};

registerMethod = function(methodName, targetApp, callback) {
    if (!methodRegistry[targetApp]) {
        methodRegistry[targetApp] = {}
    }
    methodRegistry[targetApp][methodName] = callback;
};

unregisterMethod = function(methodName, targetApp) {
    if (methodRegistry[targetApp]) {
        delete(methodRegistry[targetApp])[methodName];
    }
};

callMethod = function(target, callerName, methodName, args) {
    window.top.postMessage({
        'type': '_call',
        'target': target,
        'caller': callerName,
        'method': methodName,
        'args': args
    }, "*");
};

acceptMessage = function(event) {
    if (event.data.type != '_invoke') {
        return;
    }

    if (!event.data.method) {
        return;
    }

    var method = event.data.method;
    var caller = event.data.caller;

    method = methodRegistry[caller][method];

    if (event.data.args !== null) {
        method(event.data.args);
    } else {
        method();
    }
}

window.addEventListener('message', acceptMessage);
