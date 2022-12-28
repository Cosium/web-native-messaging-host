class NativeChannel {

    constructor() {
        if (typeof browser === "undefined") {
            this.browser = chrome;
        } else {
            this.browser = browser;
        }
        this.port = null;
    }

    async connect() {

        this.port = await new Promise((resolve, reject) => {
            const port = this.browser.runtime.connectNative('com.cosium.web_native_messaging_host_test');
            const heartbeatListener = message => {
                port.onMessage.removeListener(heartbeatListener);
                if (message.type !== 'heartbeat') {
                    return;
                }
                resolve(port);
            };
            port.onMessage.addListener(heartbeatListener);
            const disconnectListener = () => {
                port.onDisconnect.removeListener(disconnectListener);
                const error = this.browser.runtime.lastError;
                console.error(error.message);
                reject(error);
            };
            port.onDisconnect.addListener(disconnectListener);
        });

        console.info('Connected');

        this.port.onMessage.addListener(message => {
            console.info(`Received message ${JSON.stringify(message)}`);
        });
        this.port.onDisconnect.addListener(() => {
            console.info('Disconnected');
            this.port = null;
        });
    }

    pollOneMessage(timeout) {
        return new Promise((resolve, reject) => {
            const listener = message => {
                this.port.onMessage.removeListener(listener);
                resolve(message);
            };
            this.port.onMessage.addListener(listener);
            setTimeout(reject, timeout);
        });
    }

    disconnect() {
        if (this.port) {
            this.port.disconnect();
        }
        this.port = null;
    }

}

window.nativeChannel = new NativeChannel();
