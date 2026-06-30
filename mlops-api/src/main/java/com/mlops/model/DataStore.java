package com.mlops.model;

import java.util.*;

/**
 * Data Store Class
 * Uses static maps
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    // workspaceId  -> MLWorkspace
    private final Map<String, MLWorkspace> workspaces = new HashMap<>();

    // modelId      -> MachineLearningModel
    private final Map<String, MachineLearningModel> models = new HashMap<>();

    // modelId      -> list of EvaluationMetric
    private final Map<String, List<EvaluationMetric>> metrics = new HashMap<>();

    private DataStore() {
        // Seed a couple of demo workspaces and models so the API isn't empty
        MLWorkspace ws1 = new MLWorkspace("WSVISION-01", "Computer Vision Lab", 500);
        MLWorkspace ws2 = new MLWorkspace("WSNLP-02", "NLP Research Team", 200);
        workspaces.put(ws1.getId(), ws1);
        workspaces.put(ws2.getId(), ws2);

        MachineLearningModel m1 = new MachineLearningModel(
                "MOD-8832", "TensorFlow", "DEPLOYED", 0.92, "WSVISION-01");
        MachineLearningModel m2 = new MachineLearningModel(
                "MOD-0011", "PyTorch", "TRAINING", 0.75, "WSNLP-02");
        MachineLearningModel m3 = new MachineLearningModel(
                "MOD-DEPR", "Scikit-Learn", "DEPRECATED", 0.60, "WSNLP-02");

        models.put(m1.getId(), m1);
        models.put(m2.getId(), m2);
        models.put(m3.getId(), m3);

        ws1.getModelIds().add(m1.getId());
        ws2.getModelIds().add(m2.getId());
        ws2.getModelIds().add(m3.getId());

        metrics.put(m1.getId(), new ArrayList<>());
        metrics.put(m2.getId(), new ArrayList<>());
        metrics.put(m3.getId(), new ArrayList<>());
    }

    public static DataStore getInstance() { return INSTANCE; }

    // Workspaces
    public Map<String, MLWorkspace> getWorkspaces() { return workspaces; }

    public Optional<MLWorkspace> findWorkspace(String id) {
        return Optional.ofNullable(workspaces.get(id));
    }

    public void saveWorkspace(MLWorkspace ws) { workspaces.put(ws.getId(), ws); }

    public void deleteWorkspace(String id) { workspaces.remove(id); }

    // Models
    public Map<String, MachineLearningModel> getModels() { return models; }

    public Optional<MachineLearningModel> findModel(String id) {
        return Optional.ofNullable(models.get(id));
    }

    public void saveModel(MachineLearningModel m) { models.put(m.getId(), m); }

    // Metrics
    public List<EvaluationMetric> getMetricsForModel(String modelId) {
        return metrics.computeIfAbsent(modelId, k -> new ArrayList<>());
    }

    public void addMetric(String modelId, EvaluationMetric metric) {
        getMetricsForModel(modelId).add(metric);
    }
}