# lms-visualisation

This project is aimed at providing useful insights into lms staging pipelines.
It provides in browser interactive visualisations of chains of transformations and helps understand what are the kind of operations each transformation is performing.

## Trace format
To create a visualisation, we first have to generate an lms execution trace. The trace is just a sequence of `TransformInfo` objects that constitute the compilation. The full model is described in [org.lmsviz.model.lms](https://github.com/Stanford-PDM/lms-visualisation/blob/master/shared/src/main/scala/org/lmsviz/model/lms.scala) :

```scala
case class TransformInfo(name: String, before: Seq[StmInfo], 
      after: Seq[StmInfo])

case class StmInfo(id: Int, repr: String, pos: Seq[SourceLocation],
      comments: Seq[String], parentId: Option[Int],
      childrens: Seq[StmInfo])
      
case class SourceLocation(file: String, line: Int, offset: Int,
      parent: Option[SourceLocation])
```

There is currently only support for JSON serialization, using [circe](https://github.com/travisbrown/circe)'s automatic decoders.



## lms support
You can automatically generate the right trace format by using: [ExportTransforms.scala](https://github.com/Stanford-PDM/virtualization-lms-core/blob/dengels-fusion/src/util/ExportTransforms.scala)


## Caveats
The javascript VM doens't provide access to the local filesystem, so there are two solutions to load the trace:

1. Create a webserver that serves the file to the page
2. Load the trace at compile time

We decided to go for the second option for now, and use a macro to load the content of the trace file as a `String` in the source of the main class.  There is a problem with this strategy however, because sbt cannot track this kind of dependency (or I haven't found how to make it). 

To solve it we created two scripts that you can run to recompile the code:

- `recompile.sh` Will check for a change in the trace file, invalidate the main file and recompile the project
- `auto_reload.sh` Can watch for changes of a file of your choice and run `recompile.sh` automatically.