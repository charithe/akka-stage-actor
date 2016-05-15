/*
 * Copyright 2016 Charith Ellawala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.charithe.akka.streams

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.github.charithe.akka.streams.StageCoordinator.{Register, TagCommand}

import scala.collection.mutable

class StageCoordinator extends Actor with ActorLogging {
  val stageActors: mutable.Set[ActorRef] = mutable.HashSet.empty

  override def receive = {
    case Register(actorRef) => {
      log.info("Registering stage actor [{}]", actorRef)
      stageActors.add(actorRef)
      context.watch(actorRef)
    }

    case Terminated(actorRef) => {
      log.info("Stage actor terminated [{}]", actorRef)
      stageActors.remove(actorRef)
      context.unwatch(actorRef)
    }

    case cmd: TagCommand => {
      log.info("Received command: [{}]", cmd)
      stageActors.foreach(_ ! cmd)
    }
  }
}

object StageCoordinator {

  final case class Register(actorRef: ActorRef)

  sealed trait TagCommand extends Product with Serializable

  final case class AddKeywordTags(keyword: String, tags: Set[String]) extends TagCommand

  final case class RemoveKeyword(keyword: String) extends TagCommand

  def props = Props[StageCoordinator]

}