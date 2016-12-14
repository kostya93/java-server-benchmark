package Gui;

/**
 * Created by kostya on 13.12.2016.
 */
class Parameters {
    private final String variableParameter;
    private final int min;
    private final int max;
    private final int step;
    private final int numOfRequests;
    private final int numOfElements;
    private final int numOfClients;
    private final int delta;
    private final int serverType;

    Parameters(String variableParameter, int min, int max, int step, int numOfRequests, int numOfElements, int numOfClients, int delta, int serverType) {
        this.variableParameter = variableParameter;
        this.min = min;
        this.max = max;
        this.step = step;
        this.numOfRequests = numOfRequests;
        this.numOfElements = numOfElements;
        this.numOfClients = numOfClients;
        this.delta = delta;
        this.serverType = serverType;
    }

    String getVariableParameter() {
        return variableParameter;
    }

    int getMin() {
        return min;
    }

    int getMax() {
        return max;
    }

    int getStep() {
        return step;
    }

    int getNumOfRequests() {
        return numOfRequests;
    }

    int getNumOfClients() {
        return numOfClients;
    }

    int getDelta() {
        return delta;
    }

    int getServerType() {
        return serverType;
    }

    int getNumOfElements() {
        return numOfElements;
    }

    @Override
    public String toString() {
        String nl = System.getProperty("line.separator");
        return String.format(
                "Изменяемый параметр: %s" + nl +
                        "Min: %d" + nl +
                        "Max: %d" + nl +
                        "Step: %d" + nl +
                        "Количество запросов, X: %d" + nl +
                        "Количество элементов, N: %d" + nl +
                        "Количество клиентов, M: %d" + nl +
                        "Пауза между запросами, Δ: %d" + nl +
                        "Архитектура: %d",
                variableParameter, min, max, step, numOfRequests,numOfElements, numOfClients, delta, serverType);
    }
}
