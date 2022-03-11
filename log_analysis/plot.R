if (!require(ggplot2)) install.packages('ggplot2')
if (!require(here)) install.packages('here')
if (!require(funr)) install.packages('funr')

library(ggplot2)
library(here)
library(funr)

curr_path <- funr::get_script_path()

parent_path <- paste(curr_path, '/..', sep='')
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

popCounts <-        read.csv(here(parent_path, paste('population_counts.', latest_file, '.log', sep='')), header = TRUE)
nectarCollection <- read.csv(here(parent_path, paste('nectar_collection.', latest_file, '.log', sep='')), header = TRUE)
cumNectar <-        read.csv(here(parent_path, paste('cumulative_nectar.', latest_file,'.log', sep='')), header = TRUE)

pops <- seq(0:max(popCounts$Bee.Count))

rates <- sapply(pops, function(pop) {
  ticks_at_pop <- popCounts[popCounts$Bee.Count==pop,]$tick
  nectar_at_pop <- Reduce("+", sapply(ticks_at_pop, function(tick) {
    sum(nectarCollection[nectarCollection$tick == tick,]$foundNectar)
  }))
  num_ticks_at_pop <- sum(popCounts$Bee.Count == pop)
  rate <- ifelse(num_ticks_at_pop > 0, nectar_at_pop / num_ticks_at_pop, 0)
  return(rate)
})

rates_df <- data.frame(Rate = rates, Population = pops)

# Collection Rate Chart
g <- ggplot(data=rates_df, aes(x=Population, y=Rate)) +
  geom_line() +
  geom_point() +
  ggtitle("Nectar Collection Rates") +
  xlab("Bee Population") +
  ylab("Rate of Collection (Nectar/ticks at population)")
print(g)
