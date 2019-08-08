# `bifi`

Simple hack to make markdown pages for a browsable viewer of bifolio spreads for Venetus B and Upsilon 1.1 MSS.  The HMT project web sites includes the output of this repository:  see the "Citable bifolio images" at <http://www.homermultitext.org/digital/>.

## TLDR


To modify data and regenerate web pages:

1. Edit tables in the `expanded` folder
2. From an sbt console in this directory, `:load scripts/biffer.sc` and run `printAll`

To generate an string of HTML `option`s you can use in a `select` to create a popup-menu for a manuscript:

1. From an sbt console in this directory, `:load scripts/options.sc` and run `printOptions`

## Data source

The pages are built from simple tables associating a recto and verso page (identified by CITE2URN) with an image (also identified by a CITE2 URN) of their bifolio spread.  These files are in the `expanded` folder.

## Results

The `venetus-b-bifolios` and `upsilon-1-1-bifolios` directories have markdown files with YAML headers for the Venetus  B and Upsilon 1.1 manuscripts respectively.  These files can be used with a CMS like jekyll to generate web pages, or can be used with any markdown viewer.

## How the pages are made

The `raw` directory contains annotated lists of automatically generated  names of image files.  Ranges of missing pages were expanded using the `expandLines` function in the `biffer.sc` script, and the results were written in the `expanded` directory.

The cex files in the `expanded` directory can then be further manually edited, and are used as source for making web pages with one of the `printX` functions of the `biffer.sc` script.  The output from these functions consists of markdown-formatted pages written to the `venetus-b-bifolios` and `upsilon-1-1-bifolios` directories.


## Access from an HTML popup menu

The `options.sc` script reads the same data sources as the `biffer.sc` script, and writes an HTML `option` element for each entry.  You can insert the `options` within a `select` element like this one:

    <select name='x-menu' onfocus="this.selectedIndex = 0" onchange='location = this.options[this.selectedIndex].value;'>
    <!-- INSERT OPTION ELEMENTS HERE -->
    </select>
