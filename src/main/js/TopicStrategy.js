// module TopicStrategy
var TopicStrategy = (function() {

    const LType = {
            STATIC : 'static',
            FIELD  : 'field'
    }

    var parse = function ( expression ) {
        let levels = []
        while (expression != null && expression.length > 0 ) {
            const matches = expression.match( /^(.*?)\{(.+?)\}(.*)$/ )
            if (matches != null && matches.length > 1) {
                const preamble = matches[1]
                levels.push( { type:LType.STATIC, value:preamble } )
                const field = matches[2]
                levels.push( { type:LType.FIELD, value:field } )
                expression = matches[3]
            }
            else {
                levels.push( { type:LType.STATIC, value:expression } )
                expression = null
            }
        }
        return levels
    }

    var makeTopic = function ( strategy, msg ) {
        let topic = ''
        strategy.forEach( lvl => {
            if ( lvl.type == LType.STATIC )
                topic += lvl.value
            else
                topic += msg[ lvl.value ]
        })
        return topic
    }

    var makeSub = function ( strategy, keys ) {
        let topic = ''
        strategy.forEach( lvl => {
            if ( lvl.type == LType.STATIC )
                topic += lvl.value
            else {
                if ( lvl.value in keys )
                    topic += keys[ lvl.value ]
                else
                    topic += '*'
            }
        })
        return topic
    }


        return {
            LType     : LType ,
            parse     : parse,
            makeTopic : makeTopic,
            makeSub   : makeSub
        }
})();
