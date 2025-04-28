package com.jeanwest.reader.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 *  Manages the display and interaction of different types of notification popups.
 *
 *  This class provides methods to show various popups with different configurations,
 *  including single-button, two-button, dropdown list, and text field input popups.
 *  It uses Compose's state management to control the visibility of these popups and
 *  handles button clicks and dismiss events.
 *
 *  @constructor Injects an instance of [NotificationPopupHost].
 */
class NotificationPopupHost {

    var showPopup1Button by mutableStateOf(false)
        private set
    var showPopup2Button by mutableStateOf(false)
        private set
    var showPopUpWithAButtonAndDropDownList by mutableStateOf(false)
        private set
    var showPopUpWithAButtonAndTextField by mutableStateOf(false)
        private set
    var showPopUpWith2ButtonAndDropDownList by mutableStateOf(false)
        private set

    var doneButtonTitle = ""
    var oKButtonTitle = ""
    var cancelButtonTitle = ""
    var message = ""
    var textFieldValue by mutableStateOf("")

    var onTextFieldValueChange: (value: String) -> Unit = {}
    var onTextFieldDoneButtonClick: (value: String) -> Unit = {}
    var onDropDownDoneButtonClick: (dropDownValue: String) -> Unit = {}
    var onDoneButtonClick: () -> Unit = {}
    var onCancelButtonClick: () -> Unit = {}
    var onOkButtonClick: () -> Unit = {}
    var onDismiss: () -> Unit = {
        showPopup1Button = false
        showPopup2Button = false
        showPopUpWithAButtonAndTextField = false
        showPopUpWithAButtonAndDropDownList = false
        showPopUpWith2ButtonAndDropDownList = false
    }

    var dropDownText by mutableStateOf("")
    var dropDownList = mutableListOf<String>()

    fun showPopupWithAButton(
        message: String,
        onDoneButtonClick: () -> Unit = {},
        onDismiss: () -> Unit = {},
        doneButtonTitle: String = "متوجه شدم"
    ) {
        this.message = message
        this.doneButtonTitle = doneButtonTitle
        this.onDoneButtonClick = {
            onDoneButtonClick()
            showPopup1Button = false
        }
        this.onDismiss = {
            onDismiss()
            showPopup1Button = false
            showPopup2Button = false
            showPopUpWithAButtonAndDropDownList = false
            showPopUpWithAButtonAndTextField = false
        }
        showPopup2Button = false
        showPopUpWithAButtonAndTextField = false
        showPopUpWithAButtonAndDropDownList = false
        showPopup1Button = true
        showPopUpWith2ButtonAndDropDownList = false
    }

    fun showPopupWith2Button(
        message: String,
        onOkClick: () -> Unit = {},
        onCancelClick: () -> Unit = {},
        okButtonTitle: String = "بله",
        cancelButtonTitle: String = "خیر",
        onDismiss: () -> Unit = {}
    ) {
        this.oKButtonTitle = okButtonTitle
        this.cancelButtonTitle = cancelButtonTitle
        this.onOkButtonClick = {
            onOkClick()
            showPopup2Button = false
        }
        this.onCancelButtonClick = {
            onCancelClick()
            showPopup2Button = false
        }
        this.onDismiss = {
            onDismiss()
            showPopup1Button = false
            showPopup2Button = false
        }
        this.message = message
        showPopUpWithAButtonAndTextField = false
        showPopUpWithAButtonAndDropDownList = false
        showPopup1Button = false
        showPopup2Button = true
        showPopUpWith2ButtonAndDropDownList = false
    }

    fun showPopupWitheadlineMediumButtonDropDownList(
        message: String,
        dropDownText: String,
        dropDownList: List<String>,
        onDoneClick: (value: String) -> Unit = {},
        doneButtonTitle: String = "تایید",
    ) {
        this.message = message
        this.doneButtonTitle = doneButtonTitle
        this.dropDownList.clear()
        this.dropDownList.addAll(dropDownList)
        this.dropDownText = dropDownText
        this.onDropDownDoneButtonClick = { value ->
            onDoneClick(value)
            showPopUpWithAButtonAndDropDownList = false
        }
        showPopup2Button = false
        showPopup1Button = false
        showPopUpWithAButtonAndDropDownList = true
        showPopUpWithAButtonAndTextField = false
        showPopUpWith2ButtonAndDropDownList = false
    }

