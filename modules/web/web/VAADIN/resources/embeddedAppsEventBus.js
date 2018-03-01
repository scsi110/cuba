interFrameRegistry = {};

acceptMessage = function(event) {
    if (!event.data.type) {
        return;
    }
    switch (event.data.type) {
        case '_register':
            interFrameRegistry[event.data.name] = {
                'source': event.source,
                'origin': event.origin
            }
            break;
        case '_unregister':
            delete interFrameRegistry[event.data.name];
            break;
        case '_call':
            info = interFrameRegistry[event.data.target]
            info.source.postMessage({
                'type': '_invoke',
                'method': event.data.method,
                'caller': event.data.caller,
                'args': event.data.args
            }, info.origin)
            break;
        default:
            break;
    }
}

window.addEventListener('message', acceptMessage);
window.postMessage({
    'type': '_register',
    'name': 'host'
}, "*")
