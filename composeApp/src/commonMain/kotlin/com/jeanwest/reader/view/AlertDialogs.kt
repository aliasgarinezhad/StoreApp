@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.store


@Composable
fun AlertDialogWith2Button(
    title: String,
    btnConfirm: String,
    btnNotConfirm: String,
    btnNotConfirmOnClick: () -> Unit,
    btnConfirmOnClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        onDismissRequest = {
            onDismiss()
        },
        content = {
            Column(
                verticalArrangement = Arrangement.SpaceAround
            ) {

                Text(
                    text = title,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 18.sp
                )

                Row(horizontalArrangement = Arrangement.SpaceAround) {

                    OutlinedButton(
                        onClick = {
                            btnNotConfirmOnClick()
                        }, modifier = Modifier
                            .padding(top = 12.dp, end = 16.dp)
                            .testTag("notConfirm")
                    )
                    {
                        Text(
                            text = btnNotConfirm,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Button(
                        onClick = {
                            btnConfirmOnClick()
                        },
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .testTag("confirm")
                    ) {
                        Text(
                            text = btnConfirm,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun AlertDialogWith2ButtonAndAppVersion(
    title: String,
    appVersion: String,
    btnConfirm: String,
    btnNotConfirm: String,
    btnNotConfirmOnClick: () -> Unit,
    btnConfirmOnClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),

        onDismissRequest = {
            onDismiss()
        },
        content = {
            Column(
                verticalArrangement = Arrangement.SpaceAround
            ) {

                Text(
                    text = title,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 18.sp
                )
                Text(
                    text = "ورژن برنامه: $appVersion",
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 18.sp
                )

                Row(horizontalArrangement = Arrangement.SpaceAround) {

                    OutlinedButton(
                        onClick = {
                            btnNotConfirmOnClick()
                        }, modifier = Modifier
                            .padding(top = 12.dp, end = 16.dp)
                            .testTag("notConfirm")
                    )
                    {
                        Text(
                            text = btnNotConfirm,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = {
                            btnConfirmOnClick()
                        },
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .testTag("confirm")
                    ) {
                        Text(
                            text = btnConfirm,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun AlertDialogWithHeadlineMediumButton(
    title: String,
    btnTxt: String,
    btnOnClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        onDismissRequest = {
            onDismiss()
        },
        content = {

            Column(

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
                    Text(
                        text = btnTxt,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}

@Composable
fun AlertDialogWithHeadlineMediumButton1InputText(
    title: String,
    btnTxt: String,
    btnOnClick: () -> Unit,
    defaultText: String,
    onDismiss: () -> Unit,
    onValueChange: (it: String) -> Unit,
) {
    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        content = {
            Column {
                Text(
                    text = title, modifier = Modifier
                        .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                )
                OutlinedTextField(
                    value = defaultText, onValueChange = {
                        onValueChange(it)
                    },
                    modifier = Modifier
                        .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                        .align(Alignment.CenterHorizontally)
                        .testTag("alertDialogInput")
                )

                Button(
                    modifier = Modifier
                        .padding(
                            bottom = 12.dp,
                            top = 12.dp,
                            start = 12.dp,
                            end = 12.dp
                        )
                        .align(Alignment.CenterHorizontally)
                        .testTag("alertBtn"),
                    onClick = {
                        btnOnClick()
                    }) {
                    Text(
                        text = btnTxt,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        onDismissRequest = {
            onDismiss()
        }
    )
}

@Composable
fun AlertDialogWithHeadlineMediumButtonDropDownList(
    title: String,
    btnTxt: String,
    btnOnClick: () -> Unit,
    dropDownText: String,
    onDismiss: () -> Unit,
    dropDownRes: MutableList<String>,
    onSelectItem: (item: String) -> Unit,
) {

    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        onDismissRequest = {
            onDismiss()
        },
        content = {
            Column {

                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium
                )

                Row {
                    var text = dropDownText
                    FilterDropDownList(
                        modifier = Modifier
                            .padding(top = 24.dp, end = 16.dp),
                        icon = {},
                        text = {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        onClick = {
                            text = it
                            onSelectItem(it)
                        },
                        values = dropDownRes
                    )

                    Button(
                        onClick = {
                            btnOnClick()
                        }, modifier = Modifier
                            .padding(top = 24.dp)
                            .align(Alignment.CenterVertically)
                            .testTag("alertBtn")
                    ) {
                        Text(
                            text = btnTxt,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun AlertDialogWith2ButtonDropDownList(
    title: String,
    btnTxt: String,
    btnOnClick: () -> Unit,
    dropDownText: String,
    onDismiss: () -> Unit,
    dropDownRes: List<String>,
    onSelectItem: (item: String) -> Unit,
    dropDown2Text: String,
    dropDown2Res: List<String>,
    onSelectItem2: (item: String) -> Unit,
) {

    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxSize(),
        onDismissRequest = {
            onDismiss()
        },
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall
                )

                Row {
                    var text = dropDownText
                    FilterDropDownList(
                        modifier = Modifier
                            .padding(top = 24.dp, end = 16.dp),
                        icon = {},
                        text = {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        onClick = {
                            text = it
                            onSelectItem(it)
                        },
                        values = dropDownRes
                    )

                    var text2 = dropDown2Text
                    FilterDropDownList(
                        modifier = Modifier
                            .padding(top = 24.dp, end = 16.dp),
                        icon = {},
                        text = {
                            Text(
                                text = text2,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        onClick = {
                            text2 = it
                            onSelectItem2(it)
                        },
                        values = dropDown2Res
                    )
                }
                Row {
                    BigButton(
                        onClick = {
                            btnOnClick()
                        },
                        modifier = Modifier.padding(top = 32.dp),
                        text = btnTxt
                    )
                }
            }
        }
    )
}


@Composable
fun AlertDialogWith2Button1DropDownList(
    title: String,
    okTitle: String,
    cancelTitle: String,
    onOKClick: (value: String) -> Unit,
    onCancelClick: () -> Unit,
    dropDownText: String,
    onDismiss: () -> Unit,
    dropDownRes: MutableList<String>,
) {

    var selectedItem by mutableStateOf(dropDownText)

    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        onDismissRequest = {
            onDismiss()
        },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
            ) {

                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth().align(CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall
                )

                Row(horizontalArrangement = Arrangement.Center) {

                    FilterDropDownList(
                        modifier = Modifier.padding(bottom = 24.dp, top = 24.dp),
                        icon = {
                            Icon(
                                painter = painterResource(Res.drawable.store),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                                    .align(Alignment.CenterVertically).padding(start = 6.dp)
                            )
                        },
                        text = {
                            Text(
                                text = selectedItem,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .padding(start = 6.dp)
                            )
                        },
                        onClick = {
                            selectedItem = it
                        },
                        values = dropDownRes
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {

                    OutlinedButton(
                        onClick = {
                            onCancelClick()
                        },
                        modifier = Modifier.testTag("alertBtn"),
                    ) {
                        Text(text = cancelTitle, style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            onOKClick(selectedItem)
                        }, modifier = Modifier.testTag("alertBtn")
                    ) {
                        Text(text = okTitle)
                    }
                }
            }
        }
    )
}