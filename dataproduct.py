import os
from dataproduct.bigquery import BigQueryClient
from dataproduct.nais_api import get_deployments
from dotenv import load_dotenv

load_dotenv()


def main():
    nais_api_key = os.getenv("NAIS_API_TOKEN")
    if not nais_api_key:
        raise ValueError("NAIS_API_TOKEN environment variable is not set")
    dry_run = os.getenv("DRY_RUN", "false").lower() == "true"
    if dry_run:
        print("Dry run mode enabled. No data will be pushed to BigQuery.")

    bq_client = BigQueryClient()

    latest_deploy = bq_client.get_latest_deploy()
    if latest_deploy is None:
        print("No previous deploy found in BigQuery. Fetching all deployments.")
        exit(1)

    print(f"Latest deploy time in BigQuery: {latest_deploy}")

    deployments = get_deployments(nais_api_key, latest_deploy)
    print(f"Fetched {len(deployments)} deployments from Nais API.")

    if not deployments:
        print("No new deployments to push to BigQuery.")
        return

    bq_client.push_rows(deployments, dry_run=dry_run)
    print("Data pushed to BigQuery successfully.")


if __name__ == "__main__":
    main()
