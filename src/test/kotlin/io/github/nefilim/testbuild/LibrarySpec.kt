package io.github.nefilim.testbuild

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import mu.KotlinLogging

class LibrarySpec: WordSpec() {

    private val logger = KotlinLogging.logger {}

    init {
        "library" should {
            "do the right thing" {
                doesSomething() shouldBe "hello world"
            }
        }
    }
}