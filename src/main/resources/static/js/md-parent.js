$.i18n.properties({
	name: 'messages',
	path: 'js/i18n/',
	mode: 'both'
});

$(document).ready(function() {
	$('#newBlogButton').click(function() {
		var blogName = prompt("Image Output Path:");
		if((blogName == null) || (blogName.length < 1)) {
			//TODO i18n
			createAnErrorMessage("please enter the name for the blog, from Tumblr");
			return;
		}
		
		$.ajax({
			url: '/api/metadata/byBlog/' + blogName + '/orDefault',
			method: 'GET'
		}).then(function(data) {
			location.reload();
		});
	});
		
});