    fun showPopupWith2Button1DropDownList(
        message: String,
        dropDownText: String,
        dropDownList: List<String>,
        onOkClick: (value: String) -> Unit = {},
        okButtonTitle: String,
        onCancelClick: () -> Unit,
        cancelButtonTitle: String,
    ) {
        this.message = message
        this.oKButtonTitle = okButtonTitle
        this.cancelButtonTitle = cancelButtonTitle
        this.dropDownList.clear()
        this.dropDownList.addAll(dropDownList)
        this.dropDownText = dropDownText
        this.onDropDownDoneButtonClick = { value ->
            onOkClick(value)
            showPopUpWith2ButtonAndDropDownList = false
        }
        this.onCancelButtonClick = {
            onCancelClick()
            showPopUpWith2ButtonAndDropDownList = false
        }
        showPopup2Button = false
        showPopup1Button = false
        showPopUpWithAButtonAndDropDownList = false
        showPopUpWith2ButtonAndDropDownList = true
        showPopUpWithAButtonAndTextField = false
    }

    fun showPopUpWithAButtonAndTextField(
        message: String,
        value: String,
        onDoneClick: (value: String) -> Unit = {},
        doneButtonTitle: String = "تایید",
    ) {

        this.textFieldValue = value
        this.message = message
        this.doneButtonTitle = doneButtonTitle
        this.onTextFieldValueChange = {
            textFieldValue = it
        }
        this.onTextFieldDoneButtonClick = {
            onDoneClick(textFieldValue)
            showPopUpWithAButtonAndTextField = false
        }

        showPopup2Button = false
        showPopup1Button = false
        showPopUpWithAButtonAndDropDownList = false
        showPopUpWithAButtonAndTextField = true
        showPopUpWith2ButtonAndDropDownList = false
    }
}

@Composable
fun NotificationPopUp(state: NotificationPopupHost) {

    if (state.showPopup1Button) {
        AlertDialogWithHeadlineMediumButton(
            onDismiss = { state.onDismiss() },
            title = state.message,
            btnTxt = "باشه",
            btnOnClick = { state.onDoneButtonClick() })

    } else if (state.showPopup2Button) {
        AlertDialogWith2Button(
            title = state.message,
            btnConfirm = state.oKButtonTitle,
            btnNotConfirm = state.cancelButtonTitle,
            btnNotConfirmOnClick = { state.onCancelButtonClick() },
            btnConfirmOnClick = { state.onOkButtonClick() },
            onDismiss = { state.onDismiss() }
        )
    } else if (state.showPopUpWithAButtonAndDropDownList) {
        AlertDialogWithHeadlineMediumButtonDropDownList(
            title = state.message,
            btnTxt = state.doneButtonTitle,
            btnOnClick = { state.onDropDownDoneButtonClick(state.dropDownText) },
            dropDownText = state.dropDownText,
            onDismiss = { state.onDismiss() },
            onSelectItem = {
                state.dropDownText = it
            },
            dropDownRes = state.dropDownList
        )
    } else if(state.showPopUpWithAButtonAndTextField) {

        AlertDialogWithHeadlineMediumButton1InputText(
            title = state.message,
            btnTxt = state.doneButtonTitle,
            btnOnClick = {
                state.onTextFieldDoneButtonClick(state.textFieldValue)
            },
            defaultText = state.textFieldValue,
            onValueChange = { state.onTextFieldValueChange(it) },
            onDismiss = { state.onDismiss() }
        )
    } else if(state.showPopUpWith2ButtonAndDropDownList) {
        AlertDialogWith2Button1DropDownList(
            title = state.message,
            okTitle = state.oKButtonTitle,
            cancelTitle = state.cancelButtonTitle,
            onOKClick = state.onDropDownDoneButtonClick,
            onCancelClick = state.onCancelButtonClick,
            dropDownText = state.dropDownText,
            onDismiss = state.onDismiss,
            dropDownRes = state.dropDownList,
        )
    }
}