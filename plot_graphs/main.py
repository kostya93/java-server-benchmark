import matplotlib.pyplot as plt
from matplotlib.font_manager import FontProperties


class Param(object):
    def __init__(self, regular, min, max, step, name):
        self.name = name
        self.regular = regular
        self.min = min
        self.max = max
        self.step = step


with open("../Params.txt", "r") as file:
    num_of_clients = Param(int(file.readline().strip()),
                           int(file.readline().strip()),
                           int(file.readline().strip()),
                           int(file.readline().strip()),
                           'num_of_clients')
    num_of_elements = Param(int(file.readline().strip()),
                            int(file.readline().strip()),
                            int(file.readline().strip()),
                            int(file.readline().strip()),
                            'num_of_elements')
    delta = Param(int(file.readline().strip()),
                  int(file.readline().strip()),
                  int(file.readline().strip()),
                  int(file.readline().strip()),
                  'delta')


def plot_graph(filename, param):
    with open('../{}.txt'.format(filename), "r") as f:
        arrays = []
        n = int(f.readline().strip())
        for i in range(n):
            s = int(f.readline().strip())
            arr = []
            for j in range(s):
                arr.append(int(f.readline().strip()))
            arrays.append(arr)

        plt.figure()
        plt.grid()
        plt.hold(True)
        plt.xlabel(param.name)
        x = []
        i = param.min
        while i <= param.max:
            x.append(i)
            i += param.step
        for k in range(n):
            plt.plot(x, arrays[k], label=str(k+1))

        fontP = FontProperties()
        fontP.set_size('small')
        plt.title(filename)
        plt.legend(prop=fontP)
        plt.savefig('{}.png'.format(filename))


plot_graph("ClientServerByDelta", delta)
plot_graph("ClientServerByNumOfClients", num_of_clients)
plot_graph("ClientServerByNumOfElements", num_of_elements)

plot_graph("ClientByDelta", delta)
plot_graph("ClientByNumOfClients", num_of_clients)
plot_graph("ClientByNumOfElements", num_of_elements)

plot_graph("RequestServerByDelta", delta)
plot_graph("RequestServerByNumOfClients", num_of_clients)
plot_graph("RequestServerByNumOfElements", num_of_elements)

plt.show()
