/*
 * Copyright 2022 Lucas Satabin
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

package mastodon

import pureconfig._
import pureconfig.module.ip4s._
import pureconfig.generic.semiauto._

import com.comcast.ip4s.Port
import scala.concurrent.duration.FiniteDuration

case class ServerConfiguration(port: Port, mastodon: MastodonConfiguration, github: GithubConfiguration)

object ServerConfiguration {
  implicit val configReader: ConfigReader[ServerConfiguration] = deriveReader
}

case class MastodonConfiguration()

object MastodonConfiguration {
  implicit val configReader: ConfigReader[MastodonConfiguration] = deriveReader
}

case class GithubConfiguration(organization: String, project: String, refreshList: FiniteDuration)

object GithubConfiguration {
  implicit val configReader: ConfigReader[GithubConfiguration] = deriveReader
}
