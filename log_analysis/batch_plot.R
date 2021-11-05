library(plyr)
library(ggplot2)

options(digits.secs = 3)
files <- dir('../output/logs')
df <- data.frame()
ratePop <- data.frame()
for(file in files) {
  if(grepl('bee_model', file, fixed=TRUE)) {
    fileTable <- read.delim(paste('../output/logs/', file, sep=''), header = FALSE, sep='\n')

    # Parse Log File
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
          # Add to data frame
          bee <- strsplit(fileTable[i + 1,], ": ")[[1]][2]
          flower <- strsplit(fileTable[i + 2,], ": ")[[1]][2]
          tick <- strsplit(fileTable[i + 3,], ": ")[[1]][2]
          beeCnt <- strsplit(fileTable[i + 4,], ": ")[[1]][2]
          nectarFound <- rbind(nectarFound, c(tick, bee, flower, beeCnt))
          nectarCnt <- nectarCnt + 1
          nectarCum <- c(nectarCum, nectarCnt)
        } else if (action == 'POP LOG') {
          beeCnt <- strsplit(fileTable[i + 1,], ": ")[[1]][2]
          pops <- c(pops, beeCnt)
        }
      }
    }
    
    # Cumulative Nectar
    nectarFound <- nectarFound[-c(1),]
    nectarFound$cumulative <- nectarCum
    nectarFound$simulation <- file
    nectarFound$nectar <- 1
    df1 <- NULL
    df1 <- ddply(nectarFound, .(), transform, nectar=cumsum(nectar))
    df1$Tick <- as.numeric(df1$Tick)
    if(nrow(df) == 0) {
      df <- df1
    } else {
      df <- rbind(df, df1)
    }

    # Average Rate of Collection
    rates <- c()
    for(i in 1:max(pops)) {
      ticks <- sum(pops == i)
      nectar <- sum(nectarFound$`Bee Count` == i)
      rate <- nectar / ticks
      rates <- c(rates, rate)
    }
    ratePopDf <- data.frame(rates, seq(1, max(pops), 1))
    ratePopDf$simulation <- file
    colnames(ratePopDf) <- c('rate', 'pop', 'file')
    if(nrow(ratePop) == 0) {
      ratePop <- ratePopDf
    } else {
      ratePop <- rbind(ratePop, ratePopDf)
    }
  }
}

# Cumulative Nectar Collection Chart
g <- ggplot(data=df, aes(x=Tick, y=nectar, colour=simulation)) +
  geom_line() +
  geom_point() +
  ggtitle("Cumulative Nectar Collection") +
  xlab("Total Nectar Collected") +
  ylab("Ticks")
print(g)

# Collection Rate Chart
g <- ggplot(data=ratePop, aes(x=pop, y=rate, colour=file)) +
  geom_line() +
  geom_point() +
  ggtitle("Average Collection Rates") +
  xlab("Population") +
  ylab("Nectar Collection Rate")
print(g)

# TODO graph bee population counts

# TODO graph flower population counts
