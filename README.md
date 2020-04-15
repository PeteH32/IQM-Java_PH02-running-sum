
### Method using running sum
A "running sum" method was fastest. It eliminates the inner loop that recalculates total sum of IQR.
This change made it 1,000% faster (10x) -- ran in 30 seconds, compared to 5 minutes for original code.
  - See class "IQM_V4_RunningSum"
    

