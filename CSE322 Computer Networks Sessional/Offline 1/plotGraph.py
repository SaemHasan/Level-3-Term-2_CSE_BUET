import matplotlib.pyplot as plt
import numpy as np

X, Y = [], []
for line in open('mytest-throughput.dat', 'r'):
  values = [float(s) for s in line.split()]
  X.append(values[0])
  Y.append(values[1])


plt.figure(0)
plt.plot(X, Y)
plt.grid(True)
plt.xlabel('Time (s)', fontsize = 16)
plt.ylabel('Network Throughput (Kbps)', fontsize = 16)
# plt.show()

fig, ax = plt.subplots()

X, Y = [], []
for line in open('mytest-flow.dat', 'r'):
  values = [float(s) for s in line.split()]
  X.append(values[0])
  Y.append(values[1])


plt.figure(1)
plt.plot(X,Y)
ax.set_xlabel('Node/Flow', 
               fontweight ='bold')
ax.set_ylabel('Throughput (Kbps)', fontweight = 'bold')


plt.figure(2)
plt.bar(X, Y)
plt.xlabel('Node/Flow', fontsize = 16)
plt.ylabel('Throughput (Kbps)', fontsize = 16)
plt.show()


# X, Y = np.loadtxt('mytest-flow.dat', delimiter=' ', unpack=True)
 
# plt.bar(X, Y)
# plt.title('Line Graph using NUMPY')
# plt.xlabel('X')
# plt.ylabel('Y')
# plt.show()