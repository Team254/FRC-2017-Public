var waypoints = [];
var ctx;
var width = 1656; //pixels
var height = 823; //pixels
var fieldWidth = 652; // in inches
var fieldHeight = 324; // in inches
var robotWidth = 35.45; //inches
var robotHeight = 33.325; //inches
var pointRadius = 5;
var turnRadius = 30;
var kEpsilon = 1E-9;
var image;
var imageFlipped;
var wto;

var maxSpeed = 120;
var maxSpeedColor = [0, 255, 0];
var minSpeed = 0;
var minSpeedColor = [255, 0, 0];
var pathFillColor = "rgba(150, 150, 150, 0.5)";

class Translation2d {
	constructor(x, y) {
		this.x = x;
		this.y = y;
	}

	norm() {
		return Math.sqrt(Translation2d.dot(this, this));
	}

	scale(s) {
		return new Translation2d(this.x * s, this.y * s);
	}

	translate(t) {
		return new Translation2d(this.x + t.x, this.y + t.y);
	}

	invert() {
		return new Translation2d(-this.x, -this.y);
	}

	perp() {
		return new Translation2d(-this.y, this.x);
	}

	draw(color) {
		color = color || "#f72c1c";
		ctx.beginPath();
		ctx.arc(this.drawX, this.drawY, pointRadius, 0, 2 * Math.PI, false);
		ctx.fillStyle = color;
		ctx.strokeStyle = color;
		ctx.fill();
		ctx.lineWidth = 0;
		ctx.stroke();
	}

	get drawX() {
		return this.x*(width/fieldWidth);
	}

	get drawY() {
		return height - this.y*(height/fieldHeight);
	}

	get angle() {
		return Math.atan2(-this.y, this.x);
	}

	static diff(a, b) {
		return new Translation2d(b.x - a.x, b.y - a.y);
	}

	static cross(a, b) {
		return a.x * b.y - a.y * b.x;
	}

	static dot(a, b) {
		return a.x * b.x + a.y * b.y;
	}

	static angle(a, b) {
		return Math.acos(Translation2d.dot(a,b) / (a.norm() * b.norm()));
	}
}

class Waypoint {
	constructor(position, speed, radius, comment) {
		this.position = position;
		this.speed = speed;
		this.radius = radius;
		this.comment = comment;
	}

	draw() {
		this.position.draw((this.radius > 0) ? "rgba(120,120,120,0.8)" : null);
	}

	toString() {
		var comment = (this.comment.length > 0) ? " //" + this.comment : "";
		return "sWaypoints.add(new Waypoint("+this.position.x+","+this.position.y+","+this.radius+","+this.speed+"));" + comment;
	}
}

class Line {
	constructor(pointA, pointB) {
		this.pointA = pointA;
		this.pointB = pointB;
		this.slope = Translation2d.diff(pointA.position, pointB.position);
		this.start = pointA.position.translate( this.slope.scale( pointA.radius/this.slope.norm() ) );
		this.end = pointB.position.translate( this.slope.scale( pointB.radius/this.slope.norm() ).invert() );
	}

	draw() {
		ctx.beginPath();
        ctx.moveTo(this.start.drawX, this.start.drawY);
        ctx.lineTo(this.end.drawX, this.end.drawY);
        
		try {
        	var grad = ctx.createLinearGradient(this.start.drawX, this.start.drawY, this.end.drawX, this.end.drawY);
	grad.addColorStop(0, getColorForSpeed(this.pointB.speed));
		grad.addColorStop(1, getColorForSpeed(getNextSpeed(this.pointB)));
		ctx.strokeStyle = grad;
		} catch (e) {
			ctx.strokeStyle = "#00ff00"
		}

        ctx.lineWidth = pointRadius * 2;
        ctx.stroke();
        this.pointA.draw();
        this.pointB.draw();
	}

	fill() {
		var start = this.start;
		var deltaEnd = Translation2d.diff(this.start,this.end);
		var angle = deltaEnd.angle;
		var length = deltaEnd.norm();
		for(var i=0; i<length; i++) {
		drawRotatedRect(start.translate(deltaEnd.scale(i/length)), robotHeight, robotWidth, angle, null, pathFillColor, true);
		}
	}

	translation() {
		return new Translation2d(this.pointB.position.y - this.pointA.position.y, this.pointB.position.x - this.pointA.position.x)
	}

