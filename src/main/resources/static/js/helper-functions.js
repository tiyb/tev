/**
 * Called to add an item (an 'option' to a drop-down widget)
 * 
 * @param optionValue
 *            The value for the item; its underlying "code value"
 * @param selectName
 *            The drop-down object to which the item should be added
 * @param optionText
 *            The value that should be shown for the item (only used if
 *            translationFunction is null)
 */
function addOptionToSelect(optionValue, selectName, optionText) {
	var optionData = "<option value='" + optionValue + "'>" + optionText
			+ "</option>";
	$(optionData).appendTo("#" + selectName);
}

/**
 * Function to get the current blog's name, either via the tempBlogName request
 * param (if present), or via back-end API call
 * 
 * @returns The name of the currently viewed blog
 */
function getCurrentBlogName() {
	var urlParams = new URLSearchParams(window.location.search);
	if (urlParams.has("tempBlogName")) {
		return urlParams.get("tempBlogName");
	} else {
		return $.ajax({
			url : '/api/metadata/default/blogName',
			type : 'GET',
			async : false
		}).responseText;
	}
}