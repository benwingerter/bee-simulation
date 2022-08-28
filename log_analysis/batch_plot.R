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
foodCollectionMap <- read.csv(here(parent_path, paste('food_collection.', latest_file, '.batch_param_map.log', sep='')), header = TRUE)
cumFoodMap <-        read.csv(here(parent_path, paste('cumulative_food.', latest_file, '.batch_param_map.log', sep='')), header = TRUE)

popCounts <-        read.csv(here(parent_path, paste('population_counts.', latest_file, '.log', sep='')), header = TRUE)
foodCollection <- read.csv(here(parent_path, paste('food_collection.', latest_file, '.log', sep='')), header = TRUE)
cumFood <-        read.csv(here(parent_path, paste('cumulative_food.', latest_file,'.log', sep='')), header = TRUE)

# Most of the functionality should be here.
# I want a table with columns: parameter (number), survived (boolean), end bees, end food, end bees/food

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
values <- merge(values, popCountsMap, by="run")

# get_rate <- function(popCounts, foodCollection) {
#   pops <- seq(0:max(popCounts$Bee.Count))
#   sapply(pops, function(pop) {
#     ticks_at_pop <- popCounts[popCounts$Bee.Count==pop,]$tick
#     food_at_pop <- Reduce("+", sapply(ticks_at_pop, function(tick) {
#       sum(foodCollection[foodCollection$tick == tick,]$foundFood)
#     }))
#     num_ticks_at_pop <- sum(popCounts$Bee.Count == pop)
#     rate <- ifelse(num_ticks_at_pop > 0, food_at_pop / num_ticks_at_pop, 0)
#     return(rate)
#   })
# }
#
# rates <- sapply(runs, function(run) {
#   get_rate(popCounts[popCounts$run == run,], foodCollection[foodCollection$run == run,])
# })
#
# # get vector for runs with runs in order
# max_pop <- max(popCounts$Bee.Count)
# runs_col <- sapply(runs, function(run) {
#   rep(run, max_pop + 1)
# })
# runs_col <- flatten(runs_col, "columns")
# rates <- flatten(rates, "columns")
# pops <- sapply(runs, function(run) {
#   seq(1, max_pop + 1, 1)
# })
# pops <- flatten(pops, "columns")
# rates_df <- data.frame(runs=runs_col, rates, pops)

# g <- ggplot(data=rates_df, aes(x=pops, y=rates, group=runs, color=factor(runs))) +
#   geom_line() +
#   ggtitle("Food Collection Rates") +
#   xlab("Bee Population") +
#   ylab("Rate of Collection (Food/ticks at population)") +
#   labs(color="Run")
# print(g)
#
# g <- ggplot(data=popCounts, aes(x=tick, y=Bee.Count, group=run, color=factor(run))) +
#   geom_line() +
#   ggtitle("Bee Populations") +
#   xlab("Tick") +
#   ylab("Bee Population") +
#   labs(color="Run")
# print(g)
#
# g <- ggplot(data=popCounts, aes(x=tick, y=Flower.Count, group=run, color=factor(run))) +
#   geom_line() +
#   ggtitle("Flower Populations") +
#   xlab("Tick") +
#   ylab("Flower Population") +
#   labs(color="Run")
# print(g)
#
# g <- ggplot(data=cumFood, aes(x=tick, y=Cumulative.Food, group=run, color=factor(run))) +
#   geom_line() +
#   ggtitle("Cumulative Food Collected") +
#   xlab("Tick") +
#   ylab("Cumulative Food") +
#   labs(color="Run")
# print(g)
