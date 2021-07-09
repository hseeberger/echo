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

import scala.concurrent.Future
import version.BuildInfo

final class EchoServiceImpl extends grpc.EchoService {

  override def echo(request: grpc.EchoRequest): Future[grpc.EchoResponse] =
    Future.successful(grpc.EchoResponse(request.text))

  override def version(in: grpc.VersionRequest): Future[grpc.VersionResponse] =
    Future.successful(grpc.VersionResponse(BuildInfo.version, BuildInfo.scalaVersion))
}