	slope() {
		if(this.pointB.position.x - this.pointA.position.x > kEpsilon)
			return (this.pointB.position.y - this.pointA.position.y) / (this.pointB.position.x - this.pointA.position.x);
		else
			return (this.pointB.position.y - this.pointA.position.y) / kEpsilon;
	}

	b() {
		return this.pointA.y - this.slope() * this.pointA.x;
	}

	static intersect(a, b, c, d) {
		var i = ((a.x-b.x)*(c.y-d.y) - (a.y-b.y)*(c.x-d.x));
		i = (Math.abs(i) < kEpsilon) ? kEpsilon : i;
		var x = (Translation2d.cross(a, b) * (c.x - d.x) - Translation2d.cross(c, d)*(a.x - b.x)) / i;
		var y = (Translation2d.cross(a, b) * (c.y - d.y) - Translation2d.cross(c, d)*(a.y - b.y)) / i;
		return new Translation2d(x, y);
	}

	static pointSlope(p, s) {
		return new Line(p, p.translate(s));
	}
}

class Arc {
	constructor(lineA, lineB) {
		this.lineA = lineA;
		this.lineB = lineB;
		this.center = Line.intersect(lineA.end, lineA.end.translate(lineA.slope.perp()), lineB.start, lineB.start.translate(lineB.slope.perp()));
		this.center.draw;
		this.radius = Translation2d.diff(lineA.end, this.center).norm();
	}

	draw() {
		var sTrans = Translation2d.diff(this.center, this.lineA.end);
		var eTrans = Translation2d.diff(this.center, this.lineB.start);
		console.log(sTrans);
		console.log(eTrans);
		var sAngle, eAngle;
		if(Translation2d.cross(sTrans, eTrans) > 0) {
			eAngle = -Math.atan2(sTrans.y, sTrans.x);
			sAngle = -Math.atan2(eTrans.y, eTrans.x);
		} else {
			sAngle = -Math.atan2(sTrans.y, sTrans.x);
			eAngle = -Math.atan2(eTrans.y, eTrans.x);
		}
		this.lineA.draw();
		this.lineB.draw();
		ctx.beginPath();
		ctx.arc(this.center.drawX,this.center.drawY,this.radius*(width/fieldWidth),sAngle,eAngle);
		ctx.strokeStyle=getColorForSpeed(this.lineB.pointB.speed);
		ctx.stroke();
	}

	fill() {
		this.lineA.fill();
		this.lineB.fill();
		var sTrans = Translation2d.diff(this.center, this.lineA.end);
		var eTrans = Translation2d.diff(this.center, this.lineB.start);
		var sAngle = (Translation2d.cross(sTrans, eTrans) > 0) ? sTrans.angle : eTrans.angle;
		var angle = Translation2d.angle(sTrans, eTrans);
		var length = angle * this.radius;
		for(var i=0; i<length; i+=this.radius/100) {
		drawRotatedRect(this.center.translate(new Translation2d(this.radius*Math.cos(sAngle-i/length*angle),-this.radius*Math.sin(sAngle-i/length*angle))), robotHeight, robotWidth, sAngle-i/length*angle+Math.PI/2, null, pathFillColor, true);
		}

		

	}

	static fromPoints(a, b, c) {
		return new Arc( new Line(a, b), new Line(b, c));
	}
}


function init() { 
	$("#field").css("width", (width / 1.5) + "px");
	$("#field").css("height", (height / 1.5) + "px");
	ctx = document.getElementById('field').getContext('2d')
    ctx.canvas.width = width;
    ctx.canvas.height = height;
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle="#FF0000";
    image = new Image();
    image.src = 'field.png';
    image.onload = function(){
        ctx.drawImage(image, 0, 0, width, height);
        update();
    }
    imageFlipped = new Image();
    imageFlipped.src = 'fieldflipped.png';
    $('input').bind("change paste keyup", function() {
		console.log("change");
		clearTimeout(wto);
			wto = setTimeout(function() {
			update();
		}, 500);
	});
}

function clear() {
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle="#FF0000";
    if(flipped)
    	ctx.drawImage(imageFlipped, 0, 0, width, height);
    else
    	ctx.drawImage(image, 0, 0, width, height);
}

var f;
function create() {
	var a = new Waypoint(new Translation2d(30,30), 0,0,0)
	var b = new Waypoint(new Translation2d(230,30), 0,30,0)
	var c = new Waypoint(new Translation2d(230,230), 0,0,0)
	var d = new Line(a, b);
	var e = new Line(b, c);
	f = new Arc(d, e);
}

