# Movie Recommender System (Small dataset)

## Overview
The system in this branch is fit for small dataset and have a bit more features in RecommendationListGenerator, but would have problems if running for the original Netflix dataset.

## Data
Data has the following format:

User_id, Movie_id, Rating  
1, 10001, 5.0  
2, 10001, 2.0  
3, 10001, 2.0  
4, 10001, 5.0  
5, 10001, 4.0

## Step

* Divide data by user id
* Build co-occurrence matrix
* Build rating matrix
* Multiply co-occurrence matrix and rating matrix
* Generate recommendation list

## How to run

I deployed a hadoop cluster on Docker, which has one namenode and two datanodes, and tested my program on it.

```
$hadoop com.sun.tools.javac.Main *.java
$jar cf recommender.jar *.class
$hadoop jar recommender.jar Driver /input /dataDividedByUser /coOccurrenceMatrix /Multiplication /coOccurrenceMatrix/part-r-00000 /input/userRating.txt /MovieTitle/movieTitles.txt /Recommender
```

* args0: original dataset
* args1: output directory for DividerByUser job
* args2: output directory for coOccurrenceMatrixBuilder job
* args3: output directory for Multiplication job
* args4: file representing co-occurrence matrix
* args5: file representing user rating matrix
* args6: file of movie title 
* args7: output directory for RecommenderListGenerator job


