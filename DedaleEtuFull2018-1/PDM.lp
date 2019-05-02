\ENCODING=ISO-8859-1
\Problem name: ilog.cplex

Minimize
 obj: V(0)#0 + V(1)#1 + V(2)#2
Subject To
 V(Position)#0: V(0)#0 - 0.5 V(1)#1 >= 0
 V(Tanker)#1:   V(1)#1 >= 1
 V(Vide)#2:     V(2)#2 >= 0
Bounds
      V(0)#0 >= 4.94065645841247e-324
      V(1)#1 >= 4.94065645841247e-324
      V(2)#2 >= 4.94065645841247e-324
End
