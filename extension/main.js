function processOperation(data, sender, response) {
	
	try {
		data["url"] = sender.url;
		chrome.runtime.sendNativeMessage('djasmime', data, response);
		console.log("DJASMIME:Sent native message");
    }
    catch( e ) {
        console.log(e.message);
    }
    return true;
}

chrome.runtime.onMessageExternal.addListener(processOperation);