# SweetSpot Code Challenge -- Pete's Answers

-------------------------------------------------------


## INSTRUCTIONS:

The challenge has two parts. You may attempt *one* or the *other*, or *both*. If you attempt both parts, submit a single solution that includes all changes.


## PART 1: REFACTORING

The code provided is written in a very rudimentary style. It is difficult to understand, and does not communicate intent or functionality.


Can you improve the code such that:


1. It communicates function and intention clearly, AND
2. It is the kind of code YOU want to read and write.


Please provide your code, any tests you create, and answers to the following questions:


1. Explain your improvement process, and why you chose each step.
2. What improvements did you make to ensure that future developers could quickly and easily understand this code?


    Pete's answers for REFACTORING
    ------------------------------  
        
    1. Explain your improvement process, and why you chose each step.
    - I ended up writing a new implementation, and occasionally looking at the original code as a
    reference implementation. That new implementation is here:
        - com.sweetspotdiabetes.IQM_V2_SimpleRefactor.main()  
        
    - Factors that led me to writing separate implementation:
        - It took me a while to really understand the nuances of calculating the basic Interquartile 
        Mean (I used Wikipedia), and to learn it I wrote a little code as I learned a little part of it. 
        This approach helped me slowly put together the whole picture of the calculation.
        I used the old code to compare my results. One time when I was getting different results, it turned out I 
        misunderstood the "weighted" aspect mentioned late in the Wikipedia article.
          - https://en.wikipedia.org/wiki/Interquartile_mean
        - The original code is hard to read, so I wasn't quite confident I understood the portions packed with a lot
        of mathmetical calculations.  Even after I fully understood the Wikipedia descriptions and examples, 
        and had written my own working implementation, I still had hard time following the math calculations in 
        the original code.  
          
    2. What improvements did you make to ensure that future developers could quickly and easily understand this code?
    - I added reference to Wikipedia article where I got basic calculation. 
    - Added comments in places where code may be hard to follow.
    - Added sample data files, with expected results. (These are in the unit tests.)
      


## PART 2: OPTIMIZATION

The code provided is slow.  On a modern MacBook Pro it takes nearly 4 minutes to execute.

Can you optimize the code such that:


1. It runs significantly faster, AND
2. It still produces the same output as the example code, for the given data.txt input, AND
3. It still calculates the Incremental Interquartile Mean after each value is read, AND
4. It will continue to produce correct output given any data set with any number of integer values between 0 and 600.


Please provide the optimized code, an automated test that proves that your code works, and answers to the following questions:


1. Explain how your optimization works and why you took this approach
2. Would your approach continue to work if there were millions or billions of input values?
3. Would your approach still be efficient if you needed to store the intermediate state between each IQM calculation in a data store?  If not, how would you change it to meet this requirement?


    Pete's answers for OPTIMIZATION
    ------------------------------  
      

    YourKit profiler snapshots are in this folder. The runs were using data set of 25,000 values.
        - .\YourKit Profiler Snapshots\*.snapshot
        

    1. Explain how your optimization works and why you took this approach      
    - As mentioned above, I ended up writing new implementation (this was easiest way for to learn "Interquartile Mean").
    After I verified my new code got identical results to the old code, I ran timing comparisons -- and was surprised
    to see my new code was about twice as fast as the original code.
        - Results for 100,000 values:
            - Old code  --  com.sweetspotdiabetes.IncrementalIQM_ORIG.main()
                - Time: 3.6 min
            - New code  --  com.sweetspotdiabetes.IQM_V2_SimpleRefactor.main()  
                - Time: 1.7 min  (about twice as fast)
            
    - I was not sure why my code was twice as fast -- reviewing the two implementations, I could see they are pretty much 
    same algorithm. So I suspected the difference could be related to high-level language constructs and how 
    the Vector was being handled. I also suspected it had to be someting inside the inner for-loop. Next I ran a 
    profiler (YourKit) on both implementations, which confirmed they spent majority of their time inside the for-loop's.
        - Old for-loop: Uses 14.7 sec out of total of 20.7 sec (71%)
        - New for-loop: Used  3.8 sec out of total of  8.5 sec (45%)   -- Over 3 times faster
    - The old code is using "random access" a lot whereas my code used the "for-each construct" which might be avoiding 
    potential overhead of random access.  
      
    2. Would your approach continue to work if there were millions or billions of input values?
    - My original approach would not scale too well to billions since it is running Collections.sort() over entire length
    of the growing data set. I tried the second optimization described next, which had potential to be better suited 
    for billions of input values -- but it came out slower when using the file with 100,000 items.
    - Since the original code always calls "Collections.sort()" which loops over entire list. Since the list is already
    sorted (except for the one item being added added), one optimization could be to have only loop over subset until
    position is found where new item can be added, then break out of the loop.
    - This attempt is in following method.
        - IncrementalIQM_QuickAdd.addItemToDataSet_New()
    - A timing run showed this made things slower - not what I expected! Performance profiling with YourKit showed that 
    the inner for-loop in my "optimized" add was 4 times slower than  -- but there may be something in there that can
    make large improvement.
      
    3. Would your approach still be efficient if you needed to store the intermediate state between each IQM calculation 
    in a data store?  If not, how would you change it to meet this requirement?
    - This would result in very frequent writes to the data store. If done synchronously, it would be a huge performance 
    slowdown. One way to reduce the performance impact is to do the writes asynchronously (like in another worker thread).
    
      
    Automated tests:
    - Unit tests in class "com.sweetspotdiabetes.IncrementalIQM_CompareOldNew_Test". Different input files are used, and results compared 
    against known correct result. These test input files are here:
        - src\test\resources
    - Test that runs both the old code and new code at same time for each increment, and verifies new code gets 
    same result as old. This is in class "com.sweetspotdiabetes.IncrementalIQM_CompareOldNew_Test".


The End.
