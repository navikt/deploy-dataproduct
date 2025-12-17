from google.cloud import bigquery
from google.cloud.bigquery import DatasetReference, TableReference
import json

# Define your query
LATEST_DEPLOY_QUERY = """
    SELECT *
    FROM `nais-analyse-prod-2dcc.deploys.from_devrapid`
    WHERE deployTime >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 7 DAY)
    ORDER BY deployTime DESC
    LIMIT 1
"""


class BigQueryClient:
    def __init__(self):
        self.project = 'nais-analyse-prod-2dcc'
        self.dataset_id = 'deploys'
        self.table_id = 'from_devrapid'

        self.client = bigquery.Client(project=self.project)
        self.table = self._ensure_table()

    def get_latest_deploy(self):
        query_job = self.client.query(LATEST_DEPLOY_QUERY)
        results = query_job.result()
        for row in results:
            return row.deployTime.isoformat()
        return None

    def push_rows(self, rows, dry_run=False):
        if dry_run:
            print(f"Dry run enabled. Would push {len(rows)} rows to BigQuery.")
            return

        errors = self.client.insert_rows(self.table, rows)
        if errors:
            print("Errors occurred while inserting rows: ", errors)
        else:
            print(
                f"Successfully inserted {len(rows)} rows into {self.table_id}.")

    def _ensure_table(self):
        dataset_ref = bigquery.DatasetReference(self.project, self.dataset_id)
        table_ref = bigquery.TableReference(dataset_ref, self.table_id)

        with open('schema.json', 'r') as f:
            schema = json.load(f)

        return self.client.create_table(bigquery.Table(
            table_ref, schema=schema), exists_ok=True)
