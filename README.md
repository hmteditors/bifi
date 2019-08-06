# `bifi`

Simple hack to make markdown pages for a browsable viewer of bifolio spreads for Venetus B and Upsilon 1.1 MSS.


## Results

The `venetus-b-bifolios` and `upsilon-1-1-bifolios` directories have markdown files with YAML headers for the Venetus  B and Upsilon 1.1 manuscripts respectively.  These files can be used with a CMS like jekyll to generate web pages, or can be used with any markdown viewer.

## How the pages are made

The `raw` directory contains annotated lists of automatically generated  names of image files.  Ranges of missing pages were expanded using the `expandLines` function in the `biffer.sc` script, and the results were written in the `expanded` directory.

The cex files in the `expanded` directory can then be further manually edited, and are used as source for making web pages with one of the `printX` functions of the `biffer.sc` script.  The output from these functions consists of markdown-formatted pages written to the `venetus-b-bifolios` and `upsilon-1-1-bifolios` directories.
