import edu.holycross.shot.cite._
import edu.holycross.shot.citebinaryimage._


import better.files._
import File._
import java.io.{File => JFile}
import better.files.Dsl._


val imageServiceUrlBase = "http://www.homermultitext.org/iipsrv?"
val imageServicePathBase = "/project/homer/pyramidal/deepzoom/"

val vbService = IIIFApi(imageServiceUrlBase, imageServicePathBase + "hmt/vbbifolio/v1/")
val upsilonService = IIIFApi(imageServiceUrlBase, imageServicePathBase + "hmt/e3bifolio/v1/")

val imageServices  : Map[String, IIIFApi] = Map(
  "vbbifolio" -> vbService,
  "upsilonbifolio" -> upsilonService
)

val upsilonData = File("expanded/upsilon-bifs-expand.cex").lines.toVector.tail.filter(_.nonEmpty)
val vbData = File("expanded/vb-bifs-expand.cex").lines.toVector.tail.filter(_.nonEmpty)

val vbDir = File("venetus-b-bifolios")
val upsilonDir = File("upsilon-1-1-bifolios")


/*
Obsolete function used to convert intput in `raw` diretory
to data in `expanded` directory.
def expandLines(raw: Vector[String], ms: String = "", processed: Vector[String] = Vector.empty[String]): Vector[String] = {
  if (raw.isEmpty) {
    processed

  } else {

    val ln = raw.head
    if (ln.startsWith("// MISSING")) {
      println("PROCESS " + ln)
      val pair = ln.replaceFirst("// MISSING", "").replaceAll("v","").split("-").toVector.map(_.trim.toInt)
      val subs = for (pg <- pair(0) to pair(1)) yield {
        s"${ms}${pg}v#${ms}${pg+1}v#"
      }

      expandLines(raw.tail, ms, processed ++ subs.toVector)
    } else {

      def cols = ln.split("#").toVector

      if (cols.size < 3) {
        println ("BAD DATA:  " + ln)
        expandLines(raw.tail, ms, processed)
      } else {
        val recto = Cite2Urn(cols(0))
        val siglum = recto.dropSelector.toString
        expandLines(raw.tail, siglum, processed :+ ln)
      }
    }
  }
}
*/


/** Format main page body with image for a single
* line of data.
*
* @param text A single line of CEX data.
*/
def dataLine(text: String): String = {
  val data = text.split("#").toVector
  if (data.size == 2) {
    try {
      val verso = Cite2Urn(data(0))
      val recto = Cite2Urn(data(1))
      s"Composite image not yet made for bifolio spread ${verso.objectComponent}-${recto.objectComponent}\n\n"
    }  catch {
      case t: Throwable => {
        s"# Bad data\n\nUnable to format page for line ${data(0)}-${data(1)}\n"
      }
    }

  } else if (data.size >= 3) {
    try {
      val verso = Cite2Urn(data(0))
      val recto = Cite2Urn(data(1))
      val image = Cite2Urn(data(2))


      val imageService = imageServices(image.collection)

      "\n\n" + imageService.linkedMarkdownImage(image) + "\n\n"


    } catch {
      case t: Throwable => {
        s"# Bad data\n\nUnable to format page for line ${data(0)}-${data(1)} with image reference ${data(2)}\n"
      }
    }
  } else {
    "Bad data line: " + text
  }
}


/** Extract bifolio reference from a single line of
* CEX data.  Return a String with either the bifolio
* refernce or an error message, accompanied by a Boolean
* value indicating whether the bifolio referene was
* corredtly made.
*
* @ln A single line of CEX data.
*/
def bifolioRef(ln: String): (String, Boolean) = {
    val cols = ln.split("#").toVector
    if (cols.size >= 2) {
      try {
        val verso = Cite2Urn(cols(0))
        val recto = Cite2Urn(cols(1))

        //s"Yay.  Expand and format ${image}"
        (s"${verso.objectComponent}-${recto.objectComponent}", true)
      } catch {
        case t: Throwable => {
          (s"# Bad data\n\nUnable to format page for ${ln}\n", false)
        }
      }
    } else {
      (s"# Bad data\n\nUnable to format page for ${ln}\n", false)
    }
}


