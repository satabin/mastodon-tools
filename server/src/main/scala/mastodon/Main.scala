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

import cats.data.NonEmptyList
import cats.effect._
import com.comcast.ip4s._
import fs2.data.csv._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.headers._
import org.http4s.implicits._
import org.typelevel.ci._
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._
import sttp.client3.armeria.fs2.ArmeriaFs2Backend

object Main extends IOApp.Simple {

  object ReasonsQueryParamMatcher extends OptionalMultiQueryParamDecoderMatcher[String]("reasons")

  implicit val stringEncoder: RowEncoder[String] = RowEncoder.instance(NonEmptyList.one(_))

  private def service(github: GithubClient[IO]) =
    HttpRoutes
      .of[IO] {
        case GET -> Root / "blocked_domains" :? ReasonsQueryParamMatcher(reasons) =>
          github.blockedDomains(reasons.getOrElse(Nil)).flatMap(blocked => Ok(blocked.asJson))
        case GET -> Root / "blocked_domains" ~ "csv" :? ReasonsQueryParamMatcher(reasons) =>
          github
            .blockedDomains(reasons.getOrElse(Nil))
            .flatMap(blocked =>
              Ok(fs2.Stream.emits(blocked).map(_.domain).through(encodeWithoutHeaders()).compile.string)
                .map(_.withContentType(`Content-Type`(mediaType"text/csv")).putHeaders(
                  `Content-Disposition`("attachment", Map(ci"filename" -> "blocked_domains.csv")))))
        case GET -> Root / "blocked_domains" / "reasons" =>
          github.blockedDomains(Nil).flatMap(blocked => Ok(blocked.map(_.reason).distinct.sorted.asJson))
      }
      .orNotFound

  private val server =
    for {
      config <- Resource.eval(ConfigSource.default.loadF[IO, ServerConfiguration]())
      backend <- ArmeriaFs2Backend.resource[IO]()
      github <- GithubClient(config.github, backend)
      server <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(config.port)
        .withHttpApp(service(github))
        .build
    } yield server

  override def run: IO[Unit] =
    server.use(_ => IO.never)

}
