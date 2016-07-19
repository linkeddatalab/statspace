# statspace http://statspace.linkedwidgets.org
In recent years, the amount of statistical data available on the web has been growing dramatically.
Numerous organizations and governments publish statistical data in a multitude of formats and encodings using different scales and providing access through a wide range of mechanisms.
Due to such inconsistent data publishing practices, integrated analysis of statistical data sets is challenging. 

To address these issues, we build Statspace, a linked statistical data space that provides uniform access to statistical data and facilitates automated data integration.
The Linked Data infrastructure created by Statspace transparently lifts data from raw sources, maps components (e.g., geographical and temporal dimensions), aligns value ranges, and allows users to explore and integrate previously isolated data sets.
The core component of Statspace is a metadata repository that describes the data sets made available by publishers in a uniform manner.
Each metadata description in the repository captures the information needed to query and integrate individual data sets, i.e., 
(i) data structure and access method for query building, and
(ii) link relationships that connect components and values used in the data set to a set of shared URIs.
The well-defined metadata in the repository hence provides a standardized conceptual layer that makes it possible to transform generic queries into the format needed for individual data sets and to integrate the results.

Statspace is available at http://statspace.linkedwidgets.org and currently provides uniform access to more than 1,800 data sets published by a variety of data providers including the World Bank, the European Union, and the European Environmental Agency.
