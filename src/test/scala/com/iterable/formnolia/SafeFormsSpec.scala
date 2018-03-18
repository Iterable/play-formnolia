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
import play.api.libs.json.Json
import play.api.test.FakeRequest
import SafeForms._

// these imports are required but may show as unused
import be.venneborg.refined.play.RefinedForms._
import play.api.data.format.Formats._

class SafeFormsSpec extends WordSpec with MustMatchers {
  "Safe forms" must {
    "generate a mapping for a case class" in {
      case class Person(firstName: String, lastName: String, age: Int)

      val person = Person("Bill", "Smith", 32)
      val personData = Map("firstName" -> "Bill", "lastName" -> "Smith", "age" -> "32")

      val personForm = newForm[Person].bind(personData)
      personForm.errors mustBe empty
      personForm.value mustBe Some(person)

      personForm.fill(person).data mustBe personData
    }
    "generate a mapping with default values" in {
      case class Person(firstName: String, lastName: String, age: Int = 0)

      val person = Person("Bill", "Smith")
      val personData = Map("firstName" -> "Bill", "lastName" -> "Smith")
      val filledPersonData = Map("firstName" -> "Bill", "lastName" -> "Smith", "age" -> "0")

      val personForm = newForm[Person].bind(personData)
      personForm.errors mustBe empty
      personForm.value mustBe Some(person)

      personForm.fill(person).data mustBe filledPersonData
    }
    "generate a mapping for a case class using refined types" in {
      case class Person(
        firstName: String Refined NonEmpty,
        lastName: String Refined NonEmpty,
        age: Int Refined Positive
      )

      val personData = Map("firstName" -> "Bill", "lastName" -> "Smith", "age" -> "32")
      val person = Person(refineMV[NonEmpty]("Bill"), refineMV[NonEmpty]("Smith"), refineMV[Positive](32))

      val personForm = newForm[Person].bind(personData)
      personForm.errors mustBe empty
      personForm.value mustBe Some(person)
      newForm[Person].fill(person).data mustBe personData

      val personFormWithErrors = personForm.bind(Map("firstName" -> "Bill", "lastName" -> "", "age" -> "0"))
      personFormWithErrors.errors mustBe Seq(
        FormError("lastName", Seq("error.required")),
        FormError("age", Seq("error.min.strict"), Seq("0"))
      )
    }
    "generate a mapping with a sealed trait" in {
      sealed trait Role
      case object User extends Role
      case object Admin extends Role

      case class Person(firstName: String, lastName: String, age: Int, role: Option[Role] = None)

      // Workaround for compilation issue on Scala 2.11, similar to https://github.com/circe/circe/issues/639
      implicit val roleMapping = genMapping[Role]
      assert(roleMapping != null)

      val nonePerson = Person("Bobby", "Tables", 17)
      val nonePersonData = Map("firstName" -> "Bobby", "lastName" -> "Tables", "age" -> "17")
      val formWithNone = newForm[Person].bind(nonePersonData)
      formWithNone.errors mustBe empty
      formWithNone.value mustBe Some(nonePerson)
      formWithNone.fill(nonePerson).data mustBe nonePersonData

      val userPerson = Person("Bill", "Smith", 42, Some(User))
      val userPersonData = Map("firstName" -> "Bill", "lastName" -> "Smith", "age" -> "42", "role.type" -> "User")
      val formWithUser = newForm[Person].bind(userPersonData)
      formWithUser.errors mustBe empty
      formWithUser.value mustBe Some(userPerson)
      formWithUser.fill(userPerson).data mustBe userPersonData

      val adminPerson = Person("Alice", "Rodriguez", 39, Some(Admin))
      val adminPersonData =
        Map("firstName" -> "Alice", "lastName" -> "Rodriguez", "age" -> "39", "role.type" -> "Admin")
      val formWithAdmin = newForm[Person].bind(adminPersonData)
      formWithAdmin.errors mustBe empty
      formWithAdmin.value mustBe Some(adminPerson)
      formWithAdmin.fill(adminPerson).data mustBe adminPersonData
    }

    "generate a mapping with a list" in {
      sealed trait Pet {
        def name: String
      }

      case class Dog(name: String) extends Pet
      case class Cat(name: String) extends Pet
      case class Parrot(name: String) extends Pet
      case class Hamster(name: String) extends Pet

      implicit val petMapping = genMapping[Pet]
      assert(petMapping != null)

      case class Person(firstName: String, lastName: String, age: Int, pets: List[Pet] = List.empty)

      val value = Person("Bill", "Smith", 42, List(Cat("Oliver"), Dog("Wilson")))
      val data = Map(
        "firstName" -> "Bill",
        "lastName" -> "Smith",
        "age" -> "42",
        "pets[0].type" -> "Cat",
        "pets[0].value.name" -> "Oliver",
        "pets[1].type" -> "Dog",
        "pets[1].value.name" -> "Wilson"
      )

      val form = newForm[Person].bind(data)
      form.errors mustBe empty
      form.value mustBe Some(value)
      form.fill(value).data mustBe data
    }

    "bind a complex form from a request with a JSON body" in {
      sealed trait Role
      case object User extends Role
      case object Admin extends Role

      case class Person(firstName: String, lastName: String, age: Int, role: Option[Role] = None)

      // Workaround for compilation issue on Scala 2.11, similar to https://github.com/circe/circe/issues/639
      implicit val roleMapping = genMapping[Role]
      assert(roleMapping != null)

      {
        val person = Person("Bobby", "Tables", 17, Some(User))
        implicit val request = FakeRequest("GET", "/foo").withBody(
          Json.obj("firstName" -> "Bobby", "lastName" -> "Tables", "age" -> 17, "role" -> Json.obj("type" -> "User"))
        )
        val form = newForm[Person].bindFromRequest()
        form.errors mustBe empty
        form.value mustBe Some(person)
      }

      {
        val person = Person("Robert", "Chairs", 42, Some(Admin))
        implicit val request = FakeRequest("GET", "/foo").withBody(
          Json.obj("firstName" -> "Robert", "lastName" -> "Chairs", "age" -> 42, "role.type" -> "Admin")
        )
        val form = newForm[Person].bindFromRequest()
        form.errors mustBe empty
        form.value mustBe Some(person)
      }
    }
  }
}
