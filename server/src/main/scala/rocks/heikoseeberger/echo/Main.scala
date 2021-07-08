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

import akka.actor.{ CoordinatedShutdown, ActorSystem => ClassicSystem }
import akka.actor.CoordinatedShutdown.PhaseBeforeServiceUnbind
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.grpc.GrpcClientSettings
import akka.management.scaladsl.AkkaManagement
import java.net.InetAddress
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
import pureconfig.generic.auto.exportReader
import pureconfig.ConfigSource
import scala.concurrent.ExecutionContext

/**
  * Main actor, empty behavior. Initializes all components, e.g. the [[HttpServer]].
  */
object Main {

  sealed trait Command

  final case class Config(httpServer: HttpServer.Config)

  private final val Name = "echo"

  /**
    * Runner for echo. Creates the actor system with the [[Main]] actor.
    */
  def main(args: Array[String]): Unit = {
    val asyncLoggerName = classOf[AsyncLoggerContextSelector].getName
    if (!sys.props.get("log4j2.contextSelector").contains(asyncLoggerName))
      println(s"WARNING: system property log4j2.contextSelector not set to [$asyncLoggerName]!")

    val config        = ConfigSource.default.at(Name).loadOrThrow[Config]
    val classicSystem = ClassicSystem(Name)
    val mgmt          = AkkaManagement(classicSystem)
    val shutdown      = CoordinatedShutdown(classicSystem)

    implicit val _ec: ExecutionContext = classicSystem.dispatcher
    mgmt
      .start()
      .foreach(_ => shutdown.addTask(PhaseBeforeServiceUnbind, "stop-akka-mgmt")(() => mgmt.stop()))
    classicSystem.spawn(Main(config), "main")
  }

  def apply(config: Config): Behavior[Command] =
    Behaviors.setup { context =>
      implicit val _system: ActorSystem[_] = context.system

      HttpServer.run(config.httpServer)

      Behaviors.empty
    }
}
