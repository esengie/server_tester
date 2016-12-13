
nameBegs = c("TCP_PERM_ASYNC", "TCP_PERM_CACHED_POOL", "TCP_PERM_THREADS", 
             "TCP_PERM_NON_BLOCK", "TCP_TEMP_SINGLE_THREAD", 
            "UDP_FIXED_THREAD_POOL", "UDP_THREAD_PER_REQUEST")
nameEnds = c("ELEMENTS_PER_REQ", "CLIENTS_PARALLEL", "TIME_DELTA")
attach = "init"
plot_colors <- c("blue", "red", "forestgreen", "orange", "black", "brown", "grey")

prettifiedEnds = c("Number of elements per request", "Number of clients running in parallel", "Time before next request, ms") 

f = function(graph, xname, yname) {
    scale = 1.2
    lwidth = 5
    relativeT = 1.5
    plot(graph[,1], graph[,2], type="l", col=plot_colors[1], 
       xlab=paste("y =", yname, "     ", "x = ", xname), 
       ylab="",
       ylim=c(min(graph[,2:8], na.rm = TRUE) / scale, max(graph[,2:8], na.rm = TRUE) * scale),
       cex.lab=relativeT, lwd=lwidth)
    
    for (i in 3:8){
        lines(graph[,1], graph[,i], type="l", lty=i, lwd=lwidth, 
          col=plot_colors[i-1])
    }    
    legend("topleft", names(graph)[2:8], cex=relativeT-0.3, col=plot_colors, 
    lty=2:8, lwd=2, bty="n");
}

g = function(i){
    results = new.env()
    fixed_vals = read.csv(file = paste("results/0/", nameBegs[i], ":", nameEnds[i], ":", attach, sep=''), head=TRUE, sep=",")
    fixed_vals[,i] = NULL
    head(fixed_vals)
    for (s in nameBegs){
        results[[s]] = read.csv(file = paste("results/0/", s, ":", nameEnds[i], sep=''), head=TRUE, sep=",")
    }

    one = data.frame(results[[nameBegs[1]]][,1])
    colnames(one) = nameEnds[i] 
    maxr = nrow(one)
    two = one
    three = one

    for (s in nameBegs){
        one[[s]] = results[[s]][1:maxr,2] / 1000
        two[[s]] = results[[s]][1:maxr,3] / 1000
        three[[s]] = results[[s]][1:maxr,4] / 1000000000
    }
    ##################################################
    png(file=paste("figure", i, ".png", sep=""), width = 1024, height = 768, units = "px")
    layout(matrix(c(1,1,2,3), 2, 2, byrow = TRUE))
    
    f(three, prettifiedEnds[i], "Time per client, s")
    title(paste("Graphs with varying", nameEnds[i], "\nwhile", 
                names(fixed_vals)[1], "=", fixed_vals[1,1], ",", 
                names(fixed_vals)[2], "=", fixed_vals[1,2], ", and", 
                names(fixed_vals)[3], "=", fixed_vals[1,3]), cex = 1.5)
    f(one, prettifiedEnds[i], "Time per one sort op, mcs")
    f(two, prettifiedEnds[i], "Time per one request, mcs")
    dev.off()
}

g(1)
g(2)
g(3)
