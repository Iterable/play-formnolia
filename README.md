# play-formnolia

[![Travis CI](https://travis-ci.org/Iterable/play-formnolia.svg?branch=master)](https://travis-ci.org/Iterable/play-formnolia) [![Maven](https://img.shields.io/maven-central/v/com.iterable/play-formnolia_2.12.svg)](https://mvnrepository.com/artifact/com.iterable/play-formnolia_2.12)

A project to automatically generate and validate Play forms using [Magnolia](https://magnolia.work/).

## Example

```scala
// Add the required imports:
import com.iterable.formnolia.SafeForms._
import play.api.data.format.Formats._

// Create a case class representing the form:
case class UserData(
  username: String,
  firstName: String,
  lastName: String,
  avatarUrl: Option[String]
)

// Generate the form and bind it in a Play action (or elsewhere).
newForm[UserData].bindFromRequest().fold(
  { formWithErrors =>
    BadRequest(s"Errors: ${formWithErrors.errors}")
  },
  { userData =>
    Ok(s"Got $userData")
  }
)

```
