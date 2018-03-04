/*
 * Copyright 2018 Iterable
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iterable.formnolia

import org.scalatest.{MustMatchers, WordSpec}
import eu.timepit.refined._
import eu.timepit.refined.api._
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric._
import play.api.data.FormError
import SafeForms._

// these imports are required but may show as unused
import be.venneborg.refined.play.RefinedForms._
import play.api.data.format.Formats._

class SafeFormsSpec extends WordSpec with MustMatchers {
  "Safe forms" must {
    "generate a mapping for a case class" in {
      case class Person(firstName: String, lastName: String, age: Int)
      val personForm = newForm[Person].bind(Map("firstName" -> "Bill", "lastName" -> "Smith", "age" -> "32"))
      personForm.errors mustBe empty
      personForm.value mustBe Some(Person("Bill", "Smith", 32))
    }
    "generate a mapping for a case class using refined types" in {
      case class Person(
        firstName: String Refined NonEmpty,
        lastName: String Refined NonEmpty,
        age: Int Refined Positive
      )
      val personForm = newForm[Person].bind(Map("firstName" -> "Bill", "lastName" -> "Smith", "age" -> "32"))
      personForm.errors mustBe empty
      personForm.value mustBe Some(
        Person(refineMV[NonEmpty]("Bill"), refineMV[NonEmpty]("Smith"), refineMV[Positive](32))
      )
      val personFormWithErrors = personForm.bind(Map("firstName" -> "Bill", "lastName" -> "", "age" -> "0"))
      personFormWithErrors.errors mustBe Seq(
        FormError("lastName", Seq("error.required")),
        FormError("age", Seq("error.min.strict"), Seq("0"))
      )
    }
  }
}
