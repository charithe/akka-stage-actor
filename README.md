Dynamic Akka Streams Using Stage Actors
=======================================

Example project to illustrate how Akka Streams pipelines can be dynamically updated in-situ using stage actors.

This project implements a a very rudimentary `GraphStage` to produce a set of tags based on keywords found in the input string.

Keywords and associated tags can be updated whilst the pipeline is running by sending messages to the `StageCoordinator` actor.
By implementing Distributed PubSub or a simple HTTP server actor, the system can be dynamically updated by users without requiring a complete restart of the application.


See tests for complete example.

