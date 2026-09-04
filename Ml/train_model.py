from pathlib import Path

import joblib
import pandas as pd

from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder


BASE_DIR = Path(__file__).resolve().parent
DATASET_PATH = BASE_DIR / "recovery_dataset.csv"
MODEL_PATH = BASE_DIR / "recovery_model.pkl"


df = pd.read_csv(DATASET_PATH)

X = df[
    [
        "amount",
        "attemptCount",
        "failureReason",
        "priority",
        "recommendedAction"
    ]
]

y = df["recovered"]


categorical_features = [
    "failureReason",
    "priority",
    "recommendedAction"
]

numerical_features = [
    "amount",
    "attemptCount"
]


preprocessor = ColumnTransformer(
    transformers=[
        (
            "categorical",
            OneHotEncoder(
                handle_unknown="ignore"
            ),
            categorical_features
        ),
        (
            "numerical",
            "passthrough",
            numerical_features
        )
    ]
)


model = RandomForestClassifier(
    n_estimators=200,
    max_depth=8,
    random_state=42,
    class_weight="balanced"
)


pipeline = Pipeline(
    steps=[
        (
            "preprocessor",
            preprocessor
        ),
        (
            "model",
            model
        )
    ]
)


X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y
)


pipeline.fit(
    X_train,
    y_train
)


predictions = pipeline.predict(
    X_test
)


accuracy = accuracy_score(
    y_test,
    predictions
)


print(
    f"Model accuracy: {accuracy:.4f}"
)


joblib.dump(
    pipeline,
    MODEL_PATH
)


print(
    f"Model saved at: {MODEL_PATH}"
)