$.i18n.properties({
	name: 'messages',
	path: '/js/i18n/',
	mode: 'both'
});

$(document).ready(function() {
	$('#newBlogButton').click(function() {
		createNewBlog();
	});
	
	if(noBlogsCreated) {
		createNewBlog();
	}
		
});

function createNewBlog() {
	var blogName = prompt($.i18n.prop('md_createBlog_prompt'));
	if((blogName == null) || (blogName.length < 1)) {
		createAnErrorMessage($.i18n.prop('md_createBlog_errorMessage'));
		return;
	}
	
	$.ajax({
		url: '/api/metadata/byBlog/' + blogName + '/orDefault',
		method: 'GET'
	}).then(function(data) {
		window.location = "/metadata";
	});	
}