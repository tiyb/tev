$(document).ready(function() {
	$(document).one('focus.autoExpand', 'textarea.autoExpand', function() {
		var savedValue = this.value;
		this.value = '';
		this.baseScrollHeight = this.scrollHeight;
		this.value = savedValue;
	}).on('input.autoExpand', 'textarea.autoExpand', function() {
		resizeTextArea(this);
	});
	
	$.ajax({
		url: "/stagedPostsDownload",
		dataSrc: ""
	}).then(function(data) {
		$('#exportedXMLText').val(data).trigger("input.autoExpand");
	});
});

function resizeTextArea(te) {
	var minRows = te.getAttribute('data-min-rows')|0, rows;
	te.rows = minRows;
	te.baseScrollHeight = 50;
	rows = Math.ceil((te.scrollHeight - te.baseScrollHeight) / 16);
	te.rows = minRows + rows + 1;
}