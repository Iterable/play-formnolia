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

import magnolia._
import play.api.data._
import play.api.data.format.Formatter
import play.api.data.validation.Constraint

import scala.language.experimental.macros

object SafeForms {
  type Typeclass[T] = Mapping[T]

  def newForm[T](implicit mapping: Mapping[T]): Form[T] = Form(mapping)

  implicit def fieldMapping[T](implicit formatter: Formatter[T]): Mapping[T] = FieldMapping()

  implicit def gen[T]: Mapping[T] = macro Magnolia.gen[T]

  private[formnolia] def combine[T](ctx: CaseClass[Mapping, T]): Mapping[T] =
    new CaseClassMapping[T](ctx)

  private[formnolia] def dispatch[T](ctx: SealedTrait[Mapping, T]): Mapping[T] =
    throw new NotImplementedError(s"Cannot generate mapping for ${ctx.typeName}")
}

private[formnolia] case class CaseClassMapping[T](
  ctx: CaseClass[Mapping, T],
  mappings: Seq[Mapping[_]] = Seq.empty,
  constraints: Seq[Constraint[T]] = Seq.empty,
  key: String = ""
) extends Mapping[T]
    with ObjectMapping {

  override def bind(data: Map[String, String]): Either[Seq[FormError], T] = {
    val boundParams = ctx.parameters.map { param =>
      val paramMapping = param.typeclass.withPrefix(param.label).withPrefix(key)
      paramMapping.bind(data)
    }
    merge(boundParams: _*).right.map(ctx.rawConstruct).right.flatMap(applyConstraints)
  }

  override def unbind(value: T): Map[String, String] = {
    val unboundParams = ctx.parameters.map { param =>
      val paramMapping = param.typeclass.withPrefix(param.label).withPrefix(key)
      val paramValue = param.dereference(value)
      paramMapping.unbind(paramValue)
    }
    unboundParams.foldLeft(Map.empty[String, String])(_ ++ _)
  }

  override def unbindAndValidate(value: T): (Map[String, String], Seq[FormError]) = {
    val unboundParams = ctx.parameters.map { param =>
      val paramMapping = param.typeclass.withPrefix(param.label).withPrefix(key)
      val paramValue = param.dereference(value)
      paramMapping.unbindAndValidate(paramValue)
    }
    unboundParams.foldLeft((Map.empty[String, String], Seq.empty[FormError])) {
      case ((data, errors), (paramData, paramErrors)) =>
        (data ++ paramData, errors ++ paramErrors)
    }
  }

  override def verifying(newConstraints: Constraint[T]*): Mapping[T] =
    copy(constraints = constraints ++ newConstraints)

  override def withPrefix(prefix: String): Mapping[T] =
    addPrefix(prefix).map(newKey => copy(key = newKey)).getOrElse(this)
}
