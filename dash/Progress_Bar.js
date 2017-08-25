//Progress_Bar.js

class ChezyBar {

	constructor(jQueryElement, max) {
		this.element = jQueryElement
		this.innerElement = this.element.find("div")
		this.innerText = this.element.find("div").find("p")
		this.width = jQueryElement.width();
		this.max = max
	}

	set val(val) {  //Percentage from 0 to 1
		this.value = val;
		var progessWidth = this.width * this.value;
		this.innerElement.width(progessWidth)
		this.innerText.text((this.max)? (Math.floor(val * this.max * 100)/100) : (Math.floor(this.value*10000) / 100) + "%")
		this.innerElement.css('background-color', this.ColorCallback(val).bg || "#DDD");
		this.innerElement.css('color', this.ColorCallback(val).fg || "black");
	}

	get val() {
		return this.value
	}

	colorFunction(callback) { //A callback function that allows a user to specify color ranges by returning a color given an input value
		this.ColorCallback = callback
	}
}