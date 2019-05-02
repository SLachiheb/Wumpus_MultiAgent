\ENCODING=ISO-8859-1
\Problem name: ilog.cplex

Minimize
 obj: V(0)#0 + V(1)#1 + V(2)#2 + V(3)#3 + V(4)#4 + V(5)#5
Subject To
 V(Position)#0: V(0)#0 - 0.5 V(5)#5 >= 0
 V(Position)#1: V(0)#0 - 0.5 V(5)#5 >= 0
 V(Position)#2: V(0)#0 - 0.5 V(5)#5 >= 0
 V(Position)#3: V(0)#0 - 0.5 V(4)#4 >= 0
 V(Tresor1)#4:  V(1)#1 >= 0
 V(Tresor1)#5:  V(1)#1 >= 0
 V(Tresor1)#6:  V(1)#1 >= 0
 V(Tresor1)#7:  V(1)#1 >= 0
 V(Tresor2)#8:  V(2)#2 >= 0
 V(Tresor2)#9:  V(2)#2 >= 0
 V(Tresor2)#10: V(2)#2 >= 0
 V(Tresor2)#11: V(2)#2 >= 0
 V(Tresor3)#12: V(3)#3 >= 0
 V(Tresor3)#13: V(3)#3 >= 0
 V(Tresor3)#14: V(3)#3 >= 0
 V(Tresor3)#15: V(3)#3 >= 0
 V(Tanker)#16:  V(4)#4 >= 1
 V(Tanker)#17:  V(4)#4 >= 1
 V(Tanker)#18:  V(4)#4 >= 1
 V(Tanker)#19:  V(4)#4 >= 1
 V(Vide)#20:    V(5)#5 >= 0
 V(Vide)#21:    V(5)#5 >= 0
 V(Vide)#22:    V(5)#5 >= 0
 V(Vide)#23:    V(5)#5 >= 0
Bounds
      V(0)#0 >= 4.94065645841247e-324
      V(1)#1 >= 4.94065645841247e-324
      V(2)#2 >= 4.94065645841247e-324
      V(3)#3 >= 4.94065645841247e-324
      V(4)#4 >= 4.94065645841247e-324
      V(5)#5 >= 4.94065645841247e-324
End
