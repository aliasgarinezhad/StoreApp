package com.jeanwest.reader.models

/**
 * Represents a logistic entity with a name, destination, a list of items, and the total number of items.
 *
 * @property name The name of the logistic entity.  Defaults to an empty string.
 * @property destination The destination of the logistic entity. Defaults to an empty string.
 * @property items A mutable list of strings representing the items associated with the logistic entity. Defaults to an empty mutable list.
 * @property numberOfItems The total number of items associated with the logistic entity. Defaults to 0.  Should ideally be kept synchronized with the size of the `items` list, but this is not enforced by the class itself.  Consider adding a setter that updates both properties if stricter consistency is required.
 */
class Logistic (
    var name: String = "",
    var destination: String = "",
    var items: MutableList<String> = mutableListOf(),
    var numberOfItems: Int = 0
)