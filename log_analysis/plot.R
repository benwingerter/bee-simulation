library(plyr)

options(digits.secs = 3)
fileTable <- read.delim('../bee_model.log', header = FALSE, sep='\n')
nectarFound <- data.frame(matrix(ncol = 3))
colnames(nectarFound) <- c('Tick', 'Bee', 'flower')
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
      nectarFound <- rbind(nectarFound, c(tick, bee, flower))
      nectarCnt <- nectarCnt + 1
      nectarCum <- c(nectarCum, nectarCnt)
    }
  }
}
nectarFound <- nectarFound[-c(1),]
nectarFound$cumulative <- nectarCum

nectarFound$nectar <- 1

df <- ddply(nectarFound, .(), transform, nectar=cumsum(nectar))
plot(nectar~Tick, data=df, type="s", ylab="Total Nectar Retrieved", xlab="Ticks")



