var messageCounter = 0;
var TIMEOUT_VALUE = 10000;

/**
 * Initializes the containing page by finding the "messageAreas" div on the
 * containing page, and adding content holders for informational and error
 * messages. They are not shown unless and until they are needed.
 */
$(document).ready(function() {
	var messageAreas = "<div class='ui-widget' id='infoMessageContent' style='display:none;'>"
		+ "<div class='ui-state-highlight ui-corner-all' id='infoMessageText'>"
		+ "</div>"
		+ "</div>"
		+ "<div class='ui-widget' id='errorMessageContent' style='display:none;'>"
		+ "<div class='ui-state-error ui-corner-all' id='errorMessageText'>"
		+ "</div>"
		+ "</div>";

	$(messageAreas).appendTo('#messageAreas');
});

/**
 * Creates an informational message in a message area (wherever the containing
 * page put it), using Themeroller styles. Once the message has been created, a
 * timer is set to remove it again; when messages are removed, the code checks
 * to see if the container is now empty (in which case it is hidden).
 * 
 * @param message
 *            The message to be displayed
 */
function createAnInfoMessage(message) {
	$('#infoMessageContent').show();
	messageCounter++;
	var messageID = messageCounter;
	var divData = "<div id='msg" + messageID + "'><span class='ui-icon ui-icon-info'></span>" + message + "</div>";
	$('#infoMessageText').append($(divData));
	
	var removeMsg = function() {
		$('#msg' + messageID).fadeOut().remove();
		var remainingMessages = $('#infoMessageText').children();
		if(remainingMessages.length < 1) {
			$('#infoMessageContent').hide();
		}
	};
	
	setTimeout(removeMsg, TIMEOUT_VALUE);
}

/**
 * Creates an error message in a message area (wherever the containing page put
 * it), using Themeroller styles. Once the message has been created, a timer is
 * set to remove it again; when messages are removed, the code checks to see if
 * the container is now empty (in which case it is hidden).
 * 
 * @param message
 *            The message to be displayed
 */
function createAnErrorMessage(message) {
	$('#errorMessageContent').show();
	messageCounter++;
	var messageID = messageCounter;
	var divData = "<div id='msg" + messageID + "'><span class='ui-icon ui-icon-alert'></span>" + message + "</div>";
	$('#errorMessageText').append($(divData));
	
	var removeMsg = function() {
		$('#msg' + messageID).fadeOut().remove();
		var remainingMessages = $('#errorMessageText').children();
		if(remainingMessages.length < 1) {
			$('#errorMessageContent').hide();
		}
	};
	
	setTimeout(removeMsg, TIMEOUT_VALUE);
}