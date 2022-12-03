import matplotlib.pyplot as plt
import pandas as pd


types = ["NODE", "FLOW", "SPEED", "PacketsPerSec"]
cnt = 1


def graphplot(df, cnt, type):
    headers = df.keys().values.tolist()
    for i in range(1, len(headers)):
        x_values = df[headers[0]].tolist()
        y_values = df[headers[i]].tolist()
        plt.plot(x_values, y_values, label=df.name)
        plt.legend()
        plt.title(headers[i] + " vs " + headers[0])
        plt.xlabel(headers[0])
        plt.ylabel(headers[i])
        plt.grid()
        plt.savefig("taskA1_result/" + type + str(cnt) + ".png")
        cnt = cnt + 1
        # plt.show()
        plt.clf()


if __name__ == "__main__":
    for type in types:
        df = pd.read_csv("taskA1_result/result/" + type + "_data.csv")
        df.name = type
        graphplot(df, cnt, type)
        # cnt =cnt+4
