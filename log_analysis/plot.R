library(ggplot2)

popCounts <- read.csv('../population_counts.log', header = TRUE)
nectarCollection <- read.csv('../nectar_collection.log', header = TRUE)
cumNectar <- read.csv('../cumulative_nectar.log', header = TRUE)

# Challenge: reading in the latest log file instead of all log files Do this later
# strptime("2021.Nov.11.14_29_29", "%Y.%B.%d.%H_%M_%S")

getLatestFile <- function(base, extension) {
  # TODO automatically grab latest file
  # matches <- grepl('population_counts', dir('..')) & grepl('.log', dir('..'))
  # dir('..')[grepl('population_counts', dir('..')) & grepl('.log', dir('..'))]
}

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

