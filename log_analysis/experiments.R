if (!require(ggplot2)) install.packages('ggplot2')
if (!require(here)) install.packages('here')
if (!require(funr)) install.packages('funr')

# Change this to match which variable is being studied
study_variable <- "beeRegenRate"

curr_path <- funr::get_script_path()
if(is.null(curr_path)) {
  curr_path <- here()
}
parent_path <- paste(curr_path, '/../output', sep='')

#' Get the date of the latest log file
#' 
#' @return The latest log datetime in the format %Y.%b.%d.%H_%M_%S
getLatestRun <- function(base, extension) {
  matches <- grepl(base, dir(parent_path)) & grepl(extension, dir(parent_path))
  files <- dir(parent_path)[grepl(base, dir(parent_path)) & grepl(extension, dir(parent_path))]
  files <- substring(files, 19, 38)
  dates <- strptime(files, "%Y.%B.%d.%H_%M_%S")
  res <- sort(dates, decreasing = TRUE)
  res <- format(res, "%Y.%b.%d.%H_%M_%S")
  return(res)
}

latest_file <- getLatestRun('population_counts', '.log')[1]

popCountsMap <-        read.csv(here(parent_path, paste('population_counts.', latest_file, '.batch_param_map.log', sep='')), header = TRUE)
foodCollectionMap <- read.csv(here(parent_path, paste('food_collection.', latest_file, '.batch_param_map.log', sep='')), header = TRUE)
cumFoodMap <-        read.csv(here(parent_path, paste('cumulative_food.', latest_file, '.batch_param_map.log', sep='')), header = TRUE)

popCounts <-        read.csv(here(parent_path, paste('population_counts.', latest_file, '.log', sep='')), header = TRUE)
foodCollection <- read.csv(here(parent_path, paste('food_collection.', latest_file, '.log', sep='')), header = TRUE)
cumFood <-        read.csv(here(parent_path, paste('cumulative_food.', latest_file,'.log', sep='')), header = TRUE)

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
values$ratio[is.infinite(values$ratio)] <- values$Bee.Count[is.infinite(values$ratio)]
values <- merge(values, popCountsMap, by="run")
values <- values[with(values, order(tick, run)),]

save(values, popCounts, popCountsMap, cumFood, cumFoodMap, foodCollection, foodCollectionMap, file = paste(latest_file, ".RData", sep=""))

cat("\nParameter Values:\n")
cat(paste(values[,study_variable][1:10]), sep="\n")
cat("\nFirst Ten\n")
cat(paste(round(values$ratio, digits = 3)[1:10]), sep="\n")
cat("\nSecond Ten\n")
cat(paste(round(values$ratio, digits = 3)[11:20]), sep="\n")
cat("\nThird Ten\n")
cat(paste(round(values$ratio, digits = 3)[21:30]), sep="\n")

regression_formula <- paste("ratio ~", study_variable, "+ tick")
print(summary(lm(regression_formula, values)))
