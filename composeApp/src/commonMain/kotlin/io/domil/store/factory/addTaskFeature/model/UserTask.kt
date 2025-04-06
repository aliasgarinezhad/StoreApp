package io.domil.store.factory.addTaskFeature.model

data class UserTask(
    val startHour: Int = 0,
    val endHour: Int = 0,
    val startMinute: Int = 0,
    val endMinute: Int = 0,
    val product: Product = Product(),
    val task: String = "",
    val taskId: Long = 0L,
    val size: String = "",
    val sizeCode : Int = 0,
    val number: Int = 0,
    val machineCode: Int = 0
)
