library(ggplot2)
library(ramify)

popCounts <- read.csv('../output/population_counts.2021.Nov.12.10_59_16.log', header = TRUE)
nectarCollection <- read.csv('../output/nectar_collection.2021.Nov.12.10_59_16.log', header = TRUE)
cumNectar <- read.csv('../output/cumulative_nectar.2021.Nov.12.10_59_16.log', header = TRUE)

runs <- popCounts$run[!duplicated(popCounts$run)]

get_rate <- function(popCounts, nectarCollection) {
  pops <- seq(0:max(popCounts$Bee.Count))
  sapply(pops, function(pop) {
    ticks_at_pop <- popCounts[popCounts$Bee.Count==pop,]$tick
    nectar_at_pop <- Reduce("+", sapply(ticks_at_pop, function(tick) {
      sum(nectarCollection[nectarCollection$tick == tick,]$foundNectar)
    }))
    num_ticks_at_pop <- sum(popCounts$Bee.Count == pop)
    rate <- ifelse(num_ticks_at_pop > 0, nectar_at_pop / num_ticks_at_pop, 0)
    return(rate)
  })
}

rates <- sapply(runs, function(run) {
  get_rate(popCounts[popCounts$run == run,], nectarCollection[nectarCollection$run == run,])
})

# get vector for runs with runs in order
max_pop <- max(popCounts$Bee.Count)
runs_col <- sapply(runs, function(run) {
  rep(run, max_pop + 1)
})
runs_col <- flatten(runs_col, "columns")
rates <- flatten(rates, "columns")
pops <- sapply(runs, function(run) {
  seq(1, max_pop + 1, 1)
})
pops <- flatten(pops, "columns")
rates_df <- data.frame(runs=runs_col, rates, pops)

g <- ggplot(data=rates_df, aes(x=pops, y=rates, group=runs, color=factor(runs))) +
  geom_line() +
  ggtitle("Nectar Collection Rates") +
  xlab("Bee Population") +
  ylab("Rate of Collection (Nectar/ticks at population)") +
  labs(color="Run")
print(g)

g <- ggplot(data=popCounts, aes(x=tick, y=Bee.Count, group=run, color=factor(run))) +
  geom_line() +
  ggtitle("Bee Populations") +
  xlab("Tick") +
  ylab("Bee Population") +
  labs(color="Run")
print(g)

g <- ggplot(data=popCounts, aes(x=tick, y=Flower.Count, group=run, color=factor(run))) +
  geom_line() +
  ggtitle("Flower Populations") +
  xlab("Tick") +
  ylab("Flower Population") +
  labs(color="Run")
print(g)

g <- ggplot(data=cumNectar, aes(x=tick, y=Cumulative.Nectar, group=run, color=factor(run))) +
  geom_line() +
  ggtitle("Cumulative Nectar Collected") +
  xlab("Tick") +
  ylab("Cumulative Nectar") +
  labs(color="Run")
print(g)
