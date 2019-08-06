import edu.holycross.shot.cite._
import edu.holycross.shot.citebinaryimage._


import better.files._
import File._
import java.io.{File => JFile}
import better.files.Dsl._


val imageServiceUrlBase = "http://www.homermultitext.org/iipsrv?"
val imageServicePathBase = "/project/homer/pyramidal/deepzoom/"

val vbService = IIIFApi(imageServiceUrlBase, imageServicePathBase + "hmt/vbbifolio/v1/")
val oopsService = IIIFApi(imageServiceUrlBase, imageServicePathBase + "hmt/e3bifolio/v1/")

val imageServices  : Map[String, IIIFApi] = Map(
  "vbbifolio" -> vbService,
  "e3bifolio" -> oopsService
)

val oopsData = File("oops-bifs.txt").lines.toVector.tail.filter(_.nonEmpty)
val msBData = File("vb-bifs.txt").lines.toVector.tail.filter(_.nonEmpty)

val vbDir = File("msB")
val e3Dir = File("e3")

def expandLines(raw: Vector[String], processed: Vector[String] = Vector.empty[String]): Vector[String] = {
  if (raw.isEmpty) {
    processed
  } else {
    val ln = raw.head
    def cols = ln.split("#").toVector
    if (cols.size < 3) {
      println ("BAD DATA:  " + ln)
      expandLines(raw.tail, processed)
    } else {
      expandLines(raw.tail, processed :+ ln)
    }
  }
}

def dataLine(text: String): String = {
  val data = text.split("#").toVector
  if (data.size >= 3) {
    try {
      val verso = Cite2Urn(data(0))
      val recto = Cite2Urn(data(1))
      val image = Cite2Urn(data(2))


      val imageService = imageServices(image.collection)

      "\n\n" + imageService.linkedMarkdownImage(image) + "\n\n"
      //s"Yay.  Expand and format ${image}"

    } catch {
      case t: Throwable => {
        s"# Bad data\n\nUnable to format page for line ${data(0)}-${data(1)} with image reference ${data(2)}\n"
      }
    }
  } else {
    "Bad data line: " + text
  }
}


def bifolioRef(ln: String): (String, Boolean) = {
    val cols = ln.split("#").toVector
    if (cols.size >= 3) {
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

def printMS(formatted: Vector[(String,String,Boolean)], dir: File): Unit = {
  for ((pg,pgIndex) <- formatted.zipWithIndex) {
    val outFile = if (pg._3) {
      dir / s"${pg._1}.md"
//
    } else {
      dir / s"bad-file-${pgIndex}.md"

    }
    val contents = pg._2
    outFile.overwrite(contents)
  }
}

def printVB = {
  printMS(formatMS(msBData), vbDir)
}
def printUpsilon = {
  printMS(formatMS(oopsData), e3Dir)
}
def printAll = {
  printVB
  printUpsilon
}
