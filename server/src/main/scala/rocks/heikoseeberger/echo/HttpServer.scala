/*
 * Copyright 2021 Heiko Seeberger
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

package rocks.heikoseeberger.echo

import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration.FiniteDuration
import scala.util.{ Failure, Success }

/**
  * HTTP server. "rocks.heikoseeberger.echo.HttpServer$ReadinessCheck" can be configured as
  * readiness check (Akka Management).
  */
object HttpServer extends Logging {

  final case class Config(interface: String, port: Int, terminationDeadline: FiniteDuration)

  final class ReadinessCheck extends (() => Future[Boolean]) {
    override def apply(): Future[Boolean] =
      ready.future
  }

  private object BindFailure extends CoordinatedShutdown.Reason

  private val ready = Promise[Boolean]()

  def run(config: Config)(implicit system: ActorSystem[_]): Unit = {
    import config._
    import system.executionContext

    Http()
      .newServerAt(interface, port)
      .bind(grpc.EchoServiceHandler(new EchoServiceImpl))
      .onComplete {
        case Failure(cause) =>
          logger.error(s"Shutting down, because cannot bind to $interface:$port!", cause)
          CoordinatedShutdown(system).run(BindFailure)

        case Success(binding) =>
          logger.info(s"Listening to HTTP connections on ${binding.localAddress}")
          ready.success(true)
          binding.addToCoordinatedShutdown(terminationDeadline)
      }
  }
}
