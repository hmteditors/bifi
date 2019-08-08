/*
Write HTML options for a MS
*/

import edu.holycross.shot.cite._
import edu.holycross.shot.citebinaryimage._


import better.files._
import File._
import java.io.{File => JFile}
import better.files.Dsl._


val vbData = File("expanded/vb-bifs-expand.cex").lines.toVector.tail.filter(_.nonEmpty)
val upsilonData = File("expanded/upsilon-bifs-expand.cex").lines.toVector.tail.filter(_.nonEmpty)



/** Format main page body with image for a single
* line of data.
*
* @param text A single line of CEX data.
*/
def optionForLine(text: String, msDir: String): String = {
  val optionBase = "<option value='http://www.homermultitext.org/facsimiles/"
  val data = text.split("#").toVector
  if (data.size == 2) {
    try {
      val verso = Cite2Urn(data(0))
      val recto = Cite2Urn(data(1))

      val opt = optionBase + msDir + s"${verso.objectComponent}-${recto.objectComponent}'>pages ${verso.objectComponent}-${recto.objectComponent} (composite image not yet made)</option>"
      opt

    }  catch {
      case t: Throwable => {
        s"# Bad data\n\nUnable to format page for line ${data(0)}-${data(1)}\n"
      }
    }

  } else if (data.size >= 3) {
    try {
      val verso = Cite2Urn(data(0))
      val recto = Cite2Urn(data(1))



      val opt = optionBase + msDir + s"${verso.objectComponent}-${recto.objectComponent}'>pages ${verso.objectComponent}-${recto.objectComponent}</option>"
      opt

    } catch {
      case t: Throwable => {
        println(  s"\n\nBad data\n\nUnable to format page for line ${data(0)}-${data(1)} with image reference ${data(2)}")
        println(t + "\n\n")
        s"# Bad data\n\nUnable to format page for line ${data(0)}-${data(1)} with image reference ${data(2)}\n\n"
      }
    }
  } else {
    "Bad data line: " + text
  }
}

/** Recursively format HTML option list for a manuscript.
*
* @param data  Vector of CEX data lines, one per page.
* @param msDir URL for directory for this MS' images.
* @param optionList Accumlation of HTML option elements
*/
def msOptions( data: Vector[String],
  msDir: String,
  optionList: Vector[(String)] =  Vector.empty[String]) : Vector[(String)] = {

  if (data.isEmpty) {
    optionList

  } else {
    msOptions(data.tail, msDir, optionList :+ optionForLine(data.head, msDir))
  }
}

/** Write markdown pages for Venetus B.*/
def vbOptions = {
  val dir = "venetus-b-bifolios/"
  msOptions(vbData, dir, Vector.empty[String])
}

/** Write markdown pages for Upsilon 1.1.*/
def upsilonOptions = {
  val dir = "upsilon-1-1-bifolios/"
  msOptions(upsilonData, dir, Vector.empty[String])
}


/** Write markdown pages for both MSS.*/
def printOptions = {
  val vbOptFile = File("venetus-b-options.txt")
  vbOptFile.overwrite(vbOptions.mkString("\n"))

  val upsilonOptFile = File("upsilon-1-1-options.txt")
  upsilonOptFile.overwrite(upsilonOptions.mkString("\n"))
}
