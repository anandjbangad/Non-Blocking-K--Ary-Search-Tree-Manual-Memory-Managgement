# Non-Blocking-K--Ary-Search-Tree-Manual-Memory-Managgement
This work presents the generalization of a Concurrent Binary Search Tree to a Non-Blocking leaf oriented Concurrent K-Ary Search Tree in Java. We used single-word compare-and-set operations to coordinate updates to the tree. Further we extend the implementation to work without Java’s garbage collector for manual memory management. We use the concept of thread local pool of pre-allocated nodes for the memory management. The potential ABA problem that arises is taken care by using AtomicStampedRefference