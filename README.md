# bifi

Simple hack to make markdown pages for a browsable viewer of bifolio spreads for Venetus B and Upsilon 1.1 MSS.

The `raw` directory contains annotated lists of automatically generated file names.  Ranges of missing pages were expanded using the `expandLines` function in the `biffer.sc` script, and the results were written in the `expanded` directory.

Files in the `expanded` directory are then further manually edited, and should be used as source for making web pages with one of the `printX` functions of the `biffer.sc` script.  The output from these functions consists of markdown-formatted pages written to the `msB` and `e3` directories.
