import os
from dataproduct.bigquery import BigQueryClient
from datetime import datetime
from dotenv import load_dotenv
from flask import Flask, request, jsonify

load_dotenv()

dry_run = os.getenv("DRY_RUN", "false").lower() == "true"
if dry_run:
    print("Dry run mode enabled. No data will be pushed to BigQuery.")

bq_client = BigQueryClient()

app = Flask(__name__)


@app.route("/api/deployment", methods=["POST"])
def deployment():
    body = request.get_json()
    print(f"From Vera: {body}")

    if "platform" not in body:
        body["platform"] = "stormaskin"

    if "deploymentSystem" not in body:
        body["deploymentSystem"] = "unknown (Vera)"

    if "deployTime" not in body:
        body["deployTime"] = datetime.today().strftime("%Y-%m-%d %H:%M:%S")

    deployment = {
        "application": body["application"],
        "deployTime": body["deployTime"],
        "deploymentSystem": body["deploymentSystem"],
        "environment": body["environment"],
        "platform": body["platform"],
        "version": body["version"],
    }

    bq_client.push_rows([deployment], dry_run=dry_run)
    print("Data pushed to BigQuery successfully.")

    return jsonify({"message": "success"})


if __name__ == "__main__":
    app.run()
