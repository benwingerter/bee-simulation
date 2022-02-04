library(ggplot2)
library(here)

print("ben")
print(here('..', paste('population_counts.', d, '.log', sep='')))


getLatestFile <- function(base, extension) {
  matches <- grepl('population_counts', dir('..')) & grepl('.log', dir('..'))
  files <- dir('..')[grepl('population_counts', dir('..')) & grepl('.log', dir('..'))]
  files <- substring(files, 19, 38)
  dates <- strptime(files, "%Y.%B.%d.%H_%M_%S")
  res <- sort(dates, decreasing = TRUE)
  res <- format(res, "%Y.%b.%d.%H_%M_%S")
  return(res)
}

d <- getLatestFile('population_counts', '.log')[1]

popCounts <-        read.csv(here('..', paste('population_counts.', d, '.log', sep='')), header = TRUE)
nectarCollection <- read.csv(paste('../nectar_collection.', d, '.log', sep=''), header = TRUE)
cumNectar <-        read.csv(paste('../cumulative_nectar.', d,'.log', sep=''), header = TRUE)

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

