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

import akka.actor.ActorRef
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}

import scala.collection.mutable
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

class TagStage(stageCoordinator: ActorRef, initialKeywordsMap: Map[String, Set[String]]) extends GraphStage[FlowShape[String, Set[String]]] {
  val in: Inlet[String] = Inlet("tag-stage-in")
  val out: Outlet[Set[String]] = Outlet("tag-stage-out")

  override def shape: FlowShape[String, Set[String]] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =  new GraphStageLogic(shape) {
    implicit def self = stageActor.ref

    var keywordsMap = mutable.HashMap(initialKeywordsMap.toSeq:_*)

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        // Grab the next message from the inlet
        val msg = grab(in)
        // Tokenize the message and find all applicable tags
        val tags = msg.split("\\W+").collect {
          case flaggedWord if keywordsMap.contains(flaggedWord) => keywordsMap(flaggedWord)
        }.flatten

        if(tags.isEmpty){
          // if there are no tags to output, request next item from upstream
          tryPull(in)
        } else {
          // push the tags downstream
          push(out, Set(tags:_*))
        }
      }
    })

    setHandler(out, new OutHandler {
      // Request next item from upstream
      override def onPull(): Unit = tryPull(in)
    })

    override def preStart(): Unit = {
      // Register the stage actor with the coordinator actor
      val thisStageActor = getStageActor(messageHandler).ref
      stageCoordinator ! StageCoordinator.Register(thisStageActor)
    }

    private def messageHandler(receive: (ActorRef, Any)): Unit = {
      receive match {
        case (_, StageCoordinator.AddKeywordTags(keyword, tags)) => {
          // Add a new set of tags for the keyword
          val newTags = keywordsMap.get(keyword).map(_ ++ tags).getOrElse(tags)
          keywordsMap.update(keyword, newTags)
        }

        case (_, StageCoordinator.RemoveKeyword(keyword)) => {
          // Remove the keyword
          keywordsMap.remove(keyword)
        }
      }
    }
  }
}
