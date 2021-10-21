library(plyr)

options(digits.secs = 3)
fileTable <- read.delim('../bee_model.log', header = FALSE, sep='\n')
nectarFound <- data.frame(matrix(ncol = 4))
colnames(nectarFound) <- c('Tick', 'Bee', 'flower', 'Bee Count')
pops <- c()
nectarCnt <- 0
nectarCum <- c()
for(i in 1:nrow(fileTable)) {
  line <- fileTable[i,]
  begin <- strtrim(fileTable[i,], 4);
  if(begin == "INFO") {
    # Get Info
    action <- strsplit(line, ": ")[[1]][2]
    if(action == 'NECTAR FOUND') {
      # add to data frame
      bee <- strsplit(fileTable[i + 1,], ": ")[[1]][2]
      flower <- strsplit(fileTable[i + 2,], ": ")[[1]][2]
      tick <- strsplit(fileTable[i + 3,], ": ")[[1]][2]
      beeCnt <- strsplit(fileTable[i + 4,], ": ")[[1]][2]
      nectarFound <- rbind(nectarFound, c(tick, bee, flower, beeCnt))
      nectarCnt <- nectarCnt + 1
      nectarCum <- c(nectarCum, nectarCnt)
    } else if (action == 'POP LOG') {
      beeCnt <- strsplit(fileTable[i + 1,], ": ")[[1]][2]
      pops <- c(pops, beeCnt);
    }
  }
}
nectarFound <- nectarFound[-c(1),]
nectarFound$cumulative <- nectarCum

nectarFound$nectar <- 1

df <- ddply(nectarFound, .(), transform, nectar=cumsum(nectar))
plot(nectar~Tick, data=df, type="s", ylab="Total Nectar Retrieved", xlab="Ticks")

rates <- c()
for(i in 1:max(pops)) {
  ticks <- sum(pops == i)
  nectar <- sum(nectarFound$`Bee Count` == i)
  rate <- nectar / ticks
  rates <- c(rates, rate)
}
ratePopDf <- data.frame(rates, seq(1, max(pops), 1))
colnames(ratePopDf) <- c('rate', 'pop')

plot(rate~pop, data=ratePopDf, ylab="Rate of Collection (Nectar/tick)", xlab="Bee Population", type="l")