function addPoint() {
	var prev;
	if(waypoints.length > 0)
		prev = waypoints[waypoints.length - 1].position;
	else 
		prev = new Translation2d(50, 50);
	$("tbody").append("<tr>"
		+"<td><input value='"+(prev.x+20)+"'></td>"
		+"<td><input value='"+(prev.y+20)+"'></td>"
		+"<td><input value='0'></td>"
		+"<td><input value='60'></td>"
		+"<td class='comments'><input placeholder='Comments'></td>"
		+"<td><button onclick='$(this).parent().parent().remove();update()'>Delete</button></td></tr>"
	);
	update();
	$('input').unbind("change paste keyup");
	$('input').bind("change paste keyup", function() {
		console.log("change");
		clearTimeout(wto);
			wto = setTimeout(function() {
			update();
		}, 500);
	});
}

function update() {
	waypoints = [];
	$('tbody').children('tr').each(function () {
        var x = parseInt( $($($(this).children()).children()[0]).val() );
        console.log(x);
        var y = parseInt( $($($(this).children()).children()[1]).val() );
        var radius = parseInt( $($($(this).children()).children()[2]).val() );
        var speed = parseInt( $($($(this).children()).children()[3]).val() );
        if(isNaN(radius) || isNaN(speed)) {
        	radius = 0;
        	speed = 0;
        }
        var comment = ( $($($(this).children()).children()[4]).val() )
        waypoints.push(new Waypoint(new Translation2d(x,y), speed, radius, comment));
    });
    drawPoints();
    drawRobot();
}

function drawRobot() {
	if(waypoints.length > 1) {
		var deltaStart = Translation2d.diff(waypoints[0].position, waypoints[1].position);
		drawRotatedRect(waypoints[0].position, robotHeight, robotWidth, deltaStart.angle, getColorForSpeed(waypoints[1].speed));

		var deltaEnd = Translation2d.diff(waypoints[waypoints.length-2].position, waypoints[waypoints.length-1].position);
		drawRotatedRect(waypoints[waypoints.length-1].position, robotHeight, robotWidth, deltaEnd.angle, getColorForSpeed(0));
	}
}

function drawRotatedRect(pos,w,h,angle,strokeColor,fillColor,noFill){
	w = w*(width/fieldWidth);
	h = h*(height/fieldHeight);
	fillColor = fillColor || "rgba(0,0,0,0)";
	//ctx.save();
	if(noFill == null || !noFill)
		ctx.beginPath();
	ctx.translate(pos.drawX, pos.drawY);
	ctx.rotate(angle);
    	ctx.rect(-w/2, -h/2, w,h);
	ctx.fillStyle = fillColor;
	if(noFill == null || !noFill)
		ctx.fill();
	if(strokeColor != null) {
		ctx.strokeStyle = strokeColor;
		ctx.lineWidth = 4;
		ctx.stroke();
	}
	ctx.rotate(-angle);
	ctx.translate(-pos.drawX, -pos.drawY);
	//ctx.restore();

}

function drawPoints() {
	clear();
	var i = 0;
	ctx.beginPath();
	do {
		var a = Arc.fromPoints(getPoint(i), getPoint(i+1), getPoint(i+2));
		a.fill();
		i++;
	} while(i < waypoints.length - 2);
	ctx.fill();
	i=0;
	do {
		var a = Arc.fromPoints(getPoint(i), getPoint(i+1), getPoint(i+2));
		a.draw();
		i++;
	} while(i < waypoints.length - 2);

}

function getPoint(i) {
	if(i >= waypoints.length)
		return waypoints[waypoints.length - 1];
	else
		return waypoints[i];
}

