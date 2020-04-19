// Requires javascript modules:
//      solclient-full.js
//
// Requires the following HTML elements:
//      <input id=inputHost>
//      <p id=connDetails></p>
//      <p id=subDetails></p>
//
// Requires the following CSS styles in style.css
//      .connectionInfo : sets fontography for connection info display

var SolConn = (function() {

    // - + - + - + - + - + - + - + - + - + - + - + - + -
    //   Member variables
	// - + - + - + - + - + - + - + - + - + - + - + - + -
	// ws://192.168.56.201, ws://mr85s7y8ur59.messaging.solace.cloud:20259
	var context = {
	    urlList: [ 'ws://192.168.56.201','ws://192.168.56.202' ],
	    sess: null,
        hostString: 'localhost',
        msgVpnName: 'default',
        clientUsername: 'default',
        clientPassword: 'default',
        sendMsgs: false,
        color:     'green',
        timerId: -1
	}

    var topicStrategy = TopicStrategy.parse( 'poskeeper/{region}/{book}/{account}/{instrument}/position' )

    // - + - + - + - + - + - + - + - + - + - + - + - + -
    //   Public Interface UI-invoked functions
    // - + - + - + - + - + - + - + - + - + - + - + - + -
	
	var onConnect = function (ctx) {
		for( var k in ctx ) {
		    context[k] = ctx[k]
		}

		var factoryProps = new solace.SolclientFactoryProperties()
		factoryProps.logLevel = solace.LogLevel.DEBUG
		solace.SolclientFactory.init( factoryProps )

		connectSolace( context.hostString )
	}

	var onDisconnect = function () {
		disconnectSession()
		document.getElementById('connDetails').innerHTML = ''
		document.getElementById('subDetails').innerHTML = ''
		clearTimeout(context.timerId) // stops any periodic msg-sender
	}

    var toggleSender = function(cb) {
        context.sendMsgs = cb.checked
    }

    function addKeyIfPresent(keys, dockey, key) {
        let docval = document.getElementById(dockey).value
        if ( typeof docval !== 'undefined' && docval.length > 0 ) {
            keys[key] = document.getElementById(dockey).value
            return
        }
    }
    var onSubscribe = function() {
        console.log('onSubscribe')
        let keys = {}
        let dockeys = ['sub.region', 'sub.book', 'sub.account', 'sub.instrument']
        dockeys.forEach( dockey => {
                addKeyIfPresent( keys, dockey, dockey.replace('sub.','') )
            })
        console.log('KEYS:' + JSON.stringify(keys))
        let sub = TopicStrategy.makeSub( topicStrategy, keys )
        context.subscription = sub
        console.log('SUBSCRIPTION:' + sub )
        addSub( sub )
    }

	// - + - + - + - + - + - + - + - + - + - + - + - + -
    //   Private internally-invoked functions
	// - + - + - + - + - + - + - + - + - + - + - + - + -
	
	function connectSolace(destUrl) {
		context.urlList = destUrl.split(',')
		createSession()
		connectSession()
	}

	// STATIC MESSAGE EVENT CALLBACK
	function onMessage(sess, msg) {
            var text = msg.getDestination().getName().match(/([^\/]+)$/)[0]
            if ( text == null ) {
		var container = msg.getSdtContainer()
		var text = null
		if ( container != null ) {
			text = container.getValue()
		}
		else {
			text = msg.getBinaryAttachment()
		}
            }
            if ( text == null ) {
                text = 'Gak!'
            }
		context.callback( text )
	}

	// STATIC SESSION EVENT CALLBACK
	function onSessionEvt(session, evt) {
		logMsg( 'session event: ' + JSON.stringify(evt) )
		if ( evt.sessionEventCode == solace.SessionEventCode.UP_NOTICE ) {
			document.getElementById('connDetails').innerHTML = 'I am connected to ' + context.urlList
		    if ( typeof context.subscription !== 'undefined' ) {
                console.log('Subscribing')
    			addSub( context.subscription )
            }
			schedSend()
		}
		else if ( evt.sessionEventCode == solace.SessionEventCode.DOWN_NOTICE ) {
		    clearTimeout(context.timerId)
			document.getElementById('connDetails').innerHTML = '(Not connected.)'
		}
	}

    function schedSend() {
        if( context.sendMsgs && context.sess !== null ) {
            // Send logic
            let msg = {
                region: document.getElementById('msg.region').value,
                book: document.getElementById('msg.book').value,
                account: document.getElementById('msg.account').value,
                instrument: document.getElementById('msg.instrument').value
            }
            sendMsg( msg )
        }
        // ALWAYS set timer for recursive call, in case they disable/re-enable sender
        context.timerId = setTimeout( schedSend, 1000 )
    }

    function sendMsg(msg) {
        let topic = TopicStrategy.makeTopic( topicStrategy, msg )
        let solmsg = solace.SolclientFactory.createMessage()
        solmsg.setDestination( solace.SolclientFactory.createTopicDestination(topic) )
        try {
            context.sess.send( solmsg )
            // logMsg( 'Message published.' )
        } catch (error) {
            logError( "Failure publishing msg", error )
        }
    }

    // - + - + - + - + - + - + - + - + - + - + - + - + -
    //   Private Internal functions
    // - + - + - + - + - + - + - + - + - + - + - + - + -
	
	function createSession() {
	    if ( context.sess !== null ) {
	        logMsg( "SKIPPING: Session SEEMS to be already created.")
	        return
	    }
		logMsg( "CONNECTING to URL:" + context.urlList
					+ ",VPN:" + context.msgVpnName
					+ ",USER:" + context.clientUsername )
		try {
			context.sess = solace.SolclientFactory.createSession( {
                url: context.urlList,
                userName: context.clientUsername,
                vpnName: context.msgVpnName,
                password: context.clientPassword,
                generateReceiveTimestamps: true,
                reapplySubscriptions: true
			},
            new solace.MessageRxCBInfo(onMessage),
            new solace.SessionEventCBInfo(onSessionEvt) )
		}
		catch(error) {
			logError( "createSession", error )
		}
	}

	function connectSession() {
		try {
			context.sess.connect()
		}
		catch(error) {
			logError( "connectSession", error )
		}
	}

	function disconnectSession() {
		try {
		    if (typeof context.sess !== 'undefined' ) {
                context.sess.disconnect()
                context.sess.dispose()
                context.sess = null
                document.getElementById('connDetails').innerHTML = '(Not connected.)'
		    }
		}
		catch(error) {
			logError( "disconnectSession", error )
		}
	}

	function addSub(sub) {
		logMsg( "SUBSCRIBE: " + sub )
		try {
			var topic = solace.SolclientFactory.createTopic(sub)
			context.sess.subscribe( topic, true, sub, 3000 )
			document.getElementById('subDetails').innerHTML = 'I am subscribed to ' + sub
		}
		catch(error) {
			logError( "addSub", error )
		}
	}


    // - + - + - + - + - + - + - + - + - + - + - + - + -
    //  LOGGING
	// - + - + - + - + - + - + - + - + - + - + - + - + -
	
	function logMsg(msg) {
		console.log(msg)
	}

	function logError(fname, err) {
		// First format and log the error
		var subcodeStr = ( err.subcode==null ? "no subcode" : err.subcode.toString() )

		var msg = "ERROR IN "+fname+"\n"+"Subcode("
			+ subcodeStr  + ") "
			+ "Msg:{" + err.message + "} Reason:{" + err.reason + "}\n"

		logMsg(msg)

		// Then log the stack-trace
		if ( err.stack != null ) {
			logMsg( "STACK:" + err.stack.toString() )
		}
	}


	return {
	    toggleSender : toggleSender,
		onConnect    : onConnect,
		onDisconnect : onDisconnect,
		onSubscribe  : onSubscribe,
		sendMsg       : sendMsg
	}
})();
