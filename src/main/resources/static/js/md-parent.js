$.i18n.properties({
	name: 'messages',
	path: '/js/i18n/',
	mode: 'both'
});

/**
 * Helper function to prompt the user for a blog name, 
 * and then use that name to create a new blog.
 */
function createNewBlog() {
    var blogName = prompt($.i18n.prop('md_createBlog_prompt'));
    if((blogName === null) || (blogName.length < 1)) {
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

/**
 * Sets up the click handler for the 'new blog' button, 
 * and, if there are no existing blogs, prompts the user 
 * to create one.
 */
$(document).ready(function() {
	$('#newBlogButton').click(function() {
		createNewBlog();
	});
	
	if(noBlogsCreated) {
		createNewBlog();
	}
		
});

