package com.jeanwest.reader.useCases

/**
 * Converts a file name string to an integer representation of its version, if the file name conforms to a specific format.
 *
 * This function expects a file name to follow the pattern "app-debug-`<version>`\.apk", where `<version>` is a string
 * representing the version number, potentially containing dots ('.') as separators.  The function extracts the
 * version string, removes the dots, and attempts to convert the remaining characters to an integer.  If the conversion
 * is successful, that integer is returned; otherwise, `null` is returned.
 *
 * **Example:**
 * ```kotlin
 * val fileName1 = "app-debug-1.2.3.apk"
 * val version1 = fileName1.fileNameToVersionIntFormat() // version1 will be 123
 *
 * val fileName2 = "app-debug-10.apk"
 * val version2 = fileName2.fileNameToVersionIntFormat() // version2 will be 10
 *
 * val fileName3 = "invalid_file_name.apk"
 * val version3 = fileName3.fileNameToVersionIntFormat() // version3 will be null
 * ```
 *
 * **Return Value:**
 * An integer representing the version number extracted from the file name if the file name matches the expected format and the conversion is successful.  `null` otherwise.
 *
 * **Constraints:**
 * The file name must:
 * 1. Be at least 19 characters long.
 * 2. Start with "app-debug-".
 * 3. End with ".apk".
 */
fun String.fileNameToVersionIntFormat(): Int? {

    var versionIntFormat: Int? = null

    if (this.length >= 19 && this.startsWith("app-debug-") && this.endsWith(".apk")) {

        this.substringAfter("app-debug-").substringBefore(".apk").filter { versionCharacters ->
            versionCharacters != '.'
        }.toIntOrNull()?.also {
            versionIntFormat = it
        }
    }

    return versionIntFormat
}

/**
 * Converts a version string (e.g., "1.2.3") to an integer representation (e.g., 123).
 *
 * The function expects a version string with at least 5 characters and containing at least two separators (dots).  It removes all dots and attempts to parse the remaining string as an integer.  If the string cannot be parsed as an integer or does not meet the minimum length requirement, the function returns `null`.
 *
 * Example:
 *  "1.2.3" -> 123
 *  "10.11.12" -> 101112
 *  "1.2" -> null (because it has less than 5 characters when dots are removed)
 *  "1.2.3.4" -> 1234
 *  "1.a.b" -> null (because it contains non-numeric characters after dot removal)
 *
 * @return The integer representation of the version string, or `null` if the conversion fails.
 */
fun String.versionToVersionIntFormat(): Int? {

    var versionIntFormat: Int? = null

    if (this.length >= 5) {

        this.filter { versionCharacters ->
            versionCharacters != '.'
        }.toIntOrNull()?.also {
            versionIntFormat = it
        }
    }

    return versionIntFormat
}

/**
 * Extracts the version string from an APK filename.
 *
 * The function assumes the filename follows the pattern "app-debug-{version}.apk".
 * It extracts the substring between "app-debug-" and ".apk" as the version.
 *
 * @return The version string if the filename matches the expected pattern, otherwise null.
 *
 * Example:
 * ```
 * val filename = "app-debug-1.2.3-SNAPSHOT.apk"
 * val version = filename.fileNameToVersion() // version will be "1.2.3-SNAPSHOT"
 *
 * val invalidFilename = "myApp.apk"
 * val noVersion = invalidFilename.fileNameToVersion() // noVersion will be null
 * ```
 */
fun String.fileNameToVersion(): String? {

    var version: String? = null

    if (this.length >= 19 && this.startsWith("app-debug-") && this.endsWith(".apk")) {

        this.substringAfter("app-debug-").substringBefore(".apk").also {
            version = it
        }
    }

    return version
}