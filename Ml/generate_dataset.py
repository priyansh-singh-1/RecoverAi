import random
import pandas as pd

random.seed(42)

failure_reasons = [
    "NETWORK_ERROR",
    "TIMEOUT",
    "INSUFFICIENT_FUNDS",
    "DECLINED",
    "UNKNOWN"
]

priorities = [
    "LOW",
    "MEDIUM",
    "HIGH",
    "CRITICAL"
]

actions = [
    "WAIT_AND_RETRY",
    "SEND_REMINDER",
    "OFFER_ALTERNATIVE_PAYMENT_METHOD",
    "ESCALATE_TO_HUMAN",
    "STOP"
]

rows = []

for _ in range(5000):

    amount = round(random.uniform(100, 20000), 2)
    attempt_count = random.randint(1, 5)

    failure_reason = random.choice(failure_reasons)
    priority = random.choice(priorities)
    action = random.choice(actions)

    probability = 0.50

    if failure_reason == "NETWORK_ERROR":
        probability += 0.25

    elif failure_reason == "TIMEOUT":
        probability += 0.20

    elif failure_reason == "INSUFFICIENT_FUNDS":
        probability += 0.05

    elif failure_reason == "DECLINED":
        probability -= 0.20

    else:
        probability -= 0.05

    if attempt_count == 1:
        probability += 0.10

    elif attempt_count >= 4:
        probability -= 0.25

    if action == "WAIT_AND_RETRY":
        probability += 0.10

    elif action == "STOP":
        probability -= 0.30

    elif action == "OFFER_ALTERNATIVE_PAYMENT_METHOD":
        probability += 0.05

    if amount > 10000:
        probability -= 0.05

    probability = max(
        0.05,
        min(
            0.95,
            probability
        )
    )

    recovered = 1 if random.random() < probability else 0

    rows.append({
        "amount": amount,
        "attemptCount": attempt_count,
        "failureReason": failure_reason,
        "priority": priority,
        "recommendedAction": action,
        "recovered": recovered
    })

df = pd.DataFrame(rows)

df.to_csv(
    "recovery_dataset.csv",
    index=False
)

print(
    f"Generated {len(df)} rows"
)

print(
    df["recovered"].value_counts(normalize=True)
)