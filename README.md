MS/MS Peptide Finite State Machine (PFSM)
====

This repository contains the source-code for the Peptide Finite State Machine (PFSM) algorithm described in a [manuscript](Bioinformatics-2005-Falkner-bioinformatics-bti362.pdf) published during my PhD studies at the University of Michigan. 

>### Fast tandem mass spectra–based protein identification regardless of the number of spectra or potential modifications examined.
Jayson Falkner and Philip Andrews †
†Dept. of Biological Chemistry and Program in Bioinformatics , University of
Michigan, 1301 Catherine St., Ann Arbor, MI 48109

>#### Abstract
**Motivation:** Comparing tandem mass spectra (MSMS) against a known data set
of protein sequences is a common method for identifying unknown proteins;
however, the processing of tandem mass spectra by current software often
limits certain applications, including comprehensive coverage of post–
translational modifications, non–specific searches, and real–time searches to
allow result–dependent instrument control. This problem deserves attention as
new mass spectrometers provide the ability for higher throughput and as
known protein data sets rapidly grow in size. New software algorithms need to
be devised in order to address the performance issues of conventional MSMS
protein data set–based protein identification.

I've described this manuscript several times and often to people familiar with CS but not science and proteomics. An easy way to describe this in CS terms is that I implemented a regular expression based on masses in a tandem mass spectrum (MS/MS) instead of letters in an alphabet. The net benefit is that, like a regular expression, you can multiplex an arbitrary number of words (spectra in this case) and it searches in the same amount of time. 

If you aren't a science or CS person, then the explaination is simpler. Protein analysis makes a few thousand scans per run. Modern software searches one at a time, which can take a long time. This algorithm searches everything in the same amount of time it takes to search one thing. It is really fast. Magic.

