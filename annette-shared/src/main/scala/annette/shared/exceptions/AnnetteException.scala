/*
 * Copyright 2018 Valery Lobachev
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

package annette.shared.exceptions

import play.api.libs.json.Json

class AnnetteException(val code: String, val params: Map[String, String] = Map.empty, val message: Option[String] = None)
    extends RuntimeException(AnnetteException.exceptionMessage(code, params, message)) {
  def this(code: String, details: String) = {
    this(code, Json.parse(Json.parse(details).as[String]).as[Map[String, String]])
  }

  def toDetails = Json.toJson(Json.toJson(params).toString()).toString()
  def toMessage = Json.toJson(params + ("code" -> code))

}

object AnnetteException {
  def exceptionMessage(code: String, params: Map[String, String] = Map.empty, message: Option[String] = None) = {
    message.getOrElse {
      val paramsList = params.map { case (k, v) => s"$k: $v" }.mkString("[", ", ", "]")
      s"$code$paramsList"
    }
  }
}
