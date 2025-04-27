package com.jeanwest.reader.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A class to manage and display different types of notification popups.
 * This class uses mutable state to control the visibility and content of the popups,
 * allowing for reactive updates in a UI.
 */
class NotificationPopupHost {

    var showPopup1Button by mutableStateOf(false)
        private set
    var showPopup2Button by mutableStateOf(false)
    var showPopUpWithAButtonAndDropDownList by mutableStateOf(false)
        private set
    var showPopUpWithAButtonAndTextField by mutableStateOf(false)
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
    }

    fun showPopupWith1ButtonDropDownList(
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
    }
}

@Composable
fun NotificationPopUp(state: NotificationPopupHost) {

    if (state.showPopup1Button) {
        AlertDialogWith1Button(
            onDismiss = { state.onDismiss() },
            title = state.message,
            btnTxt = "باشه",
            btnOnClick = { state.onDoneButtonClick() })

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogWith1Button(
    title: String,
    btnTxt: String,
    btnOnClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        content = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceAround
            ) {

                Text(
                    text = title,
                    modifier = Modifier.padding(bottom = 10.dp),
                    fontSize = 18.sp
                )

                Button(
                    onClick = { btnOnClick() },
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .align(Alignment.CenterHorizontally)
                        .testTag("alertBtn")
                ) {
                    Text(text = btnTxt)
                }
            }
        }
    )
}
