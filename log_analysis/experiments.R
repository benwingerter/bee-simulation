if (!require(ggplot2)) install.packages('ggplot2')
if (!require(here)) install.packages('here')
if (!require(funr)) install.packages('funr')
if (!require(ramify)) install.packages('ramify')

library(ggplot2)
library(ramify)
library(here)
library(funr)

# This line requires that 
curr_path <- funr::get_script_path()
if(is.null(curr_path)) {
  curr_path <- here()
}
parent_path <- paste(curr_path, '/../output', sep='')

#' Get the date of the latest log file, using population_counts*.log as a reference
#' 
#' @return The latest log datetime in the format %Y.%b.%d.%H_%M_%S
getLatestFile <- function(base, extension) {
  matches <- grepl('population_counts', dir(parent_path)) & grepl('.log', dir(parent_path))
  files <- dir(parent_path)[grepl('population_counts', dir(parent_path)) & grepl('.log', dir(parent_path))]
  files <- substring(files, 19, 38)
  dates <- strptime(files, "%Y.%B.%d.%H_%M_%S")
  res <- sort(dates, decreasing = TRUE)
  res <- format(res, "%Y.%b.%d.%H_%M_%S")
  return(res)
}

latest_file <- getLatestFile('population_counts', '.log')[1]

popCountsMap <-        read.csv(here(parent_path, paste('population_counts.', latest_file, '.batch_param_map.log', sep='')), header = TRUE)
nectarCollectionMap <- read.csv(here(parent_path, paste('nectar_collection.', latest_file, '.batch_param_map.log', sep='')), header = TRUE)
cumNectarMap <-        read.csv(here(parent_path, paste('cumulative_nectar.', latest_file, '.batch_param_map.log', sep='')), header = TRUE)

popCounts <-        read.csv(here(parent_path, paste('population_counts.', latest_file, '.log', sep='')), header = TRUE)
nectarCollection <- read.csv(here(parent_path, paste('nectar_collection.', latest_file, '.log', sep='')), header = TRUE)
cumNectar <-        read.csv(here(parent_path, paste('cumulative_nectar.', latest_file,'.log', sep='')), header = TRUE)

# Most of the functionality should be here.
# I want a table with columns: parameter (number), survived (boolean), end bees, end nectar, end bees/nectar

runs <- popCounts$run[!duplicated(popCounts$run)]

# Decide if each run survived
values <- tail(popCounts[popCounts$run == 1,], n=1)
if(length(runs) > 1) {
  for(i in 2:length(runs)) {
    last_pop_count <- tail(popCounts[popCounts$run == i,], n=1)
    values <- rbind(values, last_pop_count)
  }
}
values$ratio <- values$Bee.Count / values$Flower.Count
values$ratio[is.infinite(values$ratio)] <- 0
values <- merge(values, popCountsMap, by="run")
