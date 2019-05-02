\ENCODING=ISO-8859-1
\Problem name: ilog.cplex

Minimize
 obj: V(0)#0 + V(1)#1 + V(2)#2 + V(3)#3
Subject To
 V(Position)#0: V(0)#0 - 0.5 V(3)#3 >= 0
 V(Position)#1: V(0)#0 - 0.5 V(2)#2 >= 0
 V(Tresor1)#2:  V(1)#1 >= 0
 V(Tresor1)#3:  V(1)#1 >= 0
 V(Tanker)#4:   V(2)#2 >= 1
 V(Tanker)#5:   V(2)#2 >= 1
 V(Vide)#6:     V(3)#3 >= 0
 V(Vide)#7:     V(3)#3 >= 0
Bounds
      V(0)#0 >= 4.94065645841247e-324
      V(1)#1 >= 4.94065645841247e-324
      V(2)#2 >= 4.94065645841247e-324
      V(3)#3 >= 4.94065645841247e-324
End
