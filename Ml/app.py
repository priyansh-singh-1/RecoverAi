import joblib
import pandas as pd

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(
    title="RecoverAI ML Service"
)

model = joblib.load(
    "recovery_model.pkl"
)

MODEL_VERSION = "recovery-model-v1"


class RecoveryPredictionRequest(BaseModel):

    amount: float
    attemptCount: int
    failureReason: str
    priority: str
    recommendedAction: str


class RecoveryPredictionResponse(BaseModel):

    recoveryProbability: float
    modelVersion: str


@app.get("/health")
def health():

    return {
        "status": "UP",
        "modelVersion": MODEL_VERSION
    }


@app.post(
    "/predict",
    response_model=RecoveryPredictionResponse
)
def predict(
    request: RecoveryPredictionRequest
):

    input_data = pd.DataFrame(
        [
            {
                "amount": request.amount,
                "attemptCount": request.attemptCount,
                "failureReason": request.failureReason,
                "priority": request.priority,
                "recommendedAction":
                    request.recommendedAction
            }
        ]
    )

    probability = model.predict_proba(
        input_data
    )[0][1]

    return RecoveryPredictionResponse(
        recoveryProbability=round(
            float(probability),
            4
        ),
        modelVersion=MODEL_VERSION
    )