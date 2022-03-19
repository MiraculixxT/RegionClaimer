package de.miraculixx.regionClaimer.claiming

enum class CreateStep(s: String) {
    FIRST_POSITION("First Position"),
    SECOND_POSITION("Second Position"),
    NAME("Region Name"),
    FINISH("Bestätigen");

    val title = s
}