function validateRequired(object) {
	return object.val().trim().length > 0;
}

function formatDate(dt) {
	return dt.getFullYear() + "-" + pad2(1 + dt.getMonth()) + "-"
			+ pad2(dt.getDate()) + "+" + pad2(dt.getHours()) + ":"
			+ pad2(dt.getMinutes()) + ":" + pad2(dt.getSeconds());
}

function pad2(number) {
	var text = "" + number;
	if (text.length == 0) {
		return "00";
	} else if (text.length == 1) {
		return "0" + text;
	} else {
		return text;
	}
}
