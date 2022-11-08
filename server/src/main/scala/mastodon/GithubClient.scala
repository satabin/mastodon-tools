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

import cats.effect._
import cats.syntax.all._
import fs2.data.csv._
import fs2.data.csv.generic.semiauto._
import fs2.data.text.utf8._
import mau._
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3._
import io.circe.generic.semiauto._

import java.time.LocalDate

case class BlockedDomain(domain: String, reason: String, since: LocalDate)
object BlockedDomain {
  implicit val csvDecoder = deriveCsvRowDecoder[BlockedDomain]
  implicit val jsonEncoder = deriveEncoder[BlockedDomain]
}

class GithubClient[F[_]] private (config: GithubConfiguration,
                                  backend: SttpBackend[F, Fs2Streams[F]],
                                  blocked: RefreshRef[F, List[BlockedDomain]])(implicit F: Async[F]) {

  def blockedDomains(reasons: List[String]): F[List[BlockedDomain]] =
    blocked
      .getOrFetch(config.refreshList)(fetchBlockedDomains)
      .map(blocked =>
        if (reasons.isEmpty) blocked
        else blocked.filter(b => reasons.contains(b.reason)))

  private def fetchBlockedDomains: F[List[BlockedDomain]] =
    basicRequest
      .get(
        uri"https://raw.githubusercontent.com/${config.organization}/${config.project}/main/lists/blocked_domains.csv")
      .response(asStream(Fs2Streams[F])(_.through(decodeUsingHeaders[BlockedDomain]()).compile.toList))
      .send(backend)
      .flatMap {
        case Response(Right(list), _, _, _, _, _) => F.pure(list)
        case Response(Left(error), _, _, _, _, _) =>
          F.raiseError(new Exception(s"could not fetch blocked list because: $error"))
      }

}

object GithubClient {

  def apply[F[_]: Async](config: GithubConfiguration,
                         backend: SttpBackend[F, Fs2Streams[F]]): Resource[F, GithubClient[F]] =
    RefreshRef.resource[F, List[BlockedDomain]].map(new GithubClient(config, backend, _))

}