function importData() {
	$('#upl').click();
	let u = $('#upl')[0];
	$('#upl').change(() => {
		var file =  u.files[0];
		var fr = new FileReader();
		fr.onload = function(e) {
			var c = fr.result;
			let re = /(?:\/\/\sWAYPOINT_DATA:\s)(.*)/gm;
			let reversed = /(?:\/\/\sIS_REVERSED:\s)(.*)/gm;
			let title = /(?:\/\/\sFILE_NAME:\s)(.*)/gm;
			console.log();
			$("#title").val(title.exec(c)[1]);
			$("#isReversed").prop('checked', reversed.exec(c)[1].includes("true"));
			let jde = re.exec(c)[1];
			let jd = JSON.parse(jde);
			// console.log(jd);
			waypoints = []
			$("tbody").empty();
			jd.forEach((wpd) => {
				let wp = new Waypoint(new Translation2d(wpd.position.x, wpd.position.y), wpd.speed, wpd.radius, wpd.comment);
				// console.log(wp);
				$("tbody").append("<tr>"
					+"<td><input value='" + wp.position.x + "'></td>"
					+"<td><input value='" + wp.position.y + "'></td>"
					+"<td><input value='" + wp.radius + "'></td>"
					+"<td><input value='" + wp.speed + "'></td>"
					+"<td class='comments'><input placeholder='Comments' value='" + wp.comment + "'></td>"
					+"<td><button onclick='$(this).parent().parent().remove();''>Delete</button></td></tr>"
				);
			})
			update();
			$('input').unbind("change paste keyup");
			$('input').bind("change paste keyup", function() {
				console.log("change");
				clearTimeout(wto);
					wto = setTimeout(function() {
					update();
				}, 500);
			});
		}
		fr.readAsText(file);
	});
}

function getDataString() {
	var title = ($("#title").val().length > 0) ? $("#title").val() : "UntitledPath";
	var pathInit = "";
	for(var i=0; i<waypoints.length; i++) {
		pathInit += "        " + waypoints[i].toString() + "\n";
	}
	var startPoint = "new Translation2d(" + waypoints[0].position.x + ", " + waypoints[0].position.y + ")";
	var importStr = "WAYPOINT_DATA: " + JSON.stringify(waypoints);
	var isReversed = $("#isReversed").is(':checked');
	var str = `package com.team254.frc2017.paths;

import java.util.ArrayList;

import com.team254.frc2017.paths.PathBuilder.Waypoint;
import com.team254.lib.util.control.Path;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;

public class ${title} implements PathContainer {
    
    @Override
    public Path buildPath() {
        ArrayList<Waypoint> sWaypoints = new ArrayList<Waypoint>();
${pathInit}
        return PathBuilder.buildPathFromWaypoints(sWaypoints);
    }
    
    @Override
    public RigidTransform2d getStartPose() {
        return new RigidTransform2d(${startPoint}, Rotation2d.fromDegrees(180.0)); 
    }

    @Override
    public boolean isReversed() {
        return ${isReversed}; 
    }
	// ${importStr}
	// IS_REVERSED: ${isReversed}
	// FILE_NAME: ${title}
}`
	return str;
}

function exportData() { 
	update();
	var title = ($("#title").val().length > 0) ? $("#title").val() : "UntitledPath";
	var blob = new Blob([getDataString()], {type: "text/plain;charset=utf-8"});
	saveAs(blob, title+".java");
}

function showData() {
	update();
	var title = ($("#title").val().length > 0) ? $("#title").val() : "UntitledPath";
	$("#modalTitle").html(title + ".java");
	$(".modal > pre").text(getDataString());
	showModal();
}

function showModal() {
	$(".modal, .shade").removeClass("behind");
	$(".modal, .shade").removeClass("hide");
}

function closeModal() {
	$(".modal, .shade").addClass("hide");
	setTimeout(function() {
		$(".modal, .shade").addClass("behind");
	}, 500);
}

var flipped = false;
function flipField() {
	flipped = !flipped;
	if(flipped)
		ctx.drawImage(imageFlipped, 0, 0, width, height);
	else
		ctx.drawImage(image, 0, 0, width, height);
	update();
}

function lerpColor(color1, color2, factor) {
	var result = color1.slice();
	for (var i=0;i<3;i++) {
	result[i] = Math.round(result[i] + factor*(color2[i]-color1[i]));
	}
	return result;
}

function getColorForSpeed(speed) {
	var u = Math.max(0, Math.min(1, speed/maxSpeed));
	if(u<0.5)
		return RGBToHex(lerpColor(minSpeedColor, [255,255,0], u*2));
	return RGBToHex(lerpColor([255,255,0], maxSpeedColor, u*2-1));

}

function hexToRGB(hex) {
    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? [
        parseInt(result[1], 16),
        parseInt(result[2], 16),
        parseInt(result[3], 16)
    ] : null;
}

function RGBToHex(rgb) {
    return "#" + ((1 << 24) + (rgb[0] << 16) + (rgb[1] << 8) + rgb[2]).toString(16).slice(1);
}

function getNextSpeed(prev) {
	for(var i=0; i<waypoints.length-1; i++) {
		if(waypoints[i] == prev)
			return waypoints[i+1].speed;
	}
	return 0;
}
