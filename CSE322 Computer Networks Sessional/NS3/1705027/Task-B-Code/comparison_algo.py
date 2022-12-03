import matplotlib.pyplot as plt
import pandas as pd

cnt = 1


def graphplot(dfs, cnt):
    headers = dfs[0].keys().values.tolist()
    for i in range(1, len(headers)):
        for df in dfs:
            x_values = df[headers[0]].tolist()
            y_values = df[headers[i]].tolist()
            plt.bar(x_values, y_values)
        plt.legend()
        plt.title(headers[i] + " vs " + headers[0])
        plt.xlabel(headers[0])
        plt.ylabel(headers[i])
        plt.grid()
        # plt.show()
        plt.savefig("taskB_result/" + str(cnt) + ".png")
        plt.clf()
        cnt = cnt + 1


if __name__ == "__main__":
    dfs = []
    df = pd.read_csv("taskB_result/result/finalData.csv")
    dfs.append(df)
    graphplot(dfs, cnt)
