#!/usr/local/bin/node

var strat = require('./TopicStrategy')

const expression = 'poskeeper/{region}/{book}/{account}/{instrument}/position'

console.log( 'strategy' )
var strategy = strat.parse( expression )
console.log( strategy )

console.log( 'makeSub' )
var subkeys = { 'book':'1*', 'instrument':'aapl' }
var subscription = strat.makeSub( strategy, subkeys )
console.log( subscription )


console.log( 'makeTopic' )
var msg = {
	region : 'APAC',
	book   : 'KEN',
	account: 1234,
	instrument: 'AAPL',
	type   : 'position'
}
var topic = strat.makeTopic( strategy, msg )
console.log( topic )