/** Compose header for jekyll web page for a single
* line of data.
*
* @param text A single line of CEX data.
*/
def header(ln: String): String = {
  val data = ln.split("#").toVector
    if (data.size >= 3) {
      try {
        val verso = Cite2Urn(data(0))
        s"---\nlayout: page\ntitle: Manuscript ${verso.collection}, bifolio ${bifolioRef(ln)._1}\n---\n\n"


      } catch {
        case t: Throwable => {
          s"---\nlayout: page\ntitle: Bad data\n---\n\nUnable to format page for ${ln}\n\n"
        }
      }
    } else {
      s"---\nlayout: page\ntitle: Bad data\n---\n\nUnable to format page for ${ln}\n\n"
    }
}


/** Compose a single markdown page. It uses a Vector of
* strings because it formats the page using the first line,
* but peeks ahead at the second line to see if there is a valid
* value for "next" links.
*
* @param text A single line of CEX data.
* @param prev Value for link to previous bifolio spread.
*/
def formatPage(data: Vector[String], prev: String = "")   :  (String, Boolean) = {
  val (currentBifolio, pgOk) = bifolioRef(data.head)

  val hdr = header(data.head)
  if (data.tail.isEmpty) {
    // last page, so empty "next"
    val nav = s"prev: [${prev}](../${prev}/) next: -\n\n"
    (hdr + nav + dataLine(data.head), pgOk)

  } else {

    val (nxtRef, nextOk) = bifolioRef(data.tail.head)

    val nav = if (nextOk) {
      s"prev: [${prev}](../${prev}/) next: [${nxtRef}](../${nxtRef}/)\n\n"
    } else {
      s"prev: [${prev}](../${prev}/) next: Invalid ref. to next pages\n\n"
    }
    (hdr + nav + dataLine(data.head), pgOk)
  }
}


/** Recursively format web pages for a manuscript.
*
* @param data  Vector of CEX data lines, one per page.
* @param mdPages Accumlation of resulting web page info.
* This triple consists of a String with the bifolio reference,
* a String with the markdown to use as the page content,
* and a Boolean indicating whether* the resulting page is
* correctly formed or in error.
* @param prev Bifolio reference for previous page.
*/
def formatMS(
  data: Vector[String],
  mdPages: Vector[(String, String, Boolean)] =  Vector.empty[(String, String, Boolean)],
  prev: String = "") : Vector[(String,String, Boolean)] = {


  if (data.isEmpty) {
    mdPages

  } else {
    val (currentBifolio, pgOk)  = bifolioRef(data.head)
    val bifolioString = if(pgOk) {
      currentBifolio
    } else {
      "(bad reference)"
    }
    val (pg, ok) = formatPage(data, prev)
    val newPage = (currentBifolio, pg, ok)
    formatMS(data.tail, mdPages :+ newPage, bifolioString)
  }
}

/** Write markdown pages in a given directory.
*
* @param formatted Triplets describing a web page, as produced by
* the `formatMS` function.
* @param dir Directory where pages should be written.
*/
def printMS(formatted: Vector[(String,String,Boolean)], dir: File): Unit = {
  for ((pg,pgIndex) <- formatted.zipWithIndex) {
    val outFile = if (pg._3) {
      dir / s"${pg._1}.md"
    } else {
      dir / s"bad-file-${pgIndex}.md"
    }
    val contents = pg._2
    outFile.overwrite(contents)
  }
}

/** Write markdown pages for Venetus B.*/
def printVB = {
  printMS(formatMS(vbData), vbDir)
}

/** Write markdown pages for Upsilon 1.1.*/
def printUpsilon = {
  printMS(formatMS(upsilonData), upsilonDir)
}


/** Write markdown pages for both MSS.*/
def printAll = {
  printVB
  printUpsilon
}
