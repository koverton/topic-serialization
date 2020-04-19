// Requires javascript modules:
//     ../solconn.js
//
// Requires the following HTML elements:
//      <img id=logo onclick="SolBounce.dropDot()" src="solace_logo_green.png">
//
// Requires the following CSS styles in style.css
//    #container: defines the size of the div containing the animated canvas
//    #animate  : the canvas being animated
//    img       : properly sizes the Solace logo image

var SolBounce = (function() {

  // - + - + - + - + - + - + - + - + - + - + - + - + -
  //   Member variables
  // - + - + - + - + - + - + - + - + - + - + - + - + -
  const accelRate = 0.08
  const radius = 42
  const RED    = '#FF0040'
  const GREEN  = '#00C895'
  const BLUE   = '#0040FF'
  var start = { top: 0, left: 0 }

  // - + - + - + - + - + - + - + - + - + - + - + - + -
  //  Drawing the dot
  // - + - + - + - + - + - + - + - + - + - + - + - + -
  var init = function (color) {
    initDot(document.getElementById('animate'), color)
  }

  var getColor = function(color) {
    if (color == 'green' ) {
        return GREEN
    }
    return color.toUpperCase()
  }

  var initDot = function (dot, color) {
    start = calcstart()
    dot.style.display = 'block'
    dot.style.left    = start.left + 'px'
    dot.style.top     = start.top + 'px'
    var ctx = dot.getContext('2d')
    ctx.beginPath()
    ctx.arc(dot.width/2, dot.height/2, radius, 0, 2*Math.PI, false)
    ctx.fillStyle = getColor(color)
    ctx.fill()
  }

  var calcstart = function () {
    const logorect = document.getElementById('logo').getBoundingClientRect()
    return {
      top: logorect.bottom - 55,
      left:logorect.right - 84
    }
  }

  // - + - + - + - + - + - + - + - + - + - + - + - + -
  //  Animating the dot
  // - + - + - + - + - + - + - + - + - + - + - + - + -

  var dropDot = function (txt) {
    var dot = document.getElementById('animate')   
    initDot(dot, txt)
    dot.style.top = start.top
    var model = {
      iter: 1,
      endtest: function () { return this.top >= this.stopAt },
      elem: dot,
      top: start.top,
      accel: 0,
      accelRate: accelRate,
      stopAt: (window.innerHeight-46),
      id: null
    }
    model.id = setInterval(animarc, 5, model)
  }

  var animarc = function (model) {
    if (model.endtest()) {
      // Reached end of the arc
      clearInterval(model.id)
      if (model.iter < 2) {
        // do another arc in the reverse (e.g. bounce)
        model.iter++
        model.stopAt = start.top
        model.endtest = function() { return this.top <= this.stopAt }
        model.accel = model.accel * (-1)
        model.id = setInterval(animarc, 5, model)
      }
    }
    else {
      // continue drawing the arc
      model.top = model.top + model.accel
      model.accel += model.accelRate
      model.elem.style.top = model.top + 'px' 
    }
  }

  return {
    init         : init,
    calcstart    : calcstart,
    dropDot      : dropDot
  }
})()
