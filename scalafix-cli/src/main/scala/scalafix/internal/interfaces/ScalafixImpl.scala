package scalafix.internal.interfaces

import buildinfo.RulesBuildInfo
import scalafix.Versions
import scalafix.interfaces.Scalafix
import scalafix.interfaces.ScalafixArguments
import scalafix.interfaces.imports._
import scalafix.internal.v1.MainOps
import scalafix.internal.rule.OrganizeImports
import scalafix.internal.v1.Rules
import scala.collection.mutable.ArrayBuffer
import java.{util => ju}

final class ScalafixImpl extends Scalafix {

  override def toString: String =
    s"""Scalafix v${scalafixVersion()}"""

  override def newArguments(): ScalafixArguments =
    ScalafixArgumentsImpl()

  override def mainHelp(screenWidth: Int): String = {
    MainOps.helpMessage(screenWidth)
  }

  override def loadOrganizeImports2(): OrganizeImportsDirect = {
    import scala.jdk.CollectionConverters._
    import java.nio.file._

    val all = Rules.all(this.getClass.getClassLoader())
    Files.write(
      Paths.get("/home/v.chelyshov/debug-fix"),
      s"ALL: ${all.map(v => v.getClass)}\n".getBytes,
      StandardOpenOption.APPEND, StandardOpenOption.CREATE
    )
    val orgImports = all.collectFirst {
      case r: OrganizeImports => r
    }

    def refToScalameta(ref: scalafix.interfaces.imports.TermRef): scala.meta.Term.Ref =
      ref match {
        case i: Ident => scala.meta.Term.Name(i.name())
        case sel: Select =>
          val qual = refToScalameta(sel.qualifier())
          scala.meta.Term.Select(qual, scala.meta.Term.Name(sel.name()))
      }
    
    def refToInterface(ref: scala.meta.Term): scalafix.interfaces.imports.TermRef =
      ref match {
        case sel: scala.meta.Term.Select => 
          val qual = refToInterface(sel.qual)
          new scalafix.interfaces.imports.Select {
            override def qualifier(): TermRef = qual

            override def name(): String =  sel.name.value

          }
        case n: scala.meta.Term.Name =>
          new scalafix.interfaces.imports.Ident {
            override def name(): String = n.value
          }
      }
    

    Files.write(
      Paths.get("/home/v.chelyshov/debug-fix"),
      s"AAAA2: ${orgImports}\n".getBytes,
      StandardOpenOption.APPEND, StandardOpenOption.CREATE
    )
    orgImports match {
      case None =>
        new OrganizeImportsDirect {
          override def toString(): String = "Scalafix BAD INSTANCE"

          def organize(in: java.util.List[Import]): java.util.List[Import] = {
            in
          }
        }
      case Some(rule) =>
        new OrganizeImportsDirect {
          override def toString(): String = "Scalafix Org import"
          def organize(in: java.util.List[Import]): java.util.List[Import] = {
            val converted =
              in.asScala.flatMap { i =>
                i.importers().asScala.map{ importer => 
                  val importees = importer.importees().asScala.map(i => scala.meta.Importee.Name(scala.meta.Name.Indeterminate(i)))

                  scala.meta.Importer(
                    refToScalameta(importer.ref()),
                    importees.toList
                  )
                }
              }
            Files.write(
              Paths.get("/home/v.chelyshov/debug-fix"),
              s"Converted: ${converted.mkString("\n")}\n".getBytes,
              StandardOpenOption.APPEND, StandardOpenOption.CREATE
            )

            val out = rule.organizeGlobalImports2(converted.toList, ArrayBuffer.empty)
            out.flatMap { groups =>
                groups.map{ imp => 
                  val i =
                    new scalafix.interfaces.imports.Importer {
                      override def ref(): TermRef = refToInterface(imp.ref)

                      override def importees(): ju.List[String] =
                        imp.importees.collect {
                          case n: scala.meta.Importee.Name => n.name.value
                        }.asJava
                    }
                  new scalafix.interfaces.imports.Import {

                    override def importers(): ju.List[Importer] = List(i).asJava

                    override def toString(): String = s"Scalafix $imp"
                  }  
                }
              }
              .asJava  
          }
        }
    }
  }

  override def scalaVersion(): String =
    RulesBuildInfo.scalaVersion
  override def scalafixVersion(): String =
    Versions.version
  override def scalametaVersion(): String =
    Versions.scalameta
  override def supportedScalaVersions(): Array[String] =
    Versions.supportedScalaVersions.toArray
  override def scala211(): String =
    throw new java.lang.UnsupportedOperationException(
      "Scala 2.11 is no longer supported; the final version supporting it is Scalafix 0.10.4"
    )
  override def scala212(): String =
    Versions.scala212
  override def scala213(): String =
    Versions.scala213
  override def scala33(): String =
    Versions.scala33
  override def scala35(): String =
    Versions.scala35
  override def scala36(): String =
    Versions.scala36
  override def scala3LTS(): String =
    Versions.scala3LTS
  override def scala3Next(): String =
    Versions.scala3Next

}
