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
 * @returns
 */
function addOptionToSelect(optionValue, selectName, optionText) {
	var optionData = "<option value='" + optionValue + "'>" + optionText
			+ "</option>";
	$(optionData).appendTo("#" + selectName);
